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

import io.matthewnelson.encoding.builders.*
import io.matthewnelson.encoding.core.Encoder.Companion.encodeToString

fun main() {
    val base16EncoderDecoder = Base16 {
        isLenient = false
        lineBreakInterval = 64
        encodeToLowercase = true
    }

    val base32CrockfordEncoderDecoder = Base32Crockford {
        isLenient = false
        encodeToLowercase = false
        hyphenInterval = 5
        checkSymbol('*')
    }

    val base32DefaultEncoderDecoder = Base32Default {
        isLenient = false
        lineBreakInterval = 64
        encodeToLowercase = true
        padEncoded = false
    }

    val base32HexEncoderDecoder = Base32Hex {
        isLenient = false
        lineBreakInterval = 64
        encodeToLowercase = true
        padEncoded = false
    }

    val base64DefaultEncoderDecoder = Base64 {
        isLenient = true
        lineBreakInterval = 64
        encodeToUrlSafe = false
        padEncoded = true
    }

    val base64UrlSafeEncoderDecoder = Base64 {
        isLenient = false
        lineBreakInterval = 64
        encodeToUrlSafe = true
        padEncoded = false
    }

    val bytes = "Hello World!".encodeToByteArray()

    val base16 = bytes.encodeToString(base16EncoderDecoder)

    val crockford = bytes.encodeToString(base32CrockfordEncoderDecoder)
    val default = bytes.encodeToString(base32DefaultEncoderDecoder)
    val hex = bytes.encodeToString(base32HexEncoderDecoder)

    val base64 = bytes.encodeToString(base64DefaultEncoderDecoder)
    val base64UrlSafe = bytes.encodeToString(base64UrlSafeEncoderDecoder)

    println("Hello World Encodes to:")
    println("    Base16 (hex): $base16")

    println("    Base32 Crockford[checkSymbol = *, hyphenInterval = 5]: $crockford")
    println("    Base32 Default: $default")
    println("    Base32 Hex: $hex")

    println("    Base64 Default: $base64")
    println("    Base64 UrlSafe: $base64UrlSafe")
}
