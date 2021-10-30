/*
*  Copyright 2013 Square, Inc.
*
*  Licensed under the Apache License, Version 2.0 (the "License");
*  you may not use this file except in compliance with the License.
*  You may obtain a copy of the License at
*
*      http://www.apache.org/licenses/LICENSE-2.0
*
*  Unless required by applicable law or agreed to in writing, software
*  distributed under the License is distributed on an "AS IS" BASIS,
*  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
*  See the License for the specific language governing permissions and
*  limitations under the License.
*
*  This is a derivative work from Okio's Base64 implementation which can
*  be found here:
*
*      https://github.com/square/okio/blob/master/okio/src/commonMain/kotlin/okio/-Base64.kt
*
*  Original Author:
*
*      Alexander Y. Kleymenov
* */
@file:Suppress("SpellCheckingInspection", "MemberVisibilityCanBePrivate", "RedundantExplicitType")

package io.matthewnelson.component.base64

/**
 * This is a derivative work from Okio's Base64 implementation which can
 * be found here:
 *
 *     https://github.com/square/okio/blob/master/okio/src/commonMain/kotlin/okio/-Base64.kt
 *
 * @author original: Alexander Y. Kleymenov
 * @suppress
 * */
sealed class Base64 {

    internal abstract val encodingTable: ByteArray

    /**
     * Base64 encoding in accordance with RFC 4648 seciton 4
     * https://www.ietf.org/rfc/rfc4648.html#section-4
     * */
    object Default: Base64() {
        const val CHARS: String = "ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz0123456789+/"
        override val encodingTable: ByteArray = CHARS.encodeToByteArray()
    }

    /**
     * Base64UrlSafe encoding in accordance with RFC 4648 section 5
     * https://www.ietf.org/rfc/rfc4648.html#section-5
     *
     * @param [pad] specify whether or not to add padding character '='
     *  while encoding (true by default).
     * */
    data class UrlSafe(val pad: Boolean = true): Base64() {

        companion object {
            const val CHARS: String = "ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz0123456789-_"
            private val encodingTable: ByteArray = CHARS.encodeToByteArray()
        }

        override val encodingTable: ByteArray
            get() = Companion.encodingTable
    }
}

@Suppress("nothing_to_inline")
inline fun String.decodeBase64ToArray(): ByteArray? {
    return toCharArray().decodeBase64ToArray()
}

fun CharArray.decodeBase64ToArray(): ByteArray? {
    var limit = size

    // Disregard padding and/or whitespace from end of input
    while (limit > 0) {
        val c = this[limit - 1]
        if (c != '=' && c != '\n' && c != '\r' && c != ' ' && c != '\t') {
            break
        }
        limit--
    }

    // Was all padding, whitespace, or otherwise ignorable characters
    if (limit == 0) {
        return null
    }

    // If the input includes whitespace, this output array will be longer than necessary.
    val out: ByteArray = ByteArray((limit * 6L / 8L).toInt())
    var outCount: Int = 0
    var inCount: Int = 0

    var bitBuffer: Int = 0
    for (i in 0 until limit) {
        val c = this[i]

        val bits: Int
        if (c in 'A'..'Z') {
            // char ASCII value
            //  A    65    0
            //  Z    90    25 (ASCII - 65)
            bits = c.code - 65
        } else if (c in 'a'..'z') {
            // char ASCII value
            //  a    97    26
            //  z    122   51 (ASCII - 71)
            bits = c.code - 71
        } else if (c in '0'..'9') {
            // char ASCII value
            //  0    48    52
            //  9    57    61 (ASCII + 4)
            bits = c.code + 4
        } else if (c == '+' || c == '-') {
            bits = 62
        } else if (c == '/' || c == '_') {
            bits = 63
        } else if (c == '\n' || c == '\r' || c == ' ' || c == '\t') {
            continue
        } else {
            return null
        }

        // Append this char's 6 bits to the word.
        bitBuffer = bitBuffer shl 6 or bits

        // For every 4 chars of input, we accumulate 24 bits of output. Emit 3 bytes.
        inCount++
        if (inCount % 4 == 0) {
            out[outCount++] = (bitBuffer shr 16).toByte()
            out[outCount++] = (bitBuffer shr 8).toByte()
            out[outCount++] = bitBuffer.toByte()
        }
    }

    when (inCount % 4) {
        1 -> {
            // We read 1 char followed by "===". But 6 bits is a truncated byte! Fail.
            return null
        }
        2 -> {
            // We read 2 chars followed by "==". Emit 1 byte with 8 of those 12 bits.
            bitBuffer = bitBuffer shl 12
            out[outCount++] = (bitBuffer shr 16).toByte()
        }
        3 -> {
            // We read 3 chars, followed by "=". Emit 2 bytes for 16 of those 18 bits.
            bitBuffer = bitBuffer shl 6
            out[outCount++] = (bitBuffer shr 16).toByte()
            out[outCount++] = (bitBuffer shr 8).toByte()
        }
    }

    return if (outCount == out.size) {
        // If we sized our out array perfectly, we're done.
        out
    } else {
        // Copy the decoded bytes to a new, right-sized array.
        out.copyOf(outCount)
    }
}

@Suppress("nothing_to_inline")
inline fun ByteArray.encodeBase64(base64: Base64 = Base64.Default): String {
    return encodeBase64ToCharArray(base64).joinToString("")
}

@Suppress("nothing_to_inline")
inline fun ByteArray.encodeBase64ToCharArray(base64: Base64 = Base64.Default): CharArray {
    return encodeBase64ToByteArray(base64).let { bytes ->
        val chars = CharArray(bytes.size)
        for ((i, byte) in bytes.withIndex()) {
            chars[i] = byte.toInt().toChar()
        }
        chars
    }
}

fun ByteArray.encodeBase64ToByteArray(base64: Base64 = Base64.Default): ByteArray {
    val base64Lookup: ByteArray = base64.encodingTable

    val out = ByteArray((size + 2) / 3 * 4)

    var index = 0
    val end = size - size % 3
    var i = 0

    while (i < end) {
        val b0 = this[i++].toInt()
        val b1 = this[i++].toInt()
        val b2 = this[i++].toInt()
        out[index++] = base64Lookup[(b0 and 0xff shr 2)]
        out[index++] = base64Lookup[(b0 and 0x03 shl 4) or (b1 and 0xff shr 4)]
        out[index++] = base64Lookup[(b1 and 0x0f shl 2) or (b2 and 0xff shr 6)]
        out[index++] = base64Lookup[(b2 and 0x3f)]
    }

    val indicesLeftOver = size - end
    when (indicesLeftOver) {
        0 -> {}
        1 -> {
            val b0 = this[i].toInt()
            out[index++] = base64Lookup[b0 and 0xff shr 2]
            out[index++] = base64Lookup[b0 and 0x03 shl 4]
            if (base64 is Base64.Default || (base64 is Base64.UrlSafe && base64.pad)) {
                out[index++] = '='.code.toByte()
                out[index]   = '='.code.toByte()
            }
        }
        2 -> {
            val b0 = this[i++].toInt()
            val b1 = this[i].toInt()
            out[index++] = base64Lookup[(b0 and 0xff shr 2)]
            out[index++] = base64Lookup[(b0 and 0x03 shl 4) or (b1 and 0xff shr 4)]
            out[index++] = base64Lookup[(b1 and 0x0f shl 2)]
            if (base64 is Base64.Default || (base64 is Base64.UrlSafe && base64.pad)) {
                out[index]  = '='.code.toByte()
            }
        }
    }

    return if (base64 is Base64.UrlSafe && !base64.pad && indicesLeftOver != 0) {
        out.copyOf(index)
    } else {
        out
    }
}
