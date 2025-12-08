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
@file:Suppress("LocalVariableName", "NOTHING_TO_INLINE")

package io.matthewnelson.encoding.core.internal

import io.matthewnelson.encoding.core.*
import io.matthewnelson.encoding.core.EncoderDecoder.Config
import io.matthewnelson.encoding.core.util.DecoderInput
import kotlin.contracts.ExperimentalContracts
import kotlin.contracts.InvocationKind
import kotlin.contracts.contract
import kotlin.math.min

private const val MAX_ENCODE_OUT_SIZE: Long = Int.MAX_VALUE.toLong()

@Throws(EncodingException::class)
@OptIn(ExperimentalContracts::class)
internal inline fun <C: Config> Decoder<C>.decode(
    input: DecoderInput,
    _get: (i: Int) -> Char,
): ByteArray {
    contract { callsInPlace(_get, InvocationKind.UNKNOWN) }
    val maxDecodeSize = config.decodeOutMaxSizeOrFail(input)
    return decode(maxDecodeSize, input.size, _get)
}

@Throws(EncodingException::class)
@OptIn(ExperimentalContracts::class)
internal inline fun <C: Config> Decoder<C>.decode(
    maxDecodeSize: Int,
    inputSize: Int,
    _get: (i: Int) -> Char,
): ByteArray {
    contract { callsInPlace(_get, InvocationKind.UNKNOWN) }
    val a = ByteArray(maxDecodeSize)

    var i = 0
    try {
        newDecoderFeed(out = { b -> a[i++] = b }).use { feed ->
            var j = 0
            while (j < inputSize) {
                feed.consume(_get(j++))
            }
        }
    } catch (t: Throwable) {
        if (config.backFillBuffers) {
            a.fill(0, toIndex = min(a.size, i))
        }
        if (t is IndexOutOfBoundsException && i >= maxDecodeSize) {
            // Something is wrong with the encoder's pre-calculation
            throw EncodingSizeException("Encoder's pre-calculation of Size[$maxDecodeSize] was incorrect", t)
        }
        throw t
    }

    if (i == maxDecodeSize) return a

    val copy = a.copyOf(i)
    if (config.backFillBuffers) {
        a.fill(0, 0, i)
    }
    return copy
}

@OptIn(ExperimentalContracts::class)
@Throws(EncodingException::class, IllegalArgumentException::class)
internal inline fun <C: Config> Decoder<C>.decodeBuffered(
    maxBufSize: Int,
    _get: (i: Int) -> Char,
    _input: () -> DecoderInput,
    _action: (buf: ByteArray, offset: Int, len: Int) -> Unit,
): Long {
    contract {
        callsInPlace(_get, InvocationKind.UNKNOWN)
        callsInPlace(_input, InvocationKind.AT_MOST_ONCE)
        callsInPlace(_action, InvocationKind.UNKNOWN)
    }

    if (config.maxDecodeEmit == -1) {
        // EncoderDecoder.Config implementation has not updated to
        // new constructor which requires it to be greater than 0.
        throw EncodingException("Decoder misconfiguration. ${this}.config.maxDecodeEmit == -1")
    }
    require(maxBufSize > config.maxDecodeEmit) {
        "maxBufSize[$maxBufSize] <= ${this}.config.maxDecodeEmit[${config.maxDecodeEmit}]"
    }

    val input = _input()
    try {
        config.decodeOutMaxSizeOrFail(input)
    } catch (_: EncodingSizeException) {
        // Only ignore EncodingSizeException such that any checks
        // the implementation has (such as Base32 Crockford) can
        // fall through and end early.

        -1 // output size exceeded Int.MAX_VALUE
    }.let { maxDecodeSize ->
        if (maxDecodeSize !in 0..maxBufSize) return@let // Chunk

        // Maximum decoded size will be smaller than or equal to maxBufSize. One-shot it.
        val decoded = decode(maxDecodeSize, input.size, _get)
        try {
            _action(decoded, 0, decoded.size)
        } finally {
            if (config.backFillBuffers) decoded.fill(0)
        }
        return decoded.size.toLong()
    }

    // Chunk
    val buf = ByteArray(maxBufSize)
    val limit = buf.size - config.maxDecodeEmit
    val inputSize = input.size
    var iBuf = 0
    var i = 0
    var size = 0L
    try {
        newDecoderFeed(out = { b -> buf[iBuf++] = b }).use { feed ->
            while (i < inputSize) {
                feed.consume(input = _get(i++))
                if (iBuf <= limit) continue
                _action(buf, 0, iBuf)
                size += iBuf
                iBuf = 0
            }
        }
        if (iBuf == 0) return size
        _action(buf, 0, iBuf)
        size += iBuf
    } finally {
        if (config.backFillBuffers) buf.fill(0)
    }
    return size
}

/**
 * Fails if the returned [Long] for [Config.encodeOutMaxSize] exceeds [Int.MAX_VALUE].
 * */
@OptIn(ExperimentalContracts::class)
@Throws(EncodingSizeException::class)
internal inline fun <T: Any> Encoder<*>.encodeOutMaxSizeOrFail(
    size: Int,
    _block: (maxSize: Int) -> T,
): T {
    contract { callsInPlace(_block, InvocationKind.AT_MOST_ONCE) }

    val maxSize = config.encodeOutMaxSize(size.toLong())
    if (maxSize > MAX_ENCODE_OUT_SIZE) {
        throw Config.outSizeExceedsMaxEncodingSizeException(maxSize, MAX_ENCODE_OUT_SIZE)
    }

    return _block(maxSize.toInt())
}

internal inline fun <C: Config> Encoder<C>.encode(
    data: ByteArray,
    out: Encoder.OutFeed,
) {
    if (data.isEmpty()) return
    newEncoderFeed(out).use { feed -> data.forEach { b -> feed.consume(b) } }
}

internal inline fun EncoderDecoder.Feed<*>.closedException(): EncodingException {
    return EncodingException("$this is closed")
}

@Suppress("UnusedReceiverParameter")
internal inline fun Config.calculatedOutputNegativeEncodingSizeException(outSize: Number): EncodingSizeException {
    return EncodingSizeException("Calculated output of Size[$outSize] was negative")
}
