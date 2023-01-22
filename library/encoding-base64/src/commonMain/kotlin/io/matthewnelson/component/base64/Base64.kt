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
)

package io.matthewnelson.component.base64

import io.matthewnelson.encoding.builders.Base64
import io.matthewnelson.encoding.core.Decoder.Companion.decodeToByteArrayOrNull
import io.matthewnelson.encoding.core.Encoder.Companion.encodeToByteArray
import io.matthewnelson.encoding.core.Encoder.Companion.encodeToCharArray
import io.matthewnelson.encoding.core.Encoder.Companion.encodeToString
import io.matthewnelson.encoding.core.internal.InternalEncodingApi
import kotlin.jvm.JvmOverloads

@PublishedApi
@InternalEncodingApi
internal val COMPATIBILITY_DEFAULT: io.matthewnelson.encoding.base64.Base64 = Base64 {
    isLenient = true
    encodeToUrlSafe = false
    padEncoded = true
}

/**
 * This is a derivative work from Okio's Base64 implementation which can
 * be found here:
 *
 *     https://github.com/square/okio/blob/master/okio/src/commonMain/kotlin/okio/-Base64.kt
 *
 * @author original: Alexander Y. Kleymenov
 * @suppress
 * */
public sealed class Base64 {

    /**
     * Base64 encoding in accordance with RFC 4648 seciton 4
     * https://www.ietf.org/rfc/rfc4648.html#section-4
     * */
    public object Default: Base64() {
        public const val CHARS: String = io.matthewnelson.encoding.base64.Base64.Default.CHARS
    }

    /**
     * Base64UrlSafe encoding in accordance with RFC 4648 section 5
     * https://www.ietf.org/rfc/rfc4648.html#section-5
     *
     * @param [pad] specify whether or not to add padding character '='
     *  while encoding (true by default).
     * */
    public data class UrlSafe @JvmOverloads constructor(val pad: Boolean = true): Base64() {

        public companion object {
            public const val CHARS: String = io.matthewnelson.encoding.base64.Base64.UrlSafe.CHARS
        }
    }
}

@Suppress("NOTHING_TO_INLINE")
public inline fun String.decodeBase64ToArray(): ByteArray? {
    @OptIn(InternalEncodingApi::class)
    return decodeToByteArrayOrNull(COMPATIBILITY_DEFAULT)
}

public fun CharArray.decodeBase64ToArray(): ByteArray? {
    @OptIn(InternalEncodingApi::class)
    return decodeToByteArrayOrNull(COMPATIBILITY_DEFAULT)
}

@JvmOverloads
@Suppress("NOTHING_TO_INLINE")
public inline fun ByteArray.encodeBase64(base64: Base64 = Base64.Default): String {
    @OptIn(InternalEncodingApi::class)
    return when (base64) {
        is Base64.Default -> {
            encodeToString(COMPATIBILITY_DEFAULT)
        }
        is Base64.UrlSafe -> {
            encodeToString(Base64 {
                isLenient = true
                encodeToUrlSafe = true
                padEncoded = base64.pad
            })
        }
    }
}

@JvmOverloads
@Suppress("NOTHING_TO_INLINE")
public inline fun ByteArray.encodeBase64ToCharArray(base64: Base64 = Base64.Default): CharArray {
    @OptIn(InternalEncodingApi::class)
    return when (base64) {
        is Base64.Default -> {
            encodeToCharArray(COMPATIBILITY_DEFAULT)
        }
        is Base64.UrlSafe -> {
            encodeToCharArray(Base64 {
                isLenient = true
                encodeToUrlSafe = true
                padEncoded = base64.pad
            })
        }
    }
}

@JvmOverloads
public fun ByteArray.encodeBase64ToByteArray(base64: Base64 = Base64.Default): ByteArray {
    @OptIn(InternalEncodingApi::class)
    return when (base64) {
        is Base64.Default -> {
            encodeToByteArray(COMPATIBILITY_DEFAULT)
        }
        is Base64.UrlSafe -> {
            encodeToByteArray(Base64 {
                isLenient = true
                encodeToUrlSafe = true
                padEncoded = base64.pad
            })
        }
    }
}
