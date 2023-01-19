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

import io.matthewnelson.encoding.builders.Base16
import io.matthewnelson.encoding.core.*
import io.matthewnelson.encoding.core.internal.EncodingTable
import io.matthewnelson.encoding.core.internal.InternalEncodingApi
import io.matthewnelson.encoding.core.util.DecoderInput
import kotlin.jvm.JvmField
import kotlin.jvm.JvmStatic

/**
 * Base16 (aka "hex") encoding/decoding in accordance with
 * RFC 4648 section 8.
 * https://www.ietf.org/rfc/rfc4648.html#section-8
 *
 * e.g.
 *
 *     val base16 = Base16 {
 *         isLenient = true
 *         decodeLowercase = true
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
 * @see [default]
 * @see [strict]
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
     * @param [decodeLowercase] If true, will also accept lowercase
     *   characters when decoding (against RFC 4648).
     * @param [encodeToLowercase] If true, will output lowercase
     *   characters instead of uppercase (against RFC 4648).
     * */
    public class Configuration(
        isLenient: Boolean,
        @JvmField
        public val decodeLowercase: Boolean,
        @JvmField
        public val encodeToLowercase: Boolean,
    ): EncoderDecoder.Configuration(isLenient, paddingChar = null) {

        override fun decodeOutMaxSizeOrFail(encodedSize: Int, input: DecoderInput): Int = encodedSize / 2
        override fun encodeOutSize(unencodedSize: Int): Int = unencodedSize * 2

        override fun toStringAddSettings(sb: StringBuilder) {
            with(sb) {
                append("    decodeLowercase: ")
                append(decodeLowercase)
                appendLine()
                append("    encodeToLowercase: ")
                append(encodeToLowercase)
            }
        }
    }

    public companion object {
        public const val CHARS: String = "0123456789ABCDEF"

        private val DEFAULT = Base16(Configuration(
            isLenient = true,
            decodeLowercase = true,
            encodeToLowercase = true,
        ))

        private val STRICT = Base16(Configuration(
            isLenient = false,
            decodeLowercase = false,
            encodeToLowercase = false,
        ))

        /**
         * A default configuration for Base16 encoding/decoding where:
         *  - [Configuration.isLenient] = true
         *  - [Configuration.decodeLowercase] = true
         *  - [Configuration.encodeToLowercase] = true
         *
         * ENCODING: Non-compliant with RFC 4648 section 8
         * DECODING: Non-compliant with RFC 4648 section 8
         * */
        @JvmStatic
        public fun default(): Base16 = DEFAULT

        /**
         * A strict configuration for Base16 encoding/decoding where:
         *  - [Configuration.isLenient] = false
         *  - [Configuration.decodeLowercase] = false
         *  - [Configuration.encodeToLowercase] = false
         *
         * ENCODING: Compliant with RFC 4648 section 8
         * DECODING: Compliant with RFC 4648 section 8
         * */
        @JvmStatic
        public fun strict(): Base16 = STRICT

        private val TABLE = EncodingTable.from(CHARS)
    }

    override fun newEncoderFeed(out: OutFeed): Encoder.Feed {
        return object : Encoder.Feed() {

            override fun updateProtected(input: Byte) {
                val bits = input.toInt() and 0xff
                val b1 = TABLE.get(bits shr    4)
                val b2 = TABLE.get(bits and 0x0f)

                if ((config as Configuration).encodeToLowercase) {
                    out.invoke(b1.lowercaseByte())
                    out.invoke(b2.lowercaseByte())
                } else {
                    out.invoke(b1.byte)
                    out.invoke(b2.byte)
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
            override fun updateProtected(input: Char) {
                val char = if ((config as Configuration).decodeLowercase) {
                    input.uppercaseChar()
                } else {
                    input
                }

                val bits: Int
                when (char) {
                    in '0'..'9' -> {
                        // char ASCII value
                        // 0     48    0
                        // 9     57    9 (ASCII - 48)
                        bits = char.code - 48
                    }
                    in 'A'..'F' -> {
                        // char ASCII value
                        //   A   65    10
                        //   F   70    15 (ASCII - 55)
                        bits = char.code - 55
                    }
                    '\n', '\r', ' ', '\t' -> {
                        if (config.isLenient) {
                            return
                        } else {
                            throw DecoderInput.isLenientFalseEncodingException()
                        }
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
