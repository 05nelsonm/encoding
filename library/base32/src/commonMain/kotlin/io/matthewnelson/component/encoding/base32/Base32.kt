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
@file:Suppress("DEPRECATION_ERROR", "NOTHING_TO_INLINE", "UNUSED_PARAMETER")

package io.matthewnelson.component.encoding.base32

import io.matthewnelson.encoding.core.Decoder.Companion.decodeToByteArrayOrNull
import io.matthewnelson.encoding.core.Encoder.Companion.encodeToByteArray
import io.matthewnelson.encoding.core.Encoder.Companion.encodeToCharArray
import io.matthewnelson.encoding.core.Encoder.Companion.encodeToString
import kotlin.jvm.JvmOverloads

/**
 * DEPRECATED
 * @suppress
 * */
@Deprecated(
    message = "Replaced by EncoderDecoder. Will be removed in future versions.",
    replaceWith = ReplaceWith(
        expression = "Base32",
        imports = [
            "io.matthewnelson.encoding.base32.Base32",
        ]
    ),
    level = DeprecationLevel.ERROR,
)
public sealed class Base32 {

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
            public const val CHARS: String = io.matthewnelson.encoding.base32.Base32.Crockford.CHARS_UPPER
        }

        inline val hasCheckSymbol: Boolean get() = checkSymbol != null
        inline val checkByte: Byte? get() = checkSymbol?.uppercaseChar()?.code?.toByte()
    }

    public object Default: Base32() {
        public const val CHARS: String = io.matthewnelson.encoding.base32.Base32.Default.CHARS_UPPER
    }

    public object Hex: Base32() {
        public const val CHARS: String = io.matthewnelson.encoding.base32.Base32.Hex.CHARS_UPPER
    }
}

/**
 * DEPRECATED
 * @suppress
 * */
@Deprecated(
    message = "Replaced by EncoderDecoders. Will be removed in future versions.",
    replaceWith = ReplaceWith(
        expression = "this.decodeToByteArrayOrNull(Base32.Default.Builder {})",
        imports = [
            "io.matthewnelson.encoding.base32.Base32",
            "io.matthewnelson.encoding.core.Decoder.Companion.decodeToByteArrayOrNull",
        ],
    ),
    level = DeprecationLevel.HIDDEN,
)
@JvmOverloads
public inline fun String.decodeBase32ToArray(base32: Base32.Default = Base32.Default): ByteArray? {
    return decodeToByteArrayOrNull(io.matthewnelson.encoding.base32.Base32.Default.Builder {})
}

/**
 * DEPRECATED
 * @suppress
 * */
@Deprecated(
    message = "Replaced by EncoderDecoders. Will be removed in future versions.",
    replaceWith = ReplaceWith(
        expression = "this.decodeToByteArrayOrNull(Base32.Hex.Builder {})",
        imports = [
            "io.matthewnelson.encoding.base32.Base32",
            "io.matthewnelson.encoding.core.Decoder.Companion.decodeToByteArrayOrNull",
        ],
    ),
    level = DeprecationLevel.HIDDEN,
)
public inline fun String.decodeBase32ToArray(base32: Base32.Hex): ByteArray? {
    return decodeToByteArrayOrNull(io.matthewnelson.encoding.base32.Base32.Hex.Builder {})
}

/**
 * DEPRECATED
 * @suppress
 * */
@Deprecated(
    message = "Replaced by EncoderDecoders. Will be removed in future versions.",
    replaceWith = ReplaceWith(
        expression = "this.decodeToByteArrayOrNull(Base32.Crockford.Builder { check(base32.checkSymbol) })",
        imports = [
            "io.matthewnelson.encoding.base32.Base32",
            "io.matthewnelson.encoding.core.Decoder.Companion.decodeToByteArrayOrNull",
        ],
    ),
    level = DeprecationLevel.HIDDEN,
)
public inline fun String.decodeBase32ToArray(base32: Base32.Crockford): ByteArray? {
    return decodeToByteArrayOrNull(io.matthewnelson.encoding.base32.Base32.Crockford.Builder { check(base32.checkSymbol) })
}

/**
 * DEPRECATED
 * @suppress
 * */
@Deprecated(
    message = "Replaced by EncoderDecoders. Will be removed in future versions.",
    replaceWith = ReplaceWith(
        expression = "this.decodeToByteArrayOrNull(Base32.Default.Builder {})",
        imports = [
            "io.matthewnelson.encoding.base32.Base32",
            "io.matthewnelson.encoding.core.Decoder.Companion.decodeToByteArrayOrNull",
        ],
    ),
    level = DeprecationLevel.HIDDEN,
)
@JvmOverloads
public fun CharArray.decodeBase32ToArray(base32: Base32.Default = Base32.Default): ByteArray? {
    return decodeToByteArrayOrNull(io.matthewnelson.encoding.base32.Base32.Default.Builder {})
}

/**
 * DEPRECATED
 * @suppress
 * */
@Deprecated(
    message = "Replaced by EncoderDecoders. Will be removed in future versions.",
    replaceWith = ReplaceWith(
        expression = "this.decodeToByteArrayOrNull(Base32.Hex.Builder {})",
        imports = [
            "io.matthewnelson.encoding.base32.Base32",
            "io.matthewnelson.encoding.core.Decoder.Companion.decodeToByteArrayOrNull",
        ],
    ),
    level = DeprecationLevel.HIDDEN,
)
public fun CharArray.decodeBase32ToArray(base32: Base32.Hex): ByteArray? {
    return decodeToByteArrayOrNull(io.matthewnelson.encoding.base32.Base32.Hex.Builder {})
}

/**
 * DEPRECATED
 * @suppress
 * */
@Deprecated(
    message = "Replaced by EncoderDecoders. Will be removed in future versions.",
    replaceWith = ReplaceWith(
        expression = "this.decodeToByteArrayOrNull(Base32.Crockford.Builder { check(base32.checkSymbol) })",
        imports = [
            "io.matthewnelson.encoding.base32.Base32",
            "io.matthewnelson.encoding.core.Decoder.Companion.decodeToByteArrayOrNull",
        ],
    ),
    level = DeprecationLevel.HIDDEN,
)
public fun CharArray.decodeBase32ToArray(base32: Base32.Crockford): ByteArray? {
    return decodeToByteArrayOrNull(io.matthewnelson.encoding.base32.Base32.Crockford.Builder { check(base32.checkSymbol) })
}

/**
 * DEPRECATED
 * @suppress
 * */
@Deprecated(
    message = "Replaced by EncoderDecoders. Will be removed in future versions.",
    replaceWith = ReplaceWith(
        expression = "this.encodeToString(Base32.Default.Builder {})",
        imports = [
            "io.matthewnelson.encoding.base32.Base32",
            "io.matthewnelson.encoding.core.Encoder.Companion.encodeToString",
        ],
    ),
    level = DeprecationLevel.HIDDEN,
)
@JvmOverloads
public inline fun ByteArray.encodeBase32(base32: Base32.Default = Base32.Default): String {
    return encodeToString(io.matthewnelson.encoding.base32.Base32.Default.Builder {})
}

/**
 * DEPRECATED
 * @suppress
 * */
@Deprecated(
    message = "Replaced by EncoderDecoders. Will be removed in future versions.",
    replaceWith = ReplaceWith(
        expression = "this.encodeToString(Base32.Hex.Builder {})",
        imports = [
            "io.matthewnelson.encoding.base32.Base32",
            "io.matthewnelson.encoding.core.Encoder.Companion.encodeToString",
        ],
    ),
    level = DeprecationLevel.HIDDEN,
)
public inline fun ByteArray.encodeBase32(base32: Base32.Hex): String {
    return encodeToString(io.matthewnelson.encoding.base32.Base32.Hex.Builder {})
}

/**
 * DEPRECATED
 * @suppress
 * */
@Deprecated(
    message = "Replaced by EncoderDecoders. Will be removed in future versions.",
    replaceWith = ReplaceWith(
        expression = "this.encodeToString(Base32.Crockford.Builder { check(base32.checkSymbol) })",
        imports = [
            "io.matthewnelson.encoding.base32.Base32",
            "io.matthewnelson.encoding.core.Encoder.Companion.encodeToString",
        ],
    ),
    level = DeprecationLevel.HIDDEN,
)
public inline fun ByteArray.encodeBase32(base32: Base32.Crockford): String {
    return encodeToString(io.matthewnelson.encoding.base32.Base32.Crockford.Builder { check(base32.checkSymbol) })
}

/**
 * DEPRECATED
 * @suppress
 * */
@Deprecated(
    message = "Replaced by EncoderDecoders. Will be removed in future versions.",
    replaceWith = ReplaceWith(
        expression = "this.encodeToCharArray(Base32.Default.Builder {})",
        imports = [
            "io.matthewnelson.encoding.base32.Base32",
            "io.matthewnelson.encoding.core.Encoder.Companion.encodeToCharArray",
        ],
    ),
    level = DeprecationLevel.HIDDEN,
)
@JvmOverloads
public inline fun ByteArray.encodeBase32ToCharArray(base32: Base32.Default = Base32.Default): CharArray {
    return encodeToCharArray(io.matthewnelson.encoding.base32.Base32.Default.Builder {})
}

/**
 * DEPRECATED
 * @suppress
 * */
@Deprecated(
    message = "Replaced by EncoderDecoders. Will be removed in future versions.",
    replaceWith = ReplaceWith(
        expression = "this.encodeToCharArray(Base32.Hex.Builder {})",
        imports = [
            "io.matthewnelson.encoding.base32.Base32",
            "io.matthewnelson.encoding.core.Encoder.Companion.encodeToCharArray",
        ],
    ),
    level = DeprecationLevel.HIDDEN,
)
public inline fun ByteArray.encodeBase32ToCharArray(base32: Base32.Hex): CharArray {
    return encodeToCharArray(io.matthewnelson.encoding.base32.Base32.Hex.Builder {})
}

/**
 * DEPRECATED
 * @suppress
 * */
@Deprecated(
    message = "Replaced by EncoderDecoders. Will be removed in future versions.",
    replaceWith = ReplaceWith(
        expression = "this.encodeToCharArray(Base32.Crockford.Builder { check(base32.checkSymbol) })",
        imports = [
            "io.matthewnelson.encoding.base32.Base32",
            "io.matthewnelson.encoding.core.Encoder.Companion.encodeToCharArray",
        ],
    ),
    level = DeprecationLevel.HIDDEN,
)
public inline fun ByteArray.encodeBase32ToCharArray(base32: Base32.Crockford): CharArray {
    return encodeToCharArray(io.matthewnelson.encoding.base32.Base32.Crockford.Builder { check(base32.checkSymbol) })
}

/**
 * DEPRECATED
 * @suppress
 * */
@Deprecated(
    message = "Replaced by EncoderDecoders. Will be removed in future versions.",
    replaceWith = ReplaceWith(
        expression = "this.encodeToByteArray(Base32.Default.Builder {})",
        imports = [
            "io.matthewnelson.encoding.base32.Base32",
            "io.matthewnelson.encoding.core.Encoder.Companion.encodeToByteArray",
        ],
    ),
    level = DeprecationLevel.HIDDEN,
)
@JvmOverloads
public fun ByteArray.encodeBase32ToByteArray(base32: Base32.Default = Base32.Default): ByteArray {
    return encodeToByteArray(io.matthewnelson.encoding.base32.Base32.Default.Builder {})
}

/**
 * DEPRECATED
 * @suppress
 * */
@Deprecated(
    message = "Replaced by EncoderDecoders. Will be removed in future versions.",
    replaceWith = ReplaceWith(
        expression = "this.encodeToByteArray(Base32.Hex.Builder {})",
        imports = [
            "io.matthewnelson.encoding.base32.Base32",
            "io.matthewnelson.encoding.core.Encoder.Companion.encodeToByteArray",
        ],
    ),
    level = DeprecationLevel.HIDDEN,
)
public fun ByteArray.encodeBase32ToByteArray(base32: Base32.Hex): ByteArray {
    return encodeToByteArray(io.matthewnelson.encoding.base32.Base32.Hex.Builder {})
}

/**
 * DEPRECATED
 * @suppress
 * */
@Deprecated(
    message = "Replaced by EncoderDecoders. Will be removed in future versions.",
    replaceWith = ReplaceWith(
        expression = "this.encodeToByteArray(Base32.Crockford.Builder { check(base32.checkSymbol) })",
        imports = [
            "io.matthewnelson.encoding.base32.Base32",
            "io.matthewnelson.encoding.core.Encoder.Companion.encodeToByteArray",
        ],
    ),
    level = DeprecationLevel.HIDDEN,
)
public fun ByteArray.encodeBase32ToByteArray(base32: Base32.Crockford): ByteArray {
    return encodeToByteArray(io.matthewnelson.encoding.base32.Base32.Crockford.Builder { check(base32.checkSymbol) })
}
