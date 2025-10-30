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
@file:Suppress("NOTHING_TO_INLINE")

package io.matthewnelson.encoding.base32.internal

import io.matthewnelson.encoding.core.EncoderDecoder

private const val MAX_UNENCODED_SIZE: Long = (Long.MAX_VALUE / 8L) * 5L

internal inline fun EncoderDecoder.Config.Companion.decodeOutMaxSize64(encodedSize: Long): Long {
    // Divide first instead of multiplying which ensures the Long
    // doesn't overflow. To do it this way, also need to calculate
    // the remainder separately then add it back in.
    val div = encodedSize / 8L
    val rem = encodedSize.rem(8L).toFloat() // 0.0 - 7.0
    return (div * 5L) + (rem * 5.0F / 8.0F).toLong()
}

internal inline fun EncoderDecoder.Config.Companion.decodeOutMaxSize32(encodedSize: Int): Int {
    return (encodedSize.toLong() * 5L / 8L).toInt()
}

internal inline fun EncoderDecoder.Config.Companion.encodeOutSize64(unEncodedSize: Long, willBePadded: Boolean): Long {
    if (unEncodedSize > MAX_UNENCODED_SIZE) {
        throw outSizeExceedsMaxEncodingSizeException(unEncodedSize, Long.MAX_VALUE)
    }
    var outSize = (unEncodedSize + 4L) / 5L * 8L
    if (!willBePadded) {
        when (unEncodedSize.rem(5L)) {
            0L -> { /* no-op */ }
            1L -> outSize -= 6L
            2L -> outSize -= 4L
            3L -> outSize -= 3L
            4L -> outSize -= 1L
        }
    }
    return outSize
}

internal inline fun Byte.toBits(): Long = if (this < 0) this + 256L else toLong()
