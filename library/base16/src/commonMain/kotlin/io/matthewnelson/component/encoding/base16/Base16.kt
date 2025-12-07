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
@file:Suppress("DEPRECATION_ERROR", "NOTHING_TO_INLINE")

package io.matthewnelson.component.encoding.base16

import io.matthewnelson.encoding.base16.Base16
import io.matthewnelson.encoding.core.Decoder.Companion.decodeToByteArrayOrNull
import io.matthewnelson.encoding.core.Encoder.Companion.encodeToByteArray
import io.matthewnelson.encoding.core.Encoder.Companion.encodeToCharArray
import io.matthewnelson.encoding.core.Encoder.Companion.encodeToString

/**
 * DEPRECATED since `1.2.0`
 * @suppress
 * */
@Deprecated(
    message = "Replaced by EncoderDecoder. Will be removed in future versions.",
    replaceWith = ReplaceWith(
        expression = "this.decodeToByteArrayOrNull(Base16.Builder {})",
        imports = [
            "io.matthewnelson.encoding.base16.Base16",
            "io.matthewnelson.encoding.core.Decoder.Companion.decodeToByteArrayOrNull",
        ],
    ),
    level = DeprecationLevel.HIDDEN,
)
public inline fun String.decodeBase16ToArray(): ByteArray? {
    return decodeToByteArrayOrNull(Base16.Builder {})
}

/**
 * DEPRECATED since `1.2.0`
 * @suppress
 * */
@Deprecated(
    message = "Replaced by EncoderDecoder. Will be removed in future versions.",
    replaceWith = ReplaceWith(
        expression = "this.decodeToByteArrayOrNull(Base16.Builder {})",
        imports = [
            "io.matthewnelson.encoding.base16.Base16",
            "io.matthewnelson.encoding.core.Decoder.Companion.decodeToByteArrayOrNull",
        ],
    ),
    level = DeprecationLevel.HIDDEN,
)
public fun CharArray.decodeBase16ToArray(): ByteArray? {
    return decodeToByteArrayOrNull(Base16.Builder {})
}

/**
 * DEPRECATED since `1.2.0`
 * @suppress
 * */
@Deprecated(
    message = "Replaced by EncoderDecoder. Will be removed in future versions.",
    replaceWith = ReplaceWith(
        expression = "this.encodeToString(Base16.Builder {})",
        imports = [
            "io.matthewnelson.encoding.base16.Base16",
            "io.matthewnelson.encoding.core.Encoder.Companion.encodeToString",
        ],
    ),
    level = DeprecationLevel.HIDDEN,
)
public inline fun ByteArray.encodeBase16(): String {
    return encodeToString(Base16.Builder {})
}

/**
 * DEPRECATED since `1.2.0`
 * @suppress
 * */
@Deprecated(
    message = "Replaced by EncoderDecoder. Will be removed in future versions.",
    replaceWith = ReplaceWith(
        expression = "this.encodeToCharArray(Base16.Builder {})",
        imports = [
            "io.matthewnelson.encoding.base16.Base16",
            "io.matthewnelson.encoding.core.Encoder.Companion.encodeToCharArray",
        ],
    ),
    level = DeprecationLevel.HIDDEN,
)
public inline fun ByteArray.encodeBase16ToCharArray(): CharArray {
    return encodeToCharArray(Base16.Builder {})
}

/**
 * DEPRECATED since `1.2.0`
 * @suppress
 * */
@Deprecated(
    message = "Replaced by EncoderDecoder. Will be removed in future versions.",
    ReplaceWith(
        expression = "this.encodeToByteArray(Base16.Builder {})",
        imports = [
            "io.matthewnelson.encoding.base16.Base16",
            "io.matthewnelson.encoding.core.Encoder.Companion.encodeToByteArray",
        ],
    ),
    level = DeprecationLevel.HIDDEN,
)
public fun ByteArray.encodeBase16ToByteArray(): ByteArray {
    return encodeToByteArray(Base16.Builder {})
}
