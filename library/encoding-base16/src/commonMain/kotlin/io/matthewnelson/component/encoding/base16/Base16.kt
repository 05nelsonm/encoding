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
@file:Suppress("KotlinRedundantDiagnosticSuppress")

package io.matthewnelson.component.encoding.base16

import io.matthewnelson.encoding.base16.Base16
import io.matthewnelson.encoding.builders.Base16
import io.matthewnelson.encoding.core.Decoder.Companion.decodeToByteArrayOrNull
import io.matthewnelson.encoding.core.Encoder.Companion.encodeToByteArray
import io.matthewnelson.encoding.core.Encoder.Companion.encodeToCharArray
import io.matthewnelson.encoding.core.Encoder.Companion.encodeToString
import io.matthewnelson.encoding.core.internal.InternalEncodingApi

@PublishedApi
@InternalEncodingApi
internal val COMPATIBILITY: Base16 = Base16 {
    isLenient = true
    acceptLowercase = false
    encodeToLowercase = false
}

@Suppress("NOTHING_TO_INLINE")
public inline fun String.decodeBase16ToArray(): ByteArray? {
    @OptIn(InternalEncodingApi::class)
    return decodeToByteArrayOrNull(COMPATIBILITY)
}

public fun CharArray.decodeBase16ToArray(): ByteArray? {
    @OptIn(InternalEncodingApi::class)
    return decodeToByteArrayOrNull(COMPATIBILITY)
}

@Suppress("NOTHING_TO_INLINE")
public inline fun ByteArray.encodeBase16(): String {
    @OptIn(InternalEncodingApi::class)
    return encodeToString(COMPATIBILITY)
}

@Suppress("NOTHING_TO_INLINE")
public inline fun ByteArray.encodeBase16ToCharArray(): CharArray {
    @OptIn(InternalEncodingApi::class)
    return encodeToCharArray(COMPATIBILITY)
}

public fun ByteArray.encodeBase16ToByteArray(): ByteArray {
    @OptIn(InternalEncodingApi::class)
    return encodeToByteArray(COMPATIBILITY)
}
