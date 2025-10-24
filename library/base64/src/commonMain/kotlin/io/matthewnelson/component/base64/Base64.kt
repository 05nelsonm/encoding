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
@file:Suppress(
    "KotlinRedundantDiagnosticSuppress",
    "MemberVisibilityCanBePrivate",
    "PrivatePropertyName",
    "RedundantExplicitType",
    "SpellCheckingInspection",
    "DEPRECATION",
    "UNUSED_PARAMETER",
)

package io.matthewnelson.component.base64

import io.matthewnelson.encoding.core.Decoder.Companion.decodeToByteArrayOrNull
import io.matthewnelson.encoding.core.Encoder.Companion.encodeToByteArray
import io.matthewnelson.encoding.core.Encoder.Companion.encodeToCharArray
import io.matthewnelson.encoding.core.Encoder.Companion.encodeToString
import kotlin.jvm.JvmOverloads

/** @suppress */
@Deprecated(
    message = "Replaced by EncoderDecoder. Will be removed in future versions.",
    replaceWith = ReplaceWith(
        expression = "Base64",
        imports = [
            "io.matthewnelson.encoding.base64.Base64",
        ]
    ),
    level = DeprecationLevel.WARNING,
)
public sealed class Base64 {

    @Deprecated(
        message = "Replaced by EncoderDecoders. Will be removed in future versions.",
        replaceWith = ReplaceWith(
            expression = "Default",
            imports = [
                "io.matthewnelson.encoding.base64.Base64.Default",
            ]
        ),
        level = DeprecationLevel.WARNING,
    )
    public object Default: Base64() {
        @Deprecated(
            message = "Replaced by EncoderDecoders. Will be removed in future versions.",
            replaceWith = ReplaceWith(
                expression = "Default.CHARS",
                imports = [
                    "io.matthewnelson.encoding.base64.Base64.Default",
                ]
            ),
            level = DeprecationLevel.WARNING,
        )
        public const val CHARS: String = io.matthewnelson.encoding.base64.Base64.Default.CHARS
    }

    @Deprecated(
        message = "Replaced by EncoderDecoders. Will be removed in future versions.",
        replaceWith = ReplaceWith(
            expression = "Base64.UrlSafe",
            imports = [
                "io.matthewnelson.encoding.base64.Base64",
            ]
        ),
        level = DeprecationLevel.WARNING,
    )
    public data class UrlSafe @JvmOverloads constructor(

        @Deprecated(
            message = "Replaced by EncoderDecoders. Use io.matthewnelson.builders.Base64 { encodeToUrlSafe = true; padEncoded = true/false } to set",
            level = DeprecationLevel.WARNING,
        )
        val pad: Boolean = true
    ): Base64() {

        public companion object {

            @Deprecated(
                message = "Replaced by EncoderDecoders. Will be removed in future versions.",
                replaceWith = ReplaceWith(
                    expression = "UrlSafe.CHARS",
                    imports = [
                        "io.matthewnelson.encoding.base64.Base64.UrlSafe",
                    ]
                ),
                level = DeprecationLevel.WARNING,
            )
            public const val CHARS: String = io.matthewnelson.encoding.base64.Base64.UrlSafe.CHARS
        }
    }
}

@Deprecated(
    message = "Replaced by EncoderDecoders. Will be removed in future versions.",
    replaceWith = ReplaceWith(
        expression = "this.decodeToByteArrayOrNull(Base64.Builder {})",
        imports = [
            "io.matthewnelson.encoding.base64.Base64",
            "io.matthewnelson.encoding.core.Decoder.Companion.decodeToByteArrayOrNull",
        ],
    ),
    level = DeprecationLevel.HIDDEN,
)
@Suppress("NOTHING_TO_INLINE")
public inline fun String.decodeBase64ToArray(): ByteArray? {
    return decodeToByteArrayOrNull(io.matthewnelson.encoding.base64.Base64.Builder {})
}

@Deprecated(
    message = "Replaced by EncoderDecoders. Will be removed in future versions.",
    replaceWith = ReplaceWith(
        expression = "this.decodeToByteArrayOrNull(Base64.Builder {})",
        imports = [
            "io.matthewnelson.encoding.base64.Base64",
            "io.matthewnelson.encoding.core.Decoder.Companion.decodeToByteArrayOrNull",
        ],
    ),
    level = DeprecationLevel.HIDDEN,
)
public fun CharArray.decodeBase64ToArray(): ByteArray? {
    return decodeToByteArrayOrNull(io.matthewnelson.encoding.base64.Base64.Builder {})
}

@Deprecated(
    message = "Replaced by EncoderDecoders. Will be removed in future versions.",
    replaceWith = ReplaceWith(
        expression = "this.encodeToString(Base64.Builder {})",
        imports = [
            "io.matthewnelson.encoding.base64.Base64",
            "io.matthewnelson.encoding.core.Encoder.Companion.encodeToString",
        ],
    ),
    level = DeprecationLevel.HIDDEN,
)
@JvmOverloads
@Suppress("NOTHING_TO_INLINE")
public inline fun ByteArray.encodeBase64(base64: Base64.Default = Base64.Default): String {
    return encodeToString(io.matthewnelson.encoding.base64.Base64.Builder {})
}

@Deprecated(
    message = "Replaced by EncoderDecoders. Will be removed in future versions.",
    replaceWith = ReplaceWith(
        expression = "this.encodeToString(Base64.Builder { encodeUrlSafe(true).padEncoded(true/false) })",
        imports = [
            "io.matthewnelson.encoding.base64.Base64",
            "io.matthewnelson.encoding.core.Encoder.Companion.encodeToString",
        ],
    ),
    level = DeprecationLevel.HIDDEN,
)
@Suppress("NOTHING_TO_INLINE")
public inline fun ByteArray.encodeBase64(base64: Base64.UrlSafe): String {
    return encodeToString(io.matthewnelson.encoding.base64.Base64.Builder {
        encodeUrlSafe(true)
        padEncoded(base64.pad)
    })
}

@Deprecated(
    message = "Replaced by EncoderDecoders. Will be removed in future versions.",
    replaceWith = ReplaceWith(
        expression = "this.encodeToCharArray(Base64.Builder {})",
        imports = [
            "io.matthewnelson.encoding.base64.Base64",
            "io.matthewnelson.encoding.core.Encoder.Companion.encodeToCharArray",
        ],
    ),
    level = DeprecationLevel.HIDDEN,
)
@JvmOverloads
@Suppress("NOTHING_TO_INLINE")
public inline fun ByteArray.encodeBase64ToCharArray(base64: Base64.Default = Base64.Default): CharArray {
    return encodeToCharArray(io.matthewnelson.encoding.base64.Base64.Builder {})
}

@Deprecated(
    message = "Replaced by EncoderDecoders. Will be removed in future versions.",
    replaceWith = ReplaceWith(
        expression = "this.encodeToCharArray(Base64 { encodeToUrlSafe = true; padEncoded = true/false })",
        imports = [
            "io.matthewnelson.encoding.base64.Base64",
            "io.matthewnelson.encoding.core.Encoder.Companion.encodeToCharArray",
        ],
    ),
    level = DeprecationLevel.HIDDEN,
)
@Suppress("NOTHING_TO_INLINE")
public inline fun ByteArray.encodeBase64ToCharArray(base64: Base64.UrlSafe): CharArray {
    return encodeToCharArray(io.matthewnelson.encoding.base64.Base64.Builder {
        encodeUrlSafe(true)
        padEncoded(base64.pad)
    })
}

@Deprecated(
    message = "Replaced by EncoderDecoders. Will be removed in future versions.",
    replaceWith = ReplaceWith(
        expression = "this.encodeToByteArray(Base64.Builder {})",
        imports = [
            "io.matthewnelson.encoding.base64.Base64",
            "io.matthewnelson.encoding.core.Encoder.Companion.encodeToByteArray",
        ],
    ),
    level = DeprecationLevel.HIDDEN,
)
@JvmOverloads
public fun ByteArray.encodeBase64ToByteArray(base64: Base64.Default = Base64.Default): ByteArray {
    @Suppress("DEPRECATION_ERROR")
    return encodeToByteArray(io.matthewnelson.encoding.base64.Base64.Builder {})
}

@Deprecated(
    message = "Replaced by EncoderDecoders. Will be removed in future versions.",
    replaceWith = ReplaceWith(
        expression = "this.encodeToByteArray(Base64 { encodeToUrlSafe = true; padEncoded = true/false })",
        imports = [
            "io.matthewnelson.encoding.base64.Base64",
            "io.matthewnelson.encoding.core.Encoder.Companion.encodeToByteArray",
        ],
    ),
    level = DeprecationLevel.HIDDEN,
)
public fun ByteArray.encodeBase64ToByteArray(base64: Base64.UrlSafe): ByteArray {
    @Suppress("DEPRECATION_ERROR")
    return encodeToByteArray(io.matthewnelson.encoding.base64.Base64.Builder {
        encodeUrlSafe(true)
        padEncoded(base64.pad)
    })
}
