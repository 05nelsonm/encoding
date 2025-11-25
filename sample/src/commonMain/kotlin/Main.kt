/*
 * Copyright (c) 2023 Matthew Nelson
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 **/

import io.matthewnelson.encoding.base16.Base16
import io.matthewnelson.encoding.base32.Base32
import io.matthewnelson.encoding.base64.Base64
import io.matthewnelson.encoding.core.Decoder.Companion.decodeToByteArray
import io.matthewnelson.encoding.core.Encoder.Companion.encodeToString
import io.matthewnelson.encoding.utf8.UTF8

fun main() {
    val bytes = "Hello World!".decodeToByteArray(UTF8)

    val base16 = bytes.encodeToString(Base16)

    val crockford = bytes.encodeToString(Base32.Crockford)
    val default = bytes.encodeToString(Base32.Default)
    val hex = bytes.encodeToString(Base32.Hex)

    val base64 = bytes.encodeToString(Base64.Default)
    val base64UrlSafe = bytes.encodeToString(Base64.UrlSafe)

    println("'Hello World!' Encodes to:")
    println("    Base16 (hex): $base16")

    println("    Base32 Crockford[hyphenInterval = 4]: $crockford")
    println("    Base32 Default: $default")
    println("    Base32 Hex: $hex")

    println("    Base64 Default: $base64")
    println("    Base64 UrlSafe: $base64UrlSafe")

    println("    UTF-8 (bytes): ${bytes.toList()}")
}
