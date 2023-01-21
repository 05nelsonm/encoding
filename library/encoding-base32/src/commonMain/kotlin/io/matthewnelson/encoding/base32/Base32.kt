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
import io.matthewnelson.encoding.core.internal.BitBuffer
import io.matthewnelson.encoding.core.internal.EncodingTable
import io.matthewnelson.encoding.core.internal.InternalEncodingApi
import io.matthewnelson.encoding.core.util.*
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
     *         encodeToLowercase = false
     *         hyphenInterval = 5
     *         checkByte(checkSymbol = '~')
     *     }
     *
     *     val text = "Hello World!"
     *     val bytes = text.encodeToByteArray()
     *     val encoded = bytes.encodeToString(base32Crockford)
     *     println(encoded) // 91JPR-V3F41-BPYWK-CCGGG~
     *     val decoded = encoded.decodeToByteArray(base32Crockford).decodeToString()
     *     assertEquals(text, decoded)
     *
     * @see [Base32Crockford]
     * @see [Crockford.Config]
     * @see [Crockford.CHARS]
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
            public val encodeToLowercase: Boolean,
            @JvmField
            public val hyphenInterval: Byte,
            @JvmField
            public val checkByte: Byte?,
        ): EncoderDecoder.Config(isLenient, paddingByte = null) {

            @get:JvmName("checkSymbol")
            public val checkSymbol: Char? get() = checkByte?.char

            @Throws(EncodingException::class)
            override fun decodeOutMaxSizeOrFailProtected(encodedSize: Long, input: DecoderInput?): Long {
                var outSize = encodedSize

                val checkSymbol = checkSymbol?.uppercaseChar()

                if (input != null && checkSymbol != null) {
                    // Always uppercase it b/c little u and U should
                    // be decoded the same for Crockford.
                    val actual = input[input.lastRelevantCharacter - 1].uppercaseChar()

                    if (actual != checkSymbol) {
                        throw EncodingException(
                            "checkSymbol[$actual] for encoded did not match expected[${checkSymbol}]"
                        )
                    } else {
                        outSize--
                    }
                }

                return decodeOutMaxSize(outSize)
            }

            override fun encodeOutSizeProtected(unEncodedSize: Long): Long {
                var outSize = encodedOutSize(unEncodedSize, willBePadded = false)

                if (hyphenInterval > 0) {
                    val hyphenCount: Float = (outSize.toFloat() / hyphenInterval) - 1F

                    if (hyphenCount > 0F) {
                        // Count rounded down
                        outSize += hyphenCount.toLong()

                        // If there was a remainder, manually add it
                        if (hyphenCount.rem(1) > 0F) {
                            outSize++
                        }
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

                private val buffer = DecodingBuffer(out)
                private var isCheckByteSet = false

                @Throws(EncodingException::class)
                override fun updateProtected(input: Byte) {
                    // Crockford requires that decoding accept both
                    // uppercase and lowercase. So, uppercase
                    // everything that comes in.
                    val char = input.char.uppercaseChar()

                    if (isCheckByteSet) {
                        val symbol = (config as Crockford.Config).checkSymbol
                        // If the set checkByte was not intended, it's only a valid
                        // as the very last character and the previous update call
                        // was invalid.
                        throw EncodingException("checkSymbol[$symbol] was set, but decoding is still being attempted.")
                    }

                    val bits: Long = when (char) {
                        in '0'..'9' -> {
                            // char ASCII value
                            //  0    48    0
                            //  9    57    9 (ASCII - 48)
                            char.code - 48L
                        }
                        in 'A'..'H' -> {
                            // char ASCII value
                            //  A    65    10
                            //  H    72    17 (ASCII - 55)
                            char.code - 55L
                        }
                        'I', 'L' -> {
                            // Crockford treats characters 'I', 'i', 'L' and 'l' as 1

                            // char ASCII value
                            //  1    49    1 (ASCII - 48)
                            '1'.code - 48L
                        }
                        'J', 'K' -> {
                            // char ASCII value
                            //  J    74    18
                            //  K    75    19 (ASCII - 56)
                            char.code - 56L
                        }
                        'M', 'N' -> {
                            // char ASCII value
                            //  M    77    20
                            //  N    78    21 (ASCII - 57)
                            char.code - 57L
                        }
                        'O' -> {
                            // Crockford treats characters 'O' and 'o' as 0

                            // char ASCII value
                            //  0    48    0 (ASCII - 48)
                            '0'.code - 48L
                        }
                        in 'P'..'T' -> {
                            // char ASCII value
                            //  P    80    22
                            //  T    84    26 (ASCII - 58)
                            char.code - 58L
                        }
                        in 'V'..'Z' -> {
                            // char ASCII value
                            //  V    86    27
                            //  Z    90    31 (ASCII - 59)
                            char.code - 59L
                        }
                        // We don't care about little u b/c
                        // everything is being uppercased.
                        '*', '~', '$', '=', 'U'/*, 'u'*/ -> {
                            when (val checkSymbol = (config as Crockford.Config).checkByte?.char?.uppercaseChar()) {
                                char -> {
                                    isCheckByteSet = true
                                    return
                                }
                                else -> {
                                    throw EncodingException(
                                        "Char[${input.char}] IS a valid checkSymbol, but did not match what is configured[$checkSymbol]"
                                    )
                                }
                            }
                        }
                        '-' -> {
                            // Crockford allows for insertion of hyphens,
                            // which are to be ignored when decoding.
                            return
                        }
                        else -> {
                            throw EncodingException("Char[${input.char}] is not a valid Base32 Crockford character")
                        }
                    }

                    buffer.update(bits)
                }

                @Throws(EncodingException::class)
                override fun doFinalProtected() {
                    buffer.finalize()
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
     *     val decoded = encoded.decodeToByteArray(base32Default).decodeToString()
     *     assertEquals(text, decoded)
     *
     * @see [Base32Default]
     * @see [Default.Config]
     * @see [Default.CHARS]
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

            override fun decodeOutMaxSizeOrFailProtected(encodedSize: Long, input: DecoderInput?): Long {
                return decodeOutMaxSize(encodedSize)
            }

            override fun encodeOutSizeProtected(unEncodedSize: Long): Long {
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

                private val buffer = DecodingBuffer(out)

                @Throws(EncodingException::class)
                override fun updateProtected(input: Byte) {
                    val char = if ((config as Default.Config).acceptLowercase) {
                        input.char.uppercaseChar()
                    } else {
                        input.char
                    }

                    val bits: Long = when (char) {
                        in '2'..'7' -> {
                            // char ASCII value
                            //  2    50    26
                            //  7    55    31 (ASCII - 24)
                            char.code - 24L
                        }
                        in 'A'..'Z' -> {
                            // char ASCII value
                            //  A    65    0
                            //  Z    90    25 (ASCII - 65)
                            char.code - 65L
                        }
                        else -> {
                            throw EncodingException("Char[${input.char}] is not a valid Base32 Default character")
                        }
                    }

                    buffer.update(bits)
                }

                @Throws(EncodingException::class)
                override fun doFinalProtected() {
                    buffer.finalize()
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
     *     val decoded = encoded.decodeToByteArray(base32Hex).decodeToString()
     *     assertEquals(text, decoded)
     *
     * @see [Base32Hex]
     * @see [Hex.Config]
     * @see [Hex.CHARS]
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

            override fun decodeOutMaxSizeOrFailProtected(encodedSize: Long, input: DecoderInput?): Long {
                return decodeOutMaxSize(encodedSize)
            }

            override fun encodeOutSizeProtected(unEncodedSize: Long): Long {
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

                private val buffer = DecodingBuffer(out)

                @Throws(EncodingException::class)
                override fun updateProtected(input: Byte) {
                    val char = if ((config as Hex.Config).acceptLowercase) {
                        input.char.uppercaseChar()
                    } else {
                        input.char
                    }

                    val bits: Long = when (char) {
                        in '0'..'9' -> {
                            // char ASCII value
                            //  0    48    0
                            //  9    57    9 (ASCII - 48)
                            char.code - 48L
                        }
                        in 'A'..'V' -> {
                            // char ASCII value
                            //  A    65    10
                            //  V    86    31 (ASCII - 55)
                            char.code - 55L
                        }
                        else -> {
                            throw EncodingException("Char[${input.char}] is not a valid Base32 Hex character")
                        }
                    }

                    buffer.update(bits)
                }

                @Throws(EncodingException::class)
                override fun doFinalProtected() {
                    buffer.finalize()
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
        private fun decodeOutMaxSize(encodedSize: Long): Long = (encodedSize * 5L / 8L)

        @JvmStatic
        private fun encodedOutSize(
            unEncodedSize: Long,
            willBePadded: Boolean,
        ): Long {
            var outSize: Long = ((unEncodedSize + 4L) / 5L) * 8L
            if (willBePadded) return outSize

            when (unEncodedSize - (unEncodedSize - unEncodedSize % 5)) {
                0L -> { /* no-op */ }
                1L -> outSize -= 6L
                2L -> outSize -= 4L
                3L -> outSize -= 3L
                4L -> outSize -= 1L
            }

            return outSize
        }
    }

    private inner class DecodingBuffer(out: OutFeed): BitBuffer<Long>(
        blockSize = 8,
        update = { buffer, bits ->
            // Append the char's 5 bits to the buffer
            buffer shl 5 or bits
        },
        flush = { buffer ->
            // For every 8 chars of input, we accumulate
            // 40 bits of output data. Emit 5 bytes.
            out.invoke((buffer shr 32).toByte())
            out.invoke((buffer shr 24).toByte())
            out.invoke((buffer shr 16).toByte())
            out.invoke((buffer shr  8).toByte())
            out.invoke((buffer       ).toByte())
        },
        finalize = { count, blockSize, buf ->
            var buffer = buf

            when (count % blockSize) {
                0 -> {}
                1, 3, 6 -> {
                    // 5*1 =  5 bits. Truncated, fail.
                    // 5*3 = 15 bits. Truncated, fail.
                    // 5*6 = 30 bits. Truncated, fail.
                    throw truncatedInputEncodingException(count)
                }
                2 -> {
                    // 5*2 = 10 bits. Drop 2
                    buffer =    buffer shr  2

                    // 8/8 = 1 byte
                    out.invoke((buffer       ).toByte())
                }
                4 -> {
                    // 5*4 = 20 bits. Drop 4
                    buffer =    buffer shr  4

                    // 16/8 = 2 bytes
                    out.invoke((buffer shr  8).toByte())
                    out.invoke((buffer       ).toByte())
                }
                5 -> {
                    // 5*5 = 25 bits. Drop 1
                    buffer =    buffer shr  1

                    // 24/8 = 3 bytes
                    out.invoke((buffer shr 16).toByte())
                    out.invoke((buffer shr  8).toByte())
                    out.invoke((buffer       ).toByte())
                }
                7 -> {
                    // 5*7 = 35 bits. Drop 3
                    buffer =    buffer shr  3

                    // 32/8 = 4 bytes
                    out.invoke((buffer shr 24).toByte())
                    out.invoke((buffer shr 16).toByte())
                    out.invoke((buffer shr  8).toByte())
                    out.invoke((buffer       ).toByte())
                }
            }
        }
    ) {
        override var bitBuffer: Long = 0L
        override fun reset() { bitBuffer = 0L }
    }
}
