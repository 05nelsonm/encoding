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
import io.matthewnelson.encoding.core.Encoder.Companion.encodeToByteArray
import io.matthewnelson.encoding.core.Encoder.Companion.encodeToCharArray
import io.matthewnelson.encoding.core.Encoder.Companion.encodeToString
import io.matthewnelson.encoding.core.internal.InternalEncodingApi
import io.matthewnelson.encoding.core.util.char
import kotlin.jvm.JvmOverloads

@PublishedApi
@InternalEncodingApi
internal val COMPATIBILITY_DEFAULT: io.matthewnelson.encoding.base32.Base32.Default = Base32Default {
    isLenient = true
    encodeToLowercase = false
    padEncoded = true
}

@PublishedApi
@InternalEncodingApi
internal val COMPATIBILITY_HEX: io.matthewnelson.encoding.base32.Base32.Hex = Base32Hex {
    isLenient = true
    encodeToLowercase = false
    padEncoded = true
}

public sealed class Base32 {

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

        inline val hasCheckSymbol: Boolean get() = checkSymbol != null
        inline val checkByte: Byte? get() = checkSymbol?.uppercaseChar()?.code?.toByte()
    }

    /**
     * Base32 encoding in accordance with RFC 4648 section 6
     * https://www.ietf.org/rfc/rfc4648.html#section-6
     * */
    public object Default: Base32() {
        public const val CHARS: String = io.matthewnelson.encoding.base32.Base32.Default.CHARS
    }

    /**
     * Base32Hex encoding in accordance with RFC 4648 section 7
     * https://www.ietf.org/rfc/rfc4648.html#section-7
     * */
    public object Hex: Base32() {
        public const val CHARS: String = io.matthewnelson.encoding.base32.Base32.Hex.CHARS
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
                checkSymbol(base32.checkByte?.char)
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
                checkSymbol(base32.checkByte?.char)
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
    @OptIn(InternalEncodingApi::class)
    return when (base32) {
        is Base32.Crockford -> {
            encodeToString(Base32Crockford {
                isLenient = true
                encodeToLowercase = false
                hyphenInterval = 0
                checkSymbol(base32.checkByte?.char)
            })
        }
        is Base32.Default -> {
            encodeToString(COMPATIBILITY_DEFAULT)
        }
        is Base32.Hex -> {
            encodeToString(COMPATIBILITY_HEX)
        }
    }
}

@JvmOverloads
@Suppress("NOTHING_TO_INLINE")
public inline fun ByteArray.encodeBase32ToCharArray(base32: Base32 = Base32.Default): CharArray {
    @OptIn(InternalEncodingApi::class)
    return when (base32) {
        is Base32.Crockford -> {
            encodeToCharArray(Base32Crockford {
                isLenient = true
                encodeToLowercase = false
                hyphenInterval = 0
                checkSymbol(base32.checkByte?.char)
            })
        }
        is Base32.Default -> {
            encodeToCharArray(COMPATIBILITY_DEFAULT)
        }
        is Base32.Hex -> {
            encodeToCharArray(COMPATIBILITY_HEX)
        }
    }
}

@JvmOverloads
public fun ByteArray.encodeBase32ToByteArray(base32: Base32 = Base32.Default): ByteArray {
    @OptIn(InternalEncodingApi::class)
    return when (base32) {
        is Base32.Crockford -> {
            encodeToByteArray(Base32Crockford {
                isLenient = true
                encodeToLowercase = false
                hyphenInterval = 0
                checkSymbol(base32.checkByte?.char)
            })
        }
        is Base32.Default -> {
            encodeToByteArray(COMPATIBILITY_DEFAULT)
        }
        is Base32.Hex -> {
            encodeToByteArray(COMPATIBILITY_HEX)
        }
    }
}
