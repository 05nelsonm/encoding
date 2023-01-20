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
@file:Suppress("MemberVisibilityCanBePrivate", "RemoveRedundantQualifierName", "SpellCheckingInspection")

package io.matthewnelson.encoding.base32

import io.matthewnelson.encoding.builders.*
import io.matthewnelson.encoding.core.*
import io.matthewnelson.encoding.core.internal.EncodingTable
import io.matthewnelson.encoding.core.internal.InternalEncodingApi
import io.matthewnelson.encoding.core.util.DecoderInput
import io.matthewnelson.encoding.core.util.byte
import io.matthewnelson.encoding.core.util.char
import kotlin.jvm.JvmField
import kotlin.jvm.JvmName
import kotlin.jvm.JvmStatic
import kotlin.jvm.JvmSynthetic

/**
 * Base32 encoding/decoding.
 *
 * @see [Crockford]
 * @see [Default]
 * @see [Hex]
 * */
@OptIn(ExperimentalEncodingApi::class, InternalEncodingApi::class)
public sealed class Base32(config: EncoderDecoder.Config): EncoderDecoder(config) {

    /**
     * Base32 Crockford encoding/decoding in accordance with
     * https://www.crockford.com/base32.html
     *
     * e.g.
     *
     *     val base32Crockford = Base32Crockford {
     *         isLenient = true
     *         acceptLowercase = true
     *         encodeToLowercase = false
     *         hyphenInterval = 5
     *         checkByte(checkSymbol = '~')
     *     }
     *
     *     val text = "Hello World!"
     *     val bytes = text.encodeToByteArray()
     *     val encoded = bytes.encodeToString(base32Crockford)
     *     println(encoded) // 91JPR-V3F41-BPYWK-CCGGG~
     *     val decoded = encoded.decodeToArray(base32Crockford).decodeToString()
     *     assertEquals(text, decoded)
     *
     * @see [Base32Crockford]
     * @see [Crockford.Config]
     * @see [CHARS]
     * @see [EncoderDecoder]
     * */
    public class Crockford(config: Crockford.Config): Base32(config) {

        /**
         * Configuration for [Base32.Crockford] encoding/decoding.
         *
         * Use [Base32CrockfordConfigBuilder] to create.
         *
         * @see [Base32CrockfordConfigBuilder]
         * @see [EncoderDecoder.Config]
         * */
        public class Config private constructor(
            isLenient: Boolean,
            @JvmField
            public val acceptLowercase: Boolean,
            @JvmField
            public val encodeToLowercase: Boolean,
            @JvmField
            public val hyphenInterval: Short,
            @JvmField
            public val checkByte: Byte?,
        ): EncoderDecoder.Config(isLenient, paddingByte = null) {

            @get:JvmName("checkSymbol")
            public val checkSymbol: Char? get() = checkByte?.char

            @Throws(EncodingException::class)
            override fun decodeOutMaxSizeOrFail(encodedSize: Int, input: DecoderInput?): Int {
                var outSize = encodedSize

                if (input != null && checkByte != null) {
                    // Check last character
                    val actual = input[encodedSize - 1]
                    if (actual != checkByte.char) {
                        throw EncodingException(
                            "checkSymbol[$actual] for encoded did not match expected[${checkByte.char}]"
                        )
                    } else {
                        outSize--
                    }
                }

                return decodeOutMaxSize(outSize)
            }

            override fun encodeOutSizeProtected(unEncodedSize: Int): Int {
                var outSize = encodedOutSize(unEncodedSize, willBePadded = false)

                if (hyphenInterval > 0) {
                    // TODO: This is still off because encodedOutSize returns Int
                    //  will need to fix (Issue #48)
                    val hyphenCount = (outSize / hyphenInterval) - 1

                    if (hyphenCount > 0) {
                        outSize += hyphenCount
                    }
                }

                // checkByte will be appended if present
                if (checkByte != null) {
                    outSize++
                }

                return outSize
            }

            override fun toStringAddSettings(sb: StringBuilder) {
                with(sb) {
                    append("    acceptLowercase: ")
                    append(acceptLowercase)
                    appendLine()
                    append("    encodeToLowercase: ")
                    append(encodeToLowercase)
                    appendLine()
                    append("    hyphenInterval: ")
                    append(hyphenInterval)
                    appendLine()
                    append("    checkSymbol: ")
                    append(checkByte?.char)
                }
            }

            internal companion object {

                @JvmSynthetic
                internal fun from(builder: Base32CrockfordConfigBuilder): Config {
                    return Config(
                        isLenient = builder.isLenient,
                        acceptLowercase = builder.acceptLowercase,
                        encodeToLowercase = builder.encodeToLowercase,
                        hyphenInterval = builder.hyphenInterval,
                        checkByte = builder.checkByte
                    )
                }
            }
        }

        public companion object {

            /**
             * Base32 Crockford encoding characters.
             * */
            public const val CHARS: String = "0123456789ABCDEFGHJKMNPQRSTVWXYZ"
            private val TABLE = EncodingTable.from(CHARS)
        }

        override fun newDecoderFeed(out: OutFeed): Decoder.Feed {
            return object : Decoder.Feed() {

                @Throws(EncodingException::class)
                override fun updateProtected(input: Byte) {
                    // TODO
                    throw EncodingException("Not yet implemented")
                }

                @Throws(EncodingException::class)
                override fun doFinalProtected() {
                    // TODO
                    throw EncodingException("Not yet implemented")
                }

            }
        }

        override fun newEncoderFeed(out: OutFeed): Encoder.Feed {
            return object : Encoder.Feed() {

                override fun updateProtected(input: Byte) {
                    // TODO
                }

                override fun doFinalProtected() {
                    // TODO
                }
            }
        }

        override fun name(): String = "Base32.Crockford"
    }

    /**
     * Base 32 Default encoding/decoding in accordance with
     * RFC 4648 section 6.
     *
     * https://www.ietf.org/rfc/rfc4648.html#section-6
     *
     * e.g.
     *
     *     val base32Default = Base32Default {
     *         isLenient = true
     *         acceptLowercase = true
     *         encodeToLowercase = false
     *         padEncoded = true
     *     }
     *
     *     val text = "Hello World!"
     *     val bytes = text.encodeToByteArray()
     *     val encoded = bytes.encodeToString(base32Default)
     *     println(encoded) // JBSWY3DPEBLW64TMMQQQ====
     *     val decoded = encoded.decodeToArray(base32Default).decodeToString()
     *     assertEquals(text, decoded)
     *
     * @see [Base32Default]
     * @see [Default.Config]
     * @see [CHARS]
     * @see [EncoderDecoder]
     * */
    public class Default(config: Default.Config): Base32(config) {

        /**
         * Configuration for [Base32.Default] encoding/decoding.
         *
         * Use [Base32DefaultConfigBuilder] to create.
         *
         * @see [Base32DefaultConfigBuilder]
         * @see [EncoderDecoder.Config]
         * */
        public class Config private constructor(
            isLenient: Boolean,
            @JvmField
            public val acceptLowercase: Boolean,
            @JvmField
            public val encodeToLowercase: Boolean,
            @JvmField
            public val padEncoded: Boolean,
        ): EncoderDecoder.Config(isLenient, paddingByte = '='.byte) {

            override fun decodeOutMaxSizeOrFail(encodedSize: Int, input: DecoderInput?): Int {
                return decodeOutMaxSize(encodedSize)
            }

            override fun encodeOutSizeProtected(unEncodedSize: Int): Int {
                return encodedOutSize(unEncodedSize, padEncoded)
            }

            override fun toStringAddSettings(sb: StringBuilder) {
                with(sb) {
                    append("    acceptLowercase: ")
                    append(acceptLowercase)
                    appendLine()
                    append("    encodeToLowercase: ")
                    append(encodeToLowercase)
                    appendLine()
                    append("    padEncoded: ")
                    append(padEncoded)
                }
            }

            internal companion object {

                @JvmSynthetic
                internal fun from(builder: Base32DefaultConfigBuilder): Config {
                    return Config(
                        isLenient = builder.isLenient,
                        acceptLowercase = builder.acceptLowercase,
                        encodeToLowercase = builder.encodeToLowercase,
                        padEncoded = builder.padEncoded,
                    )
                }
            }
        }

        public companion object {

            /**
             * Base32 Default encoding characters.
             * */
            public const val CHARS: String = "ABCDEFGHIJKLMNOPQRSTUVWXYZ234567"
            private val TABLE = EncodingTable.from(CHARS)
        }

        override fun newDecoderFeed(out: OutFeed): Decoder.Feed {
            return object : Decoder.Feed() {

                @Throws(EncodingException::class)
                override fun updateProtected(input: Byte) {
                    // TODO
                    throw EncodingException("Not yet implemented")
                }

                @Throws(EncodingException::class)
                override fun doFinalProtected() {
                    // TODO
                    throw EncodingException("Not yet implemented")
                }
            }
        }

        override fun newEncoderFeed(out: OutFeed): Encoder.Feed {
            return object : Encoder.Feed() {

                override fun updateProtected(input: Byte) {
                    // TODO
                }

                override fun doFinalProtected() {
                    // TODO
                }
            }
        }

        override fun name(): String = "Base32.Default"
    }

    /**
     * Base 32 Default encoding/decoding in accordance with
     * RFC 4648 section 7.
     *
     * https://www.ietf.org/rfc/rfc4648.html#section-7
     *
     * e.g.
     *
     *     val base32Hex = Base32Hex {
     *         isLenient = true
     *         acceptLowercase = true
     *         encodeToLowercase = false
     *         padEncoded = true
     *     }
     *
     *     val text = "Hello World!"
     *     val bytes = text.encodeToByteArray()
     *     val encoded = bytes.encodeToString(base32Hex)
     *     println(encoded) // 91IMOR3F41BMUSJCCGGG====
     *     val decoded = encoded.decodeToArray(base32Hex).decodeToString()
     *     assertEquals(text, decoded)
     *
     * @see [Base32Hex]
     * @see [Hex.Config]
     * @see [CHARS]
     * @see [EncoderDecoder]
     * */
    public class Hex(config: Hex.Config): Base32(config) {

        /**
         * Configuration for [Base32.Hex] encoding/decoding.
         *
         * Use [Base32HexConfigBuilder] to create.
         *
         * @see [Base32HexConfigBuilder]
         * @see [EncoderDecoder.Config]
         * */
        public class Config private constructor(
            isLenient: Boolean,
            @JvmField
            public val acceptLowercase: Boolean,
            @JvmField
            public val encodeToLowercase: Boolean,
            @JvmField
            public val padEncoded: Boolean,
        ): EncoderDecoder.Config(isLenient, paddingByte = '='.byte) {

            override fun decodeOutMaxSizeOrFail(encodedSize: Int, input: DecoderInput?): Int {
                return decodeOutMaxSize(encodedSize)
            }

            override fun encodeOutSizeProtected(unEncodedSize: Int): Int {
                return encodedOutSize(unEncodedSize, padEncoded)
            }

            override fun toStringAddSettings(sb: StringBuilder) {
                with(sb) {
                    append("    acceptLowercase: ")
                    append(acceptLowercase)
                    appendLine()
                    append("    encodeToLowercase: ")
                    append(encodeToLowercase)
                    appendLine()
                    append("    padEncoded: ")
                    append(padEncoded)
                }
            }

            internal companion object {

                @JvmSynthetic
                internal fun from(builder: Base32HexConfigBuilder): Config {
                    return Config(
                        isLenient = builder.isLenient,
                        acceptLowercase = builder.acceptLowercase,
                        encodeToLowercase = builder.encodeToLowercase,
                        padEncoded = builder.padEncoded,
                    )
                }
            }
        }

        public companion object {

            /**
             * Base32 Hex encoding characters.
             * */
            public const val CHARS: String = "0123456789ABCDEFGHIJKLMNOPQRSTUV"
            private val TABLE = EncodingTable.from(CHARS)
        }

        override fun newDecoderFeed(out: OutFeed): Decoder.Feed {
            return object : Decoder.Feed() {

                @Throws(EncodingException::class)
                override fun updateProtected(input: Byte) {
                    // TODO
                    throw EncodingException("Not yet implemented")
                }

                @Throws(EncodingException::class)
                override fun doFinalProtected() {
                    // TODO
                    throw EncodingException("Not yet implemented")
                }
            }
        }

        override fun newEncoderFeed(out: OutFeed): Encoder.Feed {
            return object : Encoder.Feed() {

                override fun updateProtected(input: Byte) {
                    // TODO
                }

                override fun doFinalProtected() {
                    // TODO
                }
            }
        }

        override fun name(): String = "Base32.Hex"
    }

    private companion object {

        @JvmStatic
        private fun decodeOutMaxSize(encodedSize: Int): Int = (encodedSize * 5L / 8L).toInt()

        @JvmStatic
        private fun encodedOutSize(
            unEncodedSize: Int,
            willBePadded: Boolean,
        ): Int {
            var outSize = (unEncodedSize + 4) / 5 * 8
            if (willBePadded) return outSize

            when (unEncodedSize - (unEncodedSize - unEncodedSize % 5)) {
                0 -> { /* no-op */ }
                1 -> outSize -= 6
                2 -> outSize -= 4
                3 -> outSize -= 3
                4 -> outSize -= 1
            }

            return outSize
        }
    }
}
