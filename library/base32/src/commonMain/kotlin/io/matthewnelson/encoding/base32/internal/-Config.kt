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

internal inline fun ((Boolean, Boolean, Byte, Char?, Boolean) -> Base32.Crockford.Config).build(
    b: Base32.Crockford.Builder,
    noinline crockford: (Base32.Crockford.Config, Any?) -> Base32.Crockford,
): Base32.Crockford {
    if (
        b._isLenient == Base32.Crockford.DELEGATE.config.isLenient
        && b._encodeLowercase == Base32.Crockford.DELEGATE.config.encodeLowercase
        && b._hyphenInterval == Base32.Crockford.DELEGATE.config.hyphenInterval
        && b._checkSymbol == Base32.Crockford.DELEGATE.config.checkSymbol
        && b._finalizeWhenFlushed == Base32.Crockford.DELEGATE.config.finalizeWhenFlushed
    ) {
        return Base32.Crockford.DELEGATE
    }
    val config = this(b._isLenient, b._encodeLowercase, b._hyphenInterval, b._checkSymbol, b._finalizeWhenFlushed)
    return crockford(config, null)
}

internal inline fun ((Boolean, Byte, Boolean, Boolean) -> Base32.Default.Config).build(
    b: Base32.Default.Builder,
    noinline default: (Base32.Default.Config, Any?) -> Base32.Default,
): Base32.Default {
    if (
        b._isLenient == Base32.Default.DELEGATE.config.isLenient
        && b._lineBreakInterval == Base32.Default.DELEGATE.config.lineBreakInterval
        && b._encodeLowercase == Base32.Default.DELEGATE.config.encodeLowercase
        && b._padEncoded == Base32.Default.DELEGATE.config.padEncoded
    ) {
        return Base32.Default.DELEGATE
    }
    val config = this(b._isLenient, b._lineBreakInterval, b._encodeLowercase, b._padEncoded)
    return default(config, null)
}

internal inline fun ((Boolean, Byte, Boolean, Boolean) -> Base32.Hex.Config).build(
    b: Base32.Hex.Builder,
    noinline hex: (Base32.Hex.Config, Any?) -> Base32.Hex,
): Base32.Hex {
    if (
        b._isLenient == Base32.Hex.DELEGATE.config.isLenient
        && b._lineBreakInterval == Base32.Hex.DELEGATE.config.lineBreakInterval
        && b._encodeLowercase == Base32.Hex.DELEGATE.config.encodeLowercase
        && b._padEncoded == Base32.Hex.DELEGATE.config.padEncoded
    ) {
        return Base32.Hex.DELEGATE
    }
    val config = this(b._isLenient, b._lineBreakInterval, b._encodeLowercase, b._padEncoded)
    return hex(config, null)
}
