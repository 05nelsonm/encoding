/*
 * Copyright (c) 2023 Matthew Nelson
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *         https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 **/
@file:Suppress("LocalVariableName")

package io.matthewnelson.encoding.core.internal

import io.matthewnelson.encoding.core.*
import io.matthewnelson.encoding.core.EncoderDecoder.Config
import io.matthewnelson.encoding.core.util.DecoderInput
import kotlin.contracts.ExperimentalContracts
import kotlin.contracts.InvocationKind
import kotlin.contracts.contract
import kotlin.math.min

@Throws(EncodingException::class)
@OptIn(ExperimentalContracts::class)
internal inline fun <C: Config> Decoder<C>.decode(
    input: DecoderInput,
    _get: (i: Int) -> Char,
): ByteArray {
    contract { callsInPlace(_get, InvocationKind.UNKNOWN) }
    val maxDecodeSize = config.decodeOutMaxSizeOrFail(input)
    val a = ByteArray(maxDecodeSize)
    @Suppress("DEPRECATION")
    val len = decodeTo(maxDecodeSizeArray = a, input.size, _get)
    if (len == maxDecodeSize) return a
    val copy = a.copyOf(len)
    if (config.backFillBuffers) {
        a.fill(0, 0, len)
    }
    return copy
}

@OptIn(ExperimentalContracts::class)
@Throws(EncodingException::class, IllegalArgumentException::class)
internal inline fun <C: Config> Decoder<C>.decodeBuffered(
    buf: ByteArray?,
    maxBufSize: Int,
    throwOnOverflow: Boolean,
    _get: (i: Int) -> Char,
    _input: () -> DecoderInput,
    _action: (buf: ByteArray, offset: Int, len: Int) -> Unit,
): Long {
    contract {
        callsInPlace(_get, InvocationKind.UNKNOWN)
        callsInPlace(_input, InvocationKind.AT_MOST_ONCE)
        callsInPlace(_action, InvocationKind.UNKNOWN)
    }

    if (buf != null) {
        // Ensure function caller passed in buf.size for maxBufSize
        check(buf.size == maxBufSize) { "buf.size[${buf.size}] != maxBufSize[$maxBufSize]" }
    }
    if (config.maxDecodeEmit == -1) {
        // EncoderDecoder.Config implementation has not updated to
        // new constructor which requires it to be greater than 0.
        throw EncodingException("Decoder misconfiguration. ${this}.config.maxDecodeEmit == -1")
    }
    require(maxBufSize > config.maxDecodeEmit) {
        val parameter = if (buf != null) "buf.size" else "maxBufSize"
        "$parameter[$maxBufSize] <= ${this}.config.maxDecodeEmit[${config.maxDecodeEmit}]"
    }

    val input = _input()
    try {
        config.decodeOutMaxSizeOrFail(input)
    } catch (e: EncodingSizeException) {
        // Only ignore EncodingSizeException such that any checks the
        // implementation has (such as Base32 Crockford check symbols)
        // can fall through and end early.

        if (throwOnOverflow) throw e
        -1
    }.let { maxDecodeSize ->
        if (maxDecodeSize !in 0..maxBufSize) return@let // Chunk

        // Maximum decoded size will be less than or equal to maxBufSize. One-shot it.
        val decoded = buf ?: ByteArray(maxDecodeSize)
        @Suppress("DEPRECATION")
        val len = decodeTo(maxDecodeSizeArray = decoded, input.size, _get)
        try {
            _action(decoded, 0, len)
        } finally {
            if (config.backFillBuffers) decoded.fill(0, 0, len)
        }
        return len.toLong()
    }

    // Chunk
    val _buf = buf ?: ByteArray(maxBufSize)
    val limit = _buf.size - config.maxDecodeEmit
    val inputSize = input.size
    var iBuf = 0
    var i = 0
    var size = 0L
    try {
        newDecoderFeed(out = { b -> _buf[iBuf++] = b }).use { feed ->
            while (i < inputSize) {
                feed.consume(input = _get(i++))
                if (iBuf <= limit) continue
                _action(_buf, 0, iBuf)
                size += iBuf
                iBuf = 0
            }
        }
        if (iBuf == 0) return size
        _action(_buf, 0, iBuf)
        size += iBuf
    } finally {
        if (config.backFillBuffers) _buf.fill(0)
    }
    return size
}

@Throws(EncodingException::class)
@OptIn(ExperimentalContracts::class)
@Deprecated("UNSAFE; do not reference directly. Used by decode/decodeBuffered only.")
internal inline fun <C: Config> Decoder<C>.decodeTo(
    maxDecodeSizeArray: ByteArray,
    inputSize: Int,
    _get: (i: Int) -> Char,
): Int {
    contract { callsInPlace(_get, InvocationKind.UNKNOWN) }
    if (inputSize == 0) return 0

    var i = 0
    try {
        newDecoderFeed(out = { b -> maxDecodeSizeArray[i++] = b }).use { feed ->
            var j = 0
            while (j < inputSize) {
                feed.consume(_get(j++))
            }
        }
    } catch (t: Throwable) {
        if (config.backFillBuffers) {
            maxDecodeSizeArray.fill(0, 0, min(maxDecodeSizeArray.size, i))
        }
        if (t is IndexOutOfBoundsException && i >= maxDecodeSizeArray.size) {
            // Something is wrong with the encoder's pre-calculation
            throw EncodingSizeException("Decoder's pre-calculation of Size[${maxDecodeSizeArray.size}] was incorrect", t)
        }
        throw t
    }
    return i
}
