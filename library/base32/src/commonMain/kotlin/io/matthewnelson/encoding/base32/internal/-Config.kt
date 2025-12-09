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
@file:Suppress("NOTHING_TO_INLINE")

package io.matthewnelson.encoding.base32.internal

import io.matthewnelson.encoding.base32.Base32
import io.matthewnelson.encoding.core.EncoderDecoder

internal inline fun ((Boolean, Boolean, Byte, Char?, Boolean, Boolean) -> Base32.Crockford.Config).build(
    b: Base32.Crockford.Builder,
    noinline crockford: (Base32.Crockford.Config, Any?) -> Base32.Crockford,
): Base32.Crockford {
    if (
        b._isLenient == Base32.Crockford.DELEGATE.config.isLenient
        && b._encodeLowercase == Base32.Crockford.DELEGATE.config.encodeLowercase
        && b._hyphenInterval == Base32.Crockford.DELEGATE.config.hyphenInterval
        && b._checkSymbol == Base32.Crockford.DELEGATE.config.checkSymbol
        && b._finalizeWhenFlushed == Base32.Crockford.DELEGATE.config.finalizeWhenFlushed
        && b._backFillBuffers == Base32.Crockford.DELEGATE.config.backFillBuffers
    ) {
        return Base32.Crockford.DELEGATE
    }
    val config = this(
        b._isLenient,
        b._encodeLowercase,
        b._hyphenInterval,
        b._checkSymbol,
        b._finalizeWhenFlushed,
        b._backFillBuffers,
    )
    return crockford(config, null)
}

internal inline fun ((Boolean, Byte, Boolean, Boolean, Boolean, Boolean) -> Base32.Default.Config).build(
    b: Base32.Default.Builder,
    noinline default: (Base32.Default.Config, Any?) -> Base32.Default,
): Base32.Default {
    if (
        b._isLenient == Base32.Default.DELEGATE.config.isLenient
        && b._lineBreakInterval == Base32.Default.DELEGATE.config.lineBreakInterval
        && b._lineBreakResetOnFlush == Base32.Default.DELEGATE.config.lineBreakResetOnFlush
        && b._encodeLowercase == Base32.Default.DELEGATE.config.encodeLowercase
        && b._padEncoded == Base32.Default.DELEGATE.config.padEncoded
        && b._backFillBuffers == Base32.Default.DELEGATE.config.backFillBuffers
    ) {
        return Base32.Default.DELEGATE
    }
    val config = this(
        b._isLenient,
        b._lineBreakInterval,
        b._lineBreakResetOnFlush,
        b._encodeLowercase,
        b._padEncoded,
        b._backFillBuffers,
    )
    return default(config, null)
}

internal inline fun ((Boolean, Byte, Boolean, Boolean, Boolean, Boolean) -> Base32.Hex.Config).build(
    b: Base32.Hex.Builder,
    noinline hex: (Base32.Hex.Config, Any?) -> Base32.Hex,
): Base32.Hex {
    if (
        b._isLenient == Base32.Hex.DELEGATE.config.isLenient
        && b._lineBreakInterval == Base32.Hex.DELEGATE.config.lineBreakInterval
        && b._lineBreakResetOnFlush == Base32.Hex.DELEGATE.config.lineBreakResetOnFlush
        && b._encodeLowercase == Base32.Hex.DELEGATE.config.encodeLowercase
        && b._padEncoded == Base32.Hex.DELEGATE.config.padEncoded
        && b._backFillBuffers == Base32.Hex.DELEGATE.config.backFillBuffers
    ) {
        return Base32.Hex.DELEGATE
    }
    val config = this(
        b._isLenient,
        b._lineBreakInterval,
        b._lineBreakResetOnFlush,
        b._encodeLowercase,
        b._padEncoded,
        b._backFillBuffers,
    )
    return hex(config, null)
}

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
