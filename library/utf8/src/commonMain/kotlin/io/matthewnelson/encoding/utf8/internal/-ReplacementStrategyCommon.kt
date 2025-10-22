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

package io.matthewnelson.encoding.utf8.internal

import io.matthewnelson.encoding.core.Decoder
import io.matthewnelson.encoding.core.EncodingException
import io.matthewnelson.encoding.utf8.UTF8

internal expect inline fun UTF8.ReplacementStrategy.Companion.initializeKotlin(
    U_0034: UTF8.ReplacementStrategy,
    U_FFFD: UTF8.ReplacementStrategy,
): UTF8.ReplacementStrategy

@Throws(EncodingException::class)
internal inline fun UTF8.ReplacementStrategy.doOutput(out: Decoder.OutFeed) {
    when (size) {
        UTF8.ReplacementStrategy.U_0034.size -> {
            out.output('?'.code.toByte())
        }
        UTF8.ReplacementStrategy.U_FFFD.size -> {
            out.output(0xef.toByte())
            out.output(0xbf.toByte())
            out.output(0xbd.toByte())
        }
        else -> throw EncodingException("Malformed UTF-8 character sequence")
    }
}

@Throws(EncodingException::class)
internal inline fun UTF8.ReplacementStrategy.sizeOrThrow(): Int {
    if (size == UTF8.ReplacementStrategy.THROW.size) {
        throw EncodingException("Malformed UTF-8 character sequence")
    }
    return size
}
