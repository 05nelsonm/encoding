/*
 * Copyright (c) 2023 Matthew Nelson
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
@file:Suppress("SpellCheckingInspection")

package io.matthewnelson.encoding.base16

import io.matthewnelson.encoding.builders.Base16Builder
import io.matthewnelson.encoding.core.*
import io.matthewnelson.encoding.core.internal.EncodingTable
import io.matthewnelson.encoding.core.internal.InternalEncodingApi
import io.matthewnelson.encoding.core.util.DecoderInput
import io.matthewnelson.encoding.core.util.char
import io.matthewnelson.encoding.core.util.isSpaceOrNewLine
import io.matthewnelson.encoding.core.util.lowercaseCharByte
import kotlin.jvm.JvmField

/**
 * Base16 (aka "hex") encoding/decoding in accordance with
 * RFC 4648 section 8.
 * https://www.ietf.org/rfc/rfc4648.html#section-8
 *
 * e.g.
 *
 *     val base16 = Base16 {
 *         isLenient = true
 *         acceptLowercase = true
 *         encodeToLowercase = false
 *     }
 *
 *     val text = "Hello World!"
 *     val bytes = text.encodeToByteArray()
 *     val encoded = bytes.encodeToString(base16)
 *     println(encoded) // 48656C6C6F20576F726C6421
 *     val decoded = encoded.decodeToArray(base16).decodeToString()
 *     assertEquals(text, decoded)
 *
 * @see [Base16Builder]
 * @see [Configuration]
 * @see [CHARS]
 * @see [EncoderDecoder]
 * */
@OptIn(ExperimentalEncodingApi::class, InternalEncodingApi::class)
public class Base16(config: Configuration): EncoderDecoder(config) {

    /**
     * Configuration for [Base16] encoding/decoding.
     *
     * @param [isLenient] See [EncoderDecoder.Configuration]
     * @param [acceptLowercase] If true, will also accept lowercase
     *   characters when decoding (against RFC 4648).
     * @param [encodeToLowercase] If true, will output lowercase
     *   characters instead of uppercase (against RFC 4648).
     * */
    public class Configuration(
        isLenient: Boolean,
        @JvmField
        public val acceptLowercase: Boolean,
        @JvmField
        public val encodeToLowercase: Boolean,
    ): EncoderDecoder.Configuration(isLenient, paddingByte = null) {

        override fun decodeOutMaxSizeOrFail(encodedSize: Int, input: DecoderInput): Int = encodedSize / 2
        override fun encodeOutSize(unencodedSize: Int): Int = unencodedSize * 2

        override fun toStringAddSettings(sb: StringBuilder) {
            with(sb) {
                append("    acceptLowercase: ")
                append(acceptLowercase)
                appendLine()
                append("    encodeToLowercase: ")
                append(encodeToLowercase)
            }
        }
    }

    public companion object {
        public const val CHARS: String = "0123456789ABCDEF"
        private val TABLE = EncodingTable.from(CHARS)
    }

    override fun newEncoderFeed(out: OutFeed): Encoder.Feed {
        return object : Encoder.Feed() {

            override fun updateProtected(input: Byte) {
                val bits = input.toInt() and 0xff
                val b1 = TABLE[bits shr    4]
                val b2 = TABLE[bits and 0x0f]

                if ((config as Configuration).encodeToLowercase) {
                    out.invoke(b1.lowercaseCharByte())
                    out.invoke(b2.lowercaseCharByte())
                } else {
                    out.invoke(b1)
                    out.invoke(b2)
                }
            }

            override fun doFinalProtected() { /* no-op */ }
        }
    }

    override fun newDecoderFeed(out: OutFeed): Decoder.Feed {
        return object : Decoder.Feed() {
            private var count = 0
            private var bitBuffer = 0

            @Throws(EncodingException::class)
            override fun updateProtected(input: Byte) {
                val char = if ((config as Configuration).acceptLowercase) {
                    input.char.uppercaseChar()
                } else {
                    input.char
                }

                if (char.isSpaceOrNewLine()) {
                    if (config.isLenient) {
                        return
                    } else {
                        throw DecoderInput.isLenientFalseEncodingException()
                    }
                }

                val bits: Int = when (char) {
                    in '0'..'9' -> {
                        // char ASCII value
                        // 0     48    0
                        // 9     57    9 (ASCII - 48)
                        char.code - 48
                    }
                    in 'A'..'F' -> {
                        // char ASCII value
                        //   A   65    10
                        //   F   70    15 (ASCII - 55)
                        char.code - 55
                    }
                    else -> {
                        throw EncodingException("Char[$char] is not a valid Base16 character")
                    }
                }

                bitBuffer = bitBuffer shl 4 or bits

                if (++count % 2 == 0) {
                    out.invoke(bitBuffer.toByte())
                    count = 0
                    bitBuffer = 0
                }
            }

            @Throws(EncodingException::class)
            override fun doFinalProtected() {
                // 4*1 = 4 bits. Truncated, fail.
                val i = count % 2
                if (i == 0) return
                throw EncodingException("Truncated input. Count was $i when it should have been 0")
            }
        }
    }

    override fun name(): String = "Base16"
}
