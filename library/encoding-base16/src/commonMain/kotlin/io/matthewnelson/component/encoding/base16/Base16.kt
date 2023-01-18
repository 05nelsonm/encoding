/*
 * Copyright (c) 2021 Matthew Nelson
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
@file:Suppress(
    "KotlinRedundantDiagnosticSuppress",
    "RedundantExplicitType",
    "SpellCheckingInspection",
)

package io.matthewnelson.component.encoding.base16

import kotlin.native.concurrent.SharedImmutable

@SharedImmutable
private val HEX_TABLE = "0123456789ABCDEF".encodeToByteArray()

@Suppress("NOTHING_TO_INLINE")
public inline fun String.decodeBase16ToArray(): ByteArray? {
    return toCharArray().decodeBase16ToArray()
}

public fun CharArray.decodeBase16ToArray(): ByteArray? {
    var limit = size
    while (limit > 0) {
        val c = this[limit - 1]
        if (c != '\n' && c != '\r' && c != ' ' && c != '\t') {
            break
        }
        limit--
    }

    if (limit == 0) {
        return ByteArray(0)
    }

    val out: ByteArray = ByteArray(limit / 2)
    var outCount: Int = 0
    var inCount: Int = 0

    var bitBuffer: Int = 0
    for (i in 0 until limit) {
        val c = this[i]

        val bits: Int
        when (c) {
            in '0'..'9' -> {
                // char ASCII value
                // 0     48    0
                // 9     57    9 (ASCII - 48)
                bits = c.code - 48
            }
            in 'A'..'F' -> {
                // char ASCII value
                //   A   65    10
                //   F   70    15 (ASCII - 55)
                bits = c.code - 55
            }
            '\n', '\r', ' ', '\t' -> {
                continue
            }
            else -> {
                return null
            }
        }

        // Append this char's 4 bits to the word
        bitBuffer = bitBuffer shl 4 or bits

        // For every 2 chars of input, we accumulate 8 bits of output data. Emit 1 byte
        inCount++
        if (inCount % 2 == 0) {
            out[outCount++] = bitBuffer.toByte()
        }
    }

    // 4*1 = 4 bits. Truncated, fail.
    if (inCount % 2 != 0) {
        return null
    }

    return if (outCount == out.size) {
        out
    } else {
        out.copyOf(outCount)
    }
}

@Suppress("NOTHING_TO_INLINE")
public inline fun ByteArray.encodeBase16(): String {
    return encodeBase16ToCharArray().joinToString("")
}

@Suppress("NOTHING_TO_INLINE")
public inline fun ByteArray.encodeBase16ToCharArray(): CharArray {
    return encodeBase16ToByteArray().let { bytes ->
        val chars = CharArray(bytes.size)
        for ((i, byte) in bytes.withIndex()) {
            chars[i] = byte.toInt().toChar()
        }
        chars
    }
}

public fun ByteArray.encodeBase16ToByteArray(): ByteArray {
    val base16Lookup: ByteArray = HEX_TABLE

    val out = ByteArray(size * 2)

    var outCount = 0
    for (byte in this) {
        val bits = byte.toInt() and 0xff
        out[outCount++] = base16Lookup[bits shr 4]
        out[outCount++] = base16Lookup[bits and 0x0f]
    }

    return out
}
