/*
 * Copyright (c) 2025 Matthew Nelson
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 **/
@file:Suppress("LocalVariableName", "NOTHING_TO_INLINE")

package io.matthewnelson.encoding.core.internal

import io.matthewnelson.encoding.core.Encoder
import io.matthewnelson.encoding.core.EncoderDecoder.Config
import io.matthewnelson.encoding.core.EncodingException
import io.matthewnelson.encoding.core.EncodingSizeException
import io.matthewnelson.encoding.core.use
import kotlin.contracts.ExperimentalContracts
import kotlin.contracts.InvocationKind
import kotlin.contracts.contract

private const val MAX_ENCODE_OUT_SIZE: Long = Int.MAX_VALUE.toLong()

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

@OptIn(ExperimentalContracts::class)
internal inline fun <C: Config> Encoder<C>.encode(
    data: ByteArray,
    _outFeed: () -> Encoder.OutFeed,
) {
    contract { callsInPlace(_outFeed, InvocationKind.AT_MOST_ONCE) }
    if (data.isEmpty()) return
    encode(data, out = _outFeed())
}

internal inline fun <C: Config> Encoder<C>.encode(
    data: ByteArray,
    out: Encoder.OutFeed,
) {
    newEncoderFeed(out).use { feed -> data.forEach(feed::consume) }
}

@OptIn(ExperimentalContracts::class)
@Throws(EncodingException::class)
internal inline fun <C: Config> Encoder<C>.encodeBuffered(
    data: ByteArray,
    buf: CharArray?,
    maxBufSize: Int,
    throwOnOverflow: Boolean,
    _action: (buf: CharArray, offset: Int, len: Int) -> Unit,
): Long {
    contract { callsInPlace(_action, InvocationKind.UNKNOWN) }

    if (buf != null) {
        // Ensure function caller passed in buf.size for maxBufSize
        check(buf.size == maxBufSize) { "buf.size[${buf.size}] != maxBufSize" }
    }
    if (config.maxEncodeEmitWithLineBreak == -1) {
        // EncoderDecoder.Config implementation has not updated to
        // new constructor which requires it to be greater than 0.
        throw EncodingException("Encoder misconfiguration. ${this}.config.maxEncodeEmitWithLineBreak == -1")
    }
    require(maxBufSize > config.maxEncodeEmitWithLineBreak) {
        val parameter = if (buf != null) "buf.size" else "maxBufSize"
        "$parameter[$maxBufSize] <= ${this}.config.maxEncodeEmitWithLineBreak[${config.maxEncodeEmitWithLineBreak}]"
    }
    if (data.isEmpty()) return 0L

    try {
        encodeOutMaxSizeOrFail(data.size) { it }
    } catch (e: EncodingSizeException) {
        if (throwOnOverflow) throw e
        -1
    }.let { maxEncodeSize ->
        if (maxEncodeSize !in 0..maxBufSize) return@let // Chunk

        // Maximum encoded size will be less than or equal to maxBufSize. One-shot it.
        var i = 0
        val encoded = buf ?: CharArray(maxEncodeSize)
        encode(data, out = { c -> encoded[i++] = c })
        try {
            _action(encoded, 0, i)
        } finally {
            if (config.backFillBuffers) encoded.fill('\u0000', 0, i)
        }
        return i.toLong()
    }

    // Chunk
    val _buf = buf ?: CharArray(maxBufSize)
    val limit = _buf.size - config.maxEncodeEmitWithLineBreak
    var iBuf = 0
    var size = 0L
    try {
        newEncoderFeed(out = { c -> _buf[iBuf++] = c }).use { feed ->
            for (b in data) {
                feed.consume(b)
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
        if (config.backFillBuffers) _buf.fill('\u0000')
    }
    return size
}
