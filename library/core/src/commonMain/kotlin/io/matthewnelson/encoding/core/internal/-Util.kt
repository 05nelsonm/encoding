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

package io.matthewnelson.encoding.core.internal

import io.matthewnelson.encoding.core.EncoderDecoder
import io.matthewnelson.encoding.core.EncodingException
import kotlin.contracts.ExperimentalContracts
import kotlin.contracts.InvocationKind
import kotlin.contracts.contract

internal inline fun EncoderDecoder.Feed<*>.closedException(): EncodingException {
    return EncodingException("$this is closed")
}

internal inline fun Char.isSpaceOrNewLine(): Boolean {
    return when(this) {
        '\n', '\r', ' ', '\t' -> true
        else -> false
    }
}

// Here for testing purposes. Implementation uses finalLen = 0
internal inline fun StringBuilder.commonWipe(len: Int, finalLen: Int): StringBuilder {
    setLength(0)
    // Kotlin/Js returns StringBuilder.length for capacity() as there is
    // no backing array. On all other platforms this will be the backing
    // array size. So will always return here on Kotlin/Js b/c we just set
    // length to 0.
    @Suppress("DEPRECATION")
    val cap = capacity()
    if (cap == 0) return this
    // All other platforms will set the new length from 0 to newLen, and
    // in doing so will fill their backing arrays via Array.fill
    val newLen = if (len !in 1..cap) cap else len
    setLength(newLen)
    setLength(finalLen)
    return this
}

@Throws(IndexOutOfBoundsException::class)
internal inline fun ByteArray.checkBounds(offset: Int, len: Int) { size.checkBounds(offset, len) { "size" } }
@Throws(IndexOutOfBoundsException::class)
internal inline fun CharArray.checkBounds(offset: Int, len: Int) { size.checkBounds(offset, len) { "size" } }
@Throws(IndexOutOfBoundsException::class)
internal inline fun CharSequence.checkBounds(offset: Int, len: Int) { length.checkBounds(offset, len) { "length" } }

@OptIn(ExperimentalContracts::class)
@Throws(IndexOutOfBoundsException::class)
internal inline fun Int.checkBounds(offset: Int, len: Int, paramName: () -> String) {
    contract { callsInPlace(paramName, InvocationKind.AT_MOST_ONCE) }
    val size = this
    if (offset < 0) throw IndexOutOfBoundsException("offset[$offset] < 0")
    if (len < 0) throw IndexOutOfBoundsException("len[$len] < 0")
    if (offset > size - len) {
        val parameter = paramName()
        throw IndexOutOfBoundsException("offset[$offset] > $parameter[$size] - len[$len]")
    }
}
