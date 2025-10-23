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

package io.matthewnelson.encoding.base16.internal

import io.matthewnelson.encoding.base16.Base16

internal inline fun ((Boolean, Byte, Boolean) -> Base16.Config).build(
    b: Base16.Builder,
    noinline base16: (Base16.Config) -> Base16,
): Base16 {
    if (
        b._isLenient == Base16.DELEGATE.config.isLenient
        && b._lineBreakInterval == Base16.DELEGATE.config.lineBreakInterval
        && b._encodeToLowercase == Base16.DELEGATE.config.encodeToLowercase
    ) {
        return Base16.DELEGATE
    }
    val config = this(b._isLenient, b._lineBreakInterval, b._encodeToLowercase)
    return base16(config)
}
