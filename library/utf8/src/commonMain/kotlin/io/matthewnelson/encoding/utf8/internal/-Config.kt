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
@file:Suppress("NOTHING_TO_INLINE", "RedundantCompanionReference")

package io.matthewnelson.encoding.utf8.internal

import io.matthewnelson.encoding.utf8.UTF8

internal inline fun ((UTF8.ReplacementStrategy, Boolean) -> UTF8.Config).build(
    b: UTF8.Builder,
    noinline utf8: (UTF8.Config) -> UTF8,
): UTF8 {
    if (
        b._backFillBuffers == UTF8.Default.config.backFillBuffers
    ) {
        if (b._replacementStrategy == UTF8.Default.config.replacementStrategy) return UTF8.Default
        if (b._replacementStrategy == UTF8.ThrowOnInvalid.config.replacementStrategy) return UTF8.ThrowOnInvalid
    }
    val config = this(
        b._replacementStrategy,
        b._backFillBuffers,
    )
    return utf8(config)
}
