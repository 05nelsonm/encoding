/*
*  Copyright 2021 Matthew Nelson
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
* */
@file:Suppress(
    "KotlinRedundantDiagnosticSuppress",
    "MemberVisibilityCanBePrivate",
    "PrivatePropertyName",
    "RedundantExplicitType",
    "SpellCheckingInspection",
)

package io.matthewnelson.component.encoding.base32

import io.matthewnelson.encoding.builders.Base32Crockford
import io.matthewnelson.encoding.builders.Base32Default
import io.matthewnelson.encoding.builders.Base32Hex
import io.matthewnelson.encoding.core.Decoder.Companion.decodeToByteArrayOrNull
import io.matthewnelson.encoding.core.internal.InternalEncodingApi
import io.matthewnelson.encoding.core.util.char
import kotlin.jvm.JvmOverloads
import kotlin.jvm.JvmSynthetic
import kotlin.native.concurrent.SharedImmutable

@SharedImmutable
private val CROCKFORD_TABLE = Base32.Crockford.CHARS.encodeToByteArray()
@SharedImmutable
private val DEFAULT_TABLE = Base32.Default.CHARS.encodeToByteArray()
@SharedImmutable
private val HEX_TABLE = Base32.Hex.CHARS.encodeToByteArray()

@PublishedApi
@InternalEncodingApi
internal val COMPATIBILITY_DEFAULT: io.matthewnelson.encoding.base32.Base32.Default = Base32Default {
    isLenient = true
    acceptLowercase = false
    encodeToLowercase = false
    padEncoded = true
}

@PublishedApi
@InternalEncodingApi
internal val COMPATIBILITY_HEX: io.matthewnelson.encoding.base32.Base32.Hex = Base32Hex {
    isLenient = true
    acceptLowercase = false
    encodeToLowercase = false
    padEncoded = true
}

public sealed class Base32 {

    @get:JvmSynthetic
    internal abstract val encodingTable: ByteArray

    /**
     * Base32 Crockford encoding in accordance with
     * https://www.crockford.com/base32.html
     *
     * @param [checkSymbol] specify an optional check symbol to be appended when encoding,
     *  or verified upon decoding.
     * @throws [IllegalArgumentException] if [checkSymbol] is not one of the accepted
     *  symbols (*, ~, $, =, U, u) or `null` to omit (omitted by default)
     * */
    public data class Crockford @JvmOverloads constructor(val checkSymbol: Char? = null): Base32() {

        init {
            when (checkSymbol) {
                null, '*', '~', '$', '=', 'U', 'u' -> { /* allowed */ }
                else -> throw IllegalArgumentException("""
                    Crockford's optional check symbol '$checkSymbol' not recognized.
                    Must be one of the following characters: *, ~, $, =, U, u
                    Or null to omit
                """.trimIndent()
                )
            }
        }

        public companion object {
            public const val CHARS: String = io.matthewnelson.encoding.base32.Base32.Crockford.CHARS
        }

        override val encodingTable: ByteArray get() = CROCKFORD_TABLE
        inline val hasCheckSymbol: Boolean get() = checkSymbol != null
        inline val checkByte: Byte? get() = checkSymbol?.uppercaseChar()?.code?.toByte()
    }

    /**
     * Base32 encoding in accordance with RFC 4648 section 6
     * https://www.ietf.org/rfc/rfc4648.html#section-6
     * */
    public object Default: Base32() {
        public const val CHARS: String = io.matthewnelson.encoding.base32.Base32.Default.CHARS
        override val encodingTable: ByteArray get() = DEFAULT_TABLE
    }

    /**
     * Base32Hex encoding in accordance with RFC 4648 section 7
     * https://www.ietf.org/rfc/rfc4648.html#section-7
     * */
    public object Hex: Base32() {
        public const val CHARS: String = io.matthewnelson.encoding.base32.Base32.Hex.CHARS
        override val encodingTable: ByteArray get() = HEX_TABLE
    }
}

@JvmOverloads
@Suppress("NOTHING_TO_INLINE")
public inline fun String.decodeBase32ToArray(base32: Base32 = Base32.Default): ByteArray? {
    @OptIn(InternalEncodingApi::class)
    return when (base32) {
        is Base32.Crockford -> {
            decodeToByteArrayOrNull(Base32Crockford {
                isLenient = true
                encodeToLowercase = false
                hyphenInterval = 0
                checkByte(base32.checkByte?.char)
            })
        }
        is Base32.Default -> {
            decodeToByteArrayOrNull(COMPATIBILITY_DEFAULT)
        }
        is Base32.Hex -> {
            decodeToByteArrayOrNull(COMPATIBILITY_HEX)
        }
    }
}

@JvmOverloads
public fun CharArray.decodeBase32ToArray(base32: Base32 = Base32.Default): ByteArray? {
    @OptIn(InternalEncodingApi::class)
    return when (base32) {
        is Base32.Crockford -> {
            decodeToByteArrayOrNull(Base32Crockford {
                isLenient = true
                encodeToLowercase = false
                hyphenInterval = 0
                checkByte(base32.checkByte?.char)
            })
        }
        is Base32.Default -> {
            decodeToByteArrayOrNull(COMPATIBILITY_DEFAULT)
        }
        is Base32.Hex -> {
            decodeToByteArrayOrNull(COMPATIBILITY_HEX)
        }
    }
}

@JvmOverloads
@Suppress("NOTHING_TO_INLINE")
public inline fun ByteArray.encodeBase32(base32: Base32 = Base32.Default): String {
    return encodeBase32ToCharArray(base32).joinToString("")
}

@JvmOverloads
@Suppress("NOTHING_TO_INLINE")
public inline fun ByteArray.encodeBase32ToCharArray(base32: Base32 = Base32.Default): CharArray {
    return encodeBase32ToByteArray(base32).let { bytes ->
        val chars = CharArray(bytes.size)
        for ((i, byte) in bytes.withIndex()) {
            chars[i] = byte.toInt().toChar()
        }
        chars
    }
}

@JvmOverloads
public fun ByteArray.encodeBase32ToByteArray(base32: Base32 = Base32.Default): ByteArray {
    val base32Lookup: ByteArray = base32.encodingTable

    val out = ByteArray((size + 4) / 5 * 8)

    var index = 0
    val end = size - size % 5
    var i = 0

    while (i < end) {
        var bitBuffer: Long = 0L

        repeat(5) {
            bitBuffer = (bitBuffer shl 8) + this.retrieveBits(i++)
        }

        out[index++] = base32Lookup[(bitBuffer shr 35 and 0x1fL).toInt()] // 40-1*5 = 35
        out[index++] = base32Lookup[(bitBuffer shr 30 and 0x1fL).toInt()] // 40-2*5 = 30
        out[index++] = base32Lookup[(bitBuffer shr 25 and 0x1fL).toInt()] // 40-3*5 = 25
        out[index++] = base32Lookup[(bitBuffer shr 20 and 0x1fL).toInt()] // 40-4*5 = 20
        out[index++] = base32Lookup[(bitBuffer shr 15 and 0x1fL).toInt()] // 40-5*5 = 15
        out[index++] = base32Lookup[(bitBuffer shr 10 and 0x1fL).toInt()] // 40-6*5 = 10
        out[index++] = base32Lookup[(bitBuffer shr  5 and 0x1fL).toInt()] // 40-7*5 = 5
        out[index++] = base32Lookup[(bitBuffer        and 0x1fL).toInt()] // 40-8*5 = 0
    }

    val indicesLeftOver = size - end
    var bitBuffer: Long = 0L
    when (indicesLeftOver) {
        0 -> {}
        1 -> { // 8*1 = 8 bits
            bitBuffer = (bitBuffer shl 8) + this.retrieveBits(i)
            out[index++] = base32Lookup[(bitBuffer shr 3 and 0x1fL).toInt()] // 8-1*5 = 3
            out[index++] = base32Lookup[(bitBuffer shl 2 and 0x1fL).toInt()] // 5-3 = 2
            if (base32 !is Base32.Crockford) {
                out[index++] = '='.code.toByte()
                out[index++] = '='.code.toByte()
                out[index++] = '='.code.toByte()
                out[index++] = '='.code.toByte()
                out[index++] = '='.code.toByte()
                out[index]   = '='.code.toByte()
            }
        }
        2 -> { // 8*2 = 16 bits
            bitBuffer = (bitBuffer shl 8) + this.retrieveBits(i++)
            bitBuffer = (bitBuffer shl 8) + this.retrieveBits(i)
            out[index++] = base32Lookup[(bitBuffer shr 11 and 0x1fL).toInt()] // 16-1*5 = 11
            out[index++] = base32Lookup[(bitBuffer shr  6 and 0x1fL).toInt()] // 16-2*5 = 6
            out[index++] = base32Lookup[(bitBuffer shr  1 and 0x1fL).toInt()] // 16-3*5 = 1
            out[index++] = base32Lookup[(bitBuffer shl  4 and 0x1fL).toInt()] // 5-1 = 4
            if (base32 !is Base32.Crockford) {
                out[index++] = '='.code.toByte()
                out[index++] = '='.code.toByte()
                out[index++] = '='.code.toByte()
                out[index]   = '='.code.toByte()
            }
        }
        3 -> { // 8*3 = 24 bits
            bitBuffer = (bitBuffer shl 8) + this.retrieveBits(i++)
            bitBuffer = (bitBuffer shl 8) + this.retrieveBits(i++)
            bitBuffer = (bitBuffer shl 8) + this.retrieveBits(i)
            out[index++] = base32Lookup[(bitBuffer shr 19 and 0x1fL).toInt()] // 24-1*5 = 19
            out[index++] = base32Lookup[(bitBuffer shr 14 and 0x1fL).toInt()] // 24-2*5 = 14
            out[index++] = base32Lookup[(bitBuffer shr  9 and 0x1fL).toInt()] // 24-3*5 = 9
            out[index++] = base32Lookup[(bitBuffer shr  4 and 0x1fL).toInt()] // 24-4*5 = 4
            out[index++] = base32Lookup[(bitBuffer shl  1 and 0x1fL).toInt()] // 5-4 = 1
            if (base32 !is Base32.Crockford) {
                out[index++] = '='.code.toByte()
                out[index++] = '='.code.toByte()
                out[index]   = '='.code.toByte()
            }
        }
        4 -> { // 8*4 = 32 bits
            bitBuffer = (bitBuffer shl 8) + this.retrieveBits(i++)
            bitBuffer = (bitBuffer shl 8) + this.retrieveBits(i++)
            bitBuffer = (bitBuffer shl 8) + this.retrieveBits(i++)
            bitBuffer = (bitBuffer shl 8) + this.retrieveBits(i)
            out[index++] = base32Lookup[(bitBuffer shr 27 and 0x1fL).toInt()] // 32-1*5 = 27
            out[index++] = base32Lookup[(bitBuffer shr 22 and 0x1fL).toInt()] // 32-2*5 = 22
            out[index++] = base32Lookup[(bitBuffer shr 17 and 0x1fL).toInt()] // 32-3*5 = 17
            out[index++] = base32Lookup[(bitBuffer shr 12 and 0x1fL).toInt()] // 32-4*5 = 12
            out[index++] = base32Lookup[(bitBuffer shr  7 and 0x1fL).toInt()] // 32-5*5 = 7
            out[index++] = base32Lookup[(bitBuffer shr  2 and 0x1fL).toInt()] // 32-6*5 = 2
            out[index++] = base32Lookup[(bitBuffer shl  3 and 0x1fL).toInt()] // 5-2 = 3
            if (base32 !is Base32.Crockford) {
                out[index] = '='.code.toByte()
            }
        }
    }

    return if (base32 is Base32.Crockford) {
        if (!base32.hasCheckSymbol && indicesLeftOver == 0) {
            // no need to array copy
            out
        } else {
            // need to resize array and, if present, add the check symbol
            val newOut = out.copyOf(index + if (base32.hasCheckSymbol) 1 else 0)
            base32.checkByte?.let { newOut[newOut.lastIndex] = it }
            newOut
        }
    } else {
        out
    }
}

@Suppress("NOTHING_TO_INLINE")
private inline fun ByteArray.retrieveBits(index: Int): Long =
    this[index].toLong().let { bits ->
        return if (bits < 0) {
            bits + 256L
        } else {
            bits
        }
    }
