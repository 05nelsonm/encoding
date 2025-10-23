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

package io.matthewnelson.encoding.base64.internal

import io.matthewnelson.encoding.base64.Base64

internal inline fun ((Boolean, Byte, Boolean, Boolean) -> Base64.Config).build(
    b: Base64.Builder,
    noinline base64: (Base64.Config) -> Base64,
): Base64 {
    if (
        b._isLenient == Base64.Default.DELEGATE.config.isLenient
        && b._lineBreakInterval == Base64.Default.DELEGATE.config.lineBreakInterval
        && b._padEncoded == Base64.Default.DELEGATE.config.padEncoded
    ) {
        return if (b._encodeToUrlSafe) Base64.UrlSafe.DELEGATE else Base64.Default.DELEGATE
    }
    val config = this(b._isLenient, b._lineBreakInterval, b._encodeToUrlSafe, b._padEncoded)
    return base64(config)
}
