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
@file:Suppress("KotlinRedundantDiagnosticSuppress")

package io.matthewnelson.encoding.base32.internal

@Suppress("NOTHING_TO_INLINE")
internal inline fun Long.decodeOutMaxSize(): Long = (this * 5L / 8L)

@Suppress("NOTHING_TO_INLINE")
internal inline fun Long.encodeOutSize(willBePadded: Boolean): Long {
    var outSize: Long = ((this + 4L) / 5L) * 8L
    if (willBePadded) return outSize

    when (this - (this - this % 5)) {
        0L -> { /* no-op */ }
        1L -> outSize -= 6L
        2L -> outSize -= 4L
        3L -> outSize -= 3L
        4L -> outSize -= 1L
    }

    return outSize
}

@Suppress("NOTHING_TO_INLINE")
internal inline fun Byte.toBits(): Long = if (this < 0) this + 256L else toLong()
