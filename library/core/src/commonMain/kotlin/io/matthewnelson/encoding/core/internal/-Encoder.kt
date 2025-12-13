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

// Does not check offset/len, thus the *Unsafe suffix
@OptIn(ExperimentalContracts::class)
internal inline fun <C: Config> Encoder<C>.encodeUnsafe(
    data: ByteArray,
    offset: Int,
    len: Int,
    _outFeed: () -> Encoder.OutFeed,
) {
    contract { callsInPlace(_outFeed, InvocationKind.AT_MOST_ONCE) }
    if (len == 0) return
    @Suppress("DEPRECATION")
    encodeToUnsafe(data, offset, len, out = _outFeed())
}

// Does not check offset/len, thus the *Unsafe suffix
@OptIn(ExperimentalContracts::class)
@Throws(EncodingException::class)
internal inline fun <C: Config> Encoder<C>.encodeBufferedUnsafe(
    data: ByteArray,
    offset: Int,
    len: Int,
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
    if (len == 0) return 0L

    try {
        config.encodeOutMaxSize(len)
    } catch (e: EncodingSizeException) {
        if (throwOnOverflow) throw e
        -1
    }.let { maxEncodeSize ->
        if (maxEncodeSize !in 0..maxBufSize) return@let // Chunk

        // Maximum encoded size will be less than or equal to maxBufSize. One-shot it.
        var i = 0
        val encoded = buf ?: CharArray(maxEncodeSize)
        @Suppress("DEPRECATION")
        encodeToUnsafe(data, offset, len, out = { c -> encoded[i++] = c })
        try {
            _action(encoded, 0, i)
        } finally {
            if (config.backFillBuffers) encoded.fill('\u0000', 0, i)
        }
        return i.toLong()
    }

    // Chunk
    val _buf = buf ?: CharArray(maxBufSize)
    var size = 0L
    try {
        var iBuf = 0
        newEncoderFeed(out = { c -> _buf[iBuf++] = c }).use { feed ->
            val limit = _buf.size - config.maxEncodeEmitWithLineBreak
            for (i in 0 until len) {
                feed.consume(data[offset + i])
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

// Does not check offset/len, thus the *Unsafe suffix
@Deprecated("UNSAFE; do not reference directly. Used by encodeUnsafe/encodeBufferedUnsafe only.")
internal inline fun <C: Config> Encoder<C>.encodeToUnsafe(
    data: ByteArray,
    offset: Int,
    len: Int,
    out: Encoder.OutFeed,
) {
    newEncoderFeed(out).use { feed ->
        repeat(len) { i ->
            feed.consume(data[offset + i])
        }
    }
}
