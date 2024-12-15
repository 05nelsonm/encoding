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
    "DEPRECATION",
    "DeprecatedCallableAddReplaceWith",
    "UNUSED_PARAMETER",
)

package io.matthewnelson.component.encoding.base32

import io.matthewnelson.encoding.base32.Base32Crockford
import io.matthewnelson.encoding.base32.Base32Default
import io.matthewnelson.encoding.base32.Base32Hex
import io.matthewnelson.encoding.core.Decoder.Companion.decodeToByteArrayOrNull
import io.matthewnelson.encoding.core.Encoder.Companion.encodeToByteArray
import io.matthewnelson.encoding.core.Encoder.Companion.encodeToCharArray
import io.matthewnelson.encoding.core.Encoder.Companion.encodeToString
import kotlin.jvm.JvmOverloads

/** @suppress */
@Deprecated(
    message = "Replaced by EncoderDecoder. Will be removed in future versions.",
    replaceWith = ReplaceWith(
        expression = "Base32",
        imports = [
            "io.matthewnelson.encoding.base32.Base32",
        ]
    ),
    level = DeprecationLevel.WARNING,
)
public sealed class Base32 {

    @Deprecated(
        message = "Replaced by EncoderDecoders. Will be removed in future versions.",
        replaceWith = ReplaceWith(
            expression = "Crockford",
            imports = [
                "io.matthewnelson.encoding.base32.Base32.Crockford",
            ]
        ),
        level = DeprecationLevel.WARNING,
    )
    public data class Crockford @JvmOverloads constructor(
        @Deprecated(
            message = "Replaced by EncoderDecoder. Use io.matthewnelson.base32.Base32Crockford to set",
            level = DeprecationLevel.WARNING
        )
        val checkSymbol: Char? = null
    ): Base32() {

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

            @Deprecated(
                message = "Replaced by EncoderDecoder. Will be removed in future versions.",
                replaceWith = ReplaceWith(
                    expression = "Crockford.CHARS_UPPER",
                    imports = [
                        "io.matthewnelson.encoding.base32.Base32.Crockford"
                    ],
                ),
                level = DeprecationLevel.WARNING
            )
            public const val CHARS: String = io.matthewnelson.encoding.base32.Base32.Crockford.CHARS_UPPER
        }

        @Deprecated(
            message = "Replaced by EncoderDecoder. Use io.matthewnelson.base32.Base32Crockford to set",
            level = DeprecationLevel.WARNING
        )
        inline val hasCheckSymbol: Boolean get() = checkSymbol != null

        @Deprecated(
            message = "Replaced by EncoderDecoder. Use io.matthewnelson.base32.Base32Crockford to set",
            level = DeprecationLevel.WARNING
        )
        inline val checkByte: Byte? get() = checkSymbol?.uppercaseChar()?.code?.toByte()
    }

    @Deprecated(
        message = "Replaced by EncoderDecoders. Will be removed in future versions.",
        replaceWith = ReplaceWith(
            expression = "Default",
            imports = [
                "io.matthewnelson.encoding.base32.Base32.Default"
            ]
        ),
        level = DeprecationLevel.WARNING,
    )
    public object Default: Base32() {

        @Deprecated(
            message = "Replaced by EncoderDecoder. Will be removed in future versions.",
            replaceWith = ReplaceWith(
                expression = "Default.CHARS_UPPER",
                imports = [
                    "io.matthewnelson.encoding.base32.Base32.Default"
                ],
            ),
            level = DeprecationLevel.WARNING
        )
        public const val CHARS: String = io.matthewnelson.encoding.base32.Base32.Default.CHARS_UPPER
    }

    @Deprecated(
        message = "Replaced by EncoderDecoders. Will be removed in future versions.",
        replaceWith = ReplaceWith(
            expression = "Hex",
            imports = [
                "io.matthewnelson.encoding.base32.Base32.Hex"
            ]
        ),
        level = DeprecationLevel.WARNING,
    )
    public object Hex: Base32() {

        @Deprecated(
            message = "Replaced by EncoderDecoder. Will be removed in future versions.",
            replaceWith = ReplaceWith(
                expression = "Hex.CHARS_UPPER",
                imports = [
                    "io.matthewnelson.encoding.base32.Base32.Hex"
                ],
            ),
            level = DeprecationLevel.WARNING
        )
        public const val CHARS: String = io.matthewnelson.encoding.base32.Base32.Hex.CHARS_UPPER
    }
}

@Deprecated(
    message = "Replaced by EncoderDecoders. Will be removed in future versions.",
    replaceWith = ReplaceWith(
        expression = "this.decodeToByteArrayOrNull(Base32Default())",
        imports = [
            "io.matthewnelson.encoding.base32.Base32Default",
            "io.matthewnelson.encoding.core.Decoder.Companion.decodeToByteArrayOrNull",
        ],
    ),
    level = DeprecationLevel.HIDDEN,
)
@JvmOverloads
@Suppress("NOTHING_TO_INLINE")
public inline fun String.decodeBase32ToArray(base32: Base32.Default = Base32.Default): ByteArray? {
    return decodeToByteArrayOrNull(Base32Default())
}

@Deprecated(
    message = "Replaced by EncoderDecoders. Will be removed in future versions.",
    replaceWith = ReplaceWith(
        expression = "this.decodeToByteArrayOrNull(Base32Hex())",
        imports = [
            "io.matthewnelson.encoding.base32.Base32Hex",
            "io.matthewnelson.encoding.core.Decoder.Companion.decodeToByteArrayOrNull",
        ],
    ),
    level = DeprecationLevel.HIDDEN,
)
@Suppress("NOTHING_TO_INLINE")
public inline fun String.decodeBase32ToArray(base32: Base32.Hex): ByteArray? {
    return decodeToByteArrayOrNull(Base32Hex())
}

@Deprecated(
    message = "Replaced by EncoderDecoders. Will be removed in future versions.",
    replaceWith = ReplaceWith(
        expression = "this.decodeToByteArrayOrNull(Base32Crockford { checkSymbol('value') })",
        imports = [
            "io.matthewnelson.encoding.base32.Base32Crockford",
            "io.matthewnelson.encoding.core.Decoder.Companion.decodeToByteArrayOrNull",
        ],
    ),
    level = DeprecationLevel.HIDDEN,
)
@Suppress("NOTHING_TO_INLINE")
public inline fun String.decodeBase32ToArray(base32: Base32.Crockford): ByteArray? {
    return decodeToByteArrayOrNull(Base32Crockford { checkSymbol(base32.checkSymbol) })
}

@Deprecated(
    message = "Replaced by EncoderDecoders. Will be removed in future versions.",
    replaceWith = ReplaceWith(
        expression = "this.decodeToByteArrayOrNull(Base32Default())",
        imports = [
            "io.matthewnelson.encoding.base32.Base32Default",
            "io.matthewnelson.encoding.core.Decoder.Companion.decodeToByteArrayOrNull",
        ],
    ),
    level = DeprecationLevel.HIDDEN,
)
@JvmOverloads
public fun CharArray.decodeBase32ToArray(base32: Base32.Default = Base32.Default): ByteArray? {
    return decodeToByteArrayOrNull(Base32Default())
}

@Deprecated(
    message = "Replaced by EncoderDecoders. Will be removed in future versions.",
    replaceWith = ReplaceWith(
        expression = "this.decodeToByteArrayOrNull(Base32Hex())",
        imports = [
            "io.matthewnelson.encoding.base32.Base32Hex",
            "io.matthewnelson.encoding.core.Decoder.Companion.decodeToByteArrayOrNull",
        ],
    ),
    level = DeprecationLevel.HIDDEN,
)
public fun CharArray.decodeBase32ToArray(base32: Base32.Hex): ByteArray? {
    return decodeToByteArrayOrNull(Base32Hex())
}

@Deprecated(
    message = "Replaced by EncoderDecoders. Will be removed in future versions.",
    replaceWith = ReplaceWith(
        expression = "this.decodeToByteArrayOrNull(Base32Crockford { checkSymbol('value') })",
        imports = [
            "io.matthewnelson.encoding.base32.Base32Crockford",
            "io.matthewnelson.encoding.core.Decoder.Companion.decodeToByteArrayOrNull",
        ],
    ),
    level = DeprecationLevel.HIDDEN,
)
public fun CharArray.decodeBase32ToArray(base32: Base32.Crockford): ByteArray? {
    return decodeToByteArrayOrNull(Base32Crockford { checkSymbol(base32.checkSymbol) })
}

@Deprecated(
    message = "Replaced by EncoderDecoders. Will be removed in future versions.",
    replaceWith = ReplaceWith(
        expression = "this.encodeToString(Base32Default())",
        imports = [
            "io.matthewnelson.encoding.base32.Base32Default",
            "io.matthewnelson.encoding.core.Encoder.Companion.encodeToString",
        ],
    ),
    level = DeprecationLevel.HIDDEN,
)
@JvmOverloads
@Suppress("NOTHING_TO_INLINE")
public inline fun ByteArray.encodeBase32(base32: Base32.Default = Base32.Default): String {
    return encodeToString(Base32Default())
}

@Deprecated(
    message = "Replaced by EncoderDecoders. Will be removed in future versions.",
    replaceWith = ReplaceWith(
        expression = "this.encodeToString(Base32Hex())",
        imports = [
            "io.matthewnelson.encoding.base32.Base32Hex",
            "io.matthewnelson.encoding.core.Encoder.Companion.encodeToString",
        ],
    ),
    level = DeprecationLevel.HIDDEN,
)
@Suppress("NOTHING_TO_INLINE")
public inline fun ByteArray.encodeBase32(base32: Base32.Hex): String {
    return encodeToString(Base32Hex())
}

@Deprecated(
    message = "Replaced by EncoderDecoders. Will be removed in future versions.",
    replaceWith = ReplaceWith(
        expression = "this.encodeToString(Base32Crockford { checkSymbol('value') })",
        imports = [
            "io.matthewnelson.encoding.base32.Base32Crockford",
            "io.matthewnelson.encoding.core.Encoder.Companion.encodeToString",
        ],
    ),
    level = DeprecationLevel.HIDDEN,
)
@Suppress("NOTHING_TO_INLINE")
public inline fun ByteArray.encodeBase32(base32: Base32.Crockford): String {
    return encodeToString(Base32Crockford { checkSymbol(base32.checkSymbol) })
}

@Deprecated(
    message = "Replaced by EncoderDecoders. Will be removed in future versions.",
    replaceWith = ReplaceWith(
        expression = "this.encodeToCharArray(Base32Default())",
        imports = [
            "io.matthewnelson.encoding.base32.Base32Default",
            "io.matthewnelson.encoding.core.Encoder.Companion.encodeToCharArray",
        ],
    ),
    level = DeprecationLevel.HIDDEN,
)
@JvmOverloads
@Suppress("NOTHING_TO_INLINE")
public inline fun ByteArray.encodeBase32ToCharArray(base32: Base32.Default = Base32.Default): CharArray {
    return encodeToCharArray(Base32Default())
}

@Deprecated(
    message = "Replaced by EncoderDecoders. Will be removed in future versions.",
    replaceWith = ReplaceWith(
        expression = "this.encodeToCharArray(Base32Hex())",
        imports = [
            "io.matthewnelson.encoding.base32.Base32Hex",
            "io.matthewnelson.encoding.core.Encoder.Companion.encodeToCharArray",
        ],
    ),
    level = DeprecationLevel.HIDDEN,
)
@Suppress("NOTHING_TO_INLINE")
public inline fun ByteArray.encodeBase32ToCharArray(base32: Base32.Hex): CharArray {
    return encodeToCharArray(Base32Hex())
}

@Deprecated(
    message = "Replaced by EncoderDecoders. Will be removed in future versions.",
    replaceWith = ReplaceWith(
        expression = "this.encodeToCharArray(Base32Crockford { checkSymbol('value') })",
        imports = [
            "io.matthewnelson.encoding.base32.Base32Crockford",
            "io.matthewnelson.encoding.core.Encoder.Companion.encodeToCharArray",
        ],
    ),
    level = DeprecationLevel.HIDDEN,
)
@Suppress("NOTHING_TO_INLINE")
public inline fun ByteArray.encodeBase32ToCharArray(base32: Base32.Crockford): CharArray {
    return encodeToCharArray(Base32Crockford { checkSymbol(base32.checkSymbol) })
}

@Deprecated(
    message = "Replaced by EncoderDecoders. Will be removed in future versions.",
    replaceWith = ReplaceWith(
        expression = "this.encodeToByteArray(Base32Default())",
        imports = [
            "io.matthewnelson.encoding.base32.Base32Default",
            "io.matthewnelson.encoding.core.Encoder.Companion.encodeToByteArray",
        ],
    ),
    level = DeprecationLevel.HIDDEN,
)
@JvmOverloads
public fun ByteArray.encodeBase32ToByteArray(base32: Base32.Default = Base32.Default): ByteArray {
    return encodeToByteArray(Base32Default())
}

@Deprecated(
    message = "Replaced by EncoderDecoders. Will be removed in future versions.",
    replaceWith = ReplaceWith(
        expression = "this.encodeToByteArray(Base32Hex())",
        imports = [
            "io.matthewnelson.encoding.base32.Base32Hex",
            "io.matthewnelson.encoding.core.Encoder.Companion.encodeToByteArray",
        ],
    ),
    level = DeprecationLevel.HIDDEN,
)
public fun ByteArray.encodeBase32ToByteArray(base32: Base32.Hex): ByteArray {
    return encodeToByteArray(Base32Hex())
}

@Deprecated(
    message = "Replaced by EncoderDecoders. Will be removed in future versions.",
    replaceWith = ReplaceWith(
        expression = "this.encodeToByteArray(Base32Crockford { checkSymbol('value') })",
        imports = [
            "io.matthewnelson.encoding.base32.Base32Crockford",
            "io.matthewnelson.encoding.core.Encoder.Companion.encodeToByteArray",
        ],
    ),
    level = DeprecationLevel.HIDDEN,
)
public fun ByteArray.encodeBase32ToByteArray(base32: Base32.Crockford): ByteArray {
    return encodeToByteArray(Base32Crockford { checkSymbol(base32.checkSymbol) })
}
