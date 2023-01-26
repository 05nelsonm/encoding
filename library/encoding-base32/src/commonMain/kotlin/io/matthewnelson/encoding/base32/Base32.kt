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
@file:Suppress("RemoveRedundantQualifierName", "SpellCheckingInspection")

package io.matthewnelson.encoding.base32

import io.matthewnelson.encoding.base32.internal.decodeOutMaxSize
import io.matthewnelson.encoding.base32.internal.encodeOutSize
import io.matthewnelson.encoding.base32.internal.isCheckSymbol
import io.matthewnelson.encoding.builders.*
import io.matthewnelson.encoding.core.*
import io.matthewnelson.encoding.core.util.*
import io.matthewnelson.encoding.core.util.FeedBuffer
import kotlin.jvm.JvmField
import kotlin.jvm.JvmSynthetic

/**
 * Base32 encoding/decoding.
 *
 * @see [Crockford]
 * @see [Default]
 * @see [Hex]
 * @see [Decoder.decodeToByteArray]
 * @see [Decoder.decodeToByteArrayOrNull]
 * @see [Encoder.encodeToString]
 * @see [Encoder.encodeToCharArray]
 * @see [Encoder.encodeToByteArray]
 * */
@OptIn(ExperimentalEncodingApi::class)
public sealed class Base32<C: EncoderDecoder.Config>(config: C): EncoderDecoder<C>(config) {

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
     *         checkSymbol(symbol = '~')
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
     * @see [Crockford.CHARS_UPPER]
     * @see [Crockford.CHARS_LOWER]
     * @see [EncoderDecoder]
     * */
    public class Crockford(config: Crockford.Config): Base32<Crockford.Config>(config) {

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
            public val checkSymbol: Char?,
        ): EncoderDecoder.Config(
            isLenient = isLenient,
            lineBreakInterval = 0,
            paddingChar = null,
        ) {

            protected override fun decodeOutMaxSizeProtected(encodedSize: Long): Long {
                return encodedSize.decodeOutMaxSize()
            }

            @Throws(EncodingException::class)
            protected override fun decodeOutMaxSizeOrFailProtected(encodedSize: Int, input: DecoderInput): Int {
                var outSize = encodedSize

                val actual = input[encodedSize - 1]

                if (checkSymbol != null) {
                    // Uppercase them so that little 'u' is always
                    // compared as big 'U'.
                    val expectedUpper = checkSymbol.uppercaseChar()
                    val actualUpper = actual.uppercaseChar()

                    if (actualUpper != expectedUpper) {
                        // Must have a matching checkSymbol

                        if (actual.isCheckSymbol()) {
                            throw EncodingException(
                                "Check symbol did not match. Expected[$expectedUpper], Actual[$actual]"
                            )
                        } else {
                            throw EncodingException(
                                "Check symbol not found. Expected[$expectedUpper]"
                            )
                        }
                    } else {
                        outSize--
                    }
                } else {
                    // Mine as well check it here before actually
                    // decoding because otherwise it will fail on
                    // the very last byte.
                    if (actual.isCheckSymbol()) {
                        throw EncodingException(
                            "Decoder Misconfiguration.\n" +
                            "Encoded data had Checksymbol[$actual], but the " +
                            "decoder is configured to reject."
                        )
                    }
                }

                return outSize.toLong().decodeOutMaxSize().toInt()
            }

            protected override fun encodeOutSizeProtected(unEncodedSize: Long): Long {
                var outSize = unEncodedSize.encodeOutSize(willBePadded = false)

                // checkByte will be appended if present
                if (checkSymbol != null) {
                    outSize++
                }

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

                return outSize
            }

            protected override fun toStringAddSettings(): Set<Setting> {
                return buildSet {
                    add(Setting(name = "encodeToLowercase", value = encodeToLowercase))
                    add(Setting(name = "hyphenInterval", value = hyphenInterval))
                    add(Setting(name = "checkSymbol", value = checkSymbol))
                }
            }

            internal companion object {

                @JvmSynthetic
                internal fun from(builder: Base32CrockfordConfigBuilder): Config {
                    return Config(
                        isLenient = builder.isLenient,
                        encodeToLowercase = builder.encodeToLowercase,
                        hyphenInterval = if (builder.hyphenInterval > 0) builder.hyphenInterval else 0,
                        checkSymbol = builder.checkSymbol
                    )
                }
            }
        }

        public companion object {

            /**
             * Uppercase Base32 Crockford encoding characters.
             * */
            public const val CHARS_UPPER: String = "0123456789ABCDEFGHJKMNPQRSTVWXYZ"

            /**
             * Lowercase Base32 Crockford encoding characters.
             * */
            public const val CHARS_LOWER: String = "0123456789abcdefghjkmnpqrstvwxyz"
        }

        protected override fun newDecoderFeedProtected(out: Decoder.OutFeed): Decoder<Crockford.Config>.Feed {
            return object : Decoder<Crockford.Config>.Feed() {

                private val buffer = DecodingBuffer(out)
                private var isCheckSymbolSet = false

                @Throws(EncodingException::class)
                override fun consumeProtected(input: Char) {
                    if (isCheckSymbolSet) {
                        // If the set checkByte was not intended, it's only a valid
                        // as the very last character and the previous update call
                        // was invalid.
                        throw EncodingException(
                            "Checksymbol[${config.checkSymbol}] was set, but decoding is still being attempted."
                        )
                    }

                    // Crockford requires that decoding accept both
                    // uppercase and lowercase. So, uppercase
                    // everything that comes in.
                    val bits: Int = when (input) {
                        in '0'..'9' -> {
                            // char ASCII value
                            //  0    48    0
                            //  9    57    9 (ASCII - 48)
                            input.code - 48
                        }
                        in 'a'..'h' -> {
                            // char ASCII value
                            //  A    65    10
                            //  H    72    17 (ASCII - 55)
                            input.uppercaseChar().code - 55
                        }
                        in 'A'..'H' -> {
                            // char ASCII value
                            //  A    65    10
                            //  H    72    17 (ASCII - 55)
                            input.code - 55
                        }
                        'I', 'i', 'L', 'l' -> {
                            // Crockford treats characters 'I', 'i', 'L' and 'l' as 1

                            // char ASCII value
                            //  1    49    1 (ASCII - 48)
                            '1'.code - 48
                        }
                        'j', 'k' -> {
                            // char ASCII value
                            //  J    74    18
                            //  K    75    19 (ASCII - 56)
                            input.uppercaseChar().code - 56
                        }
                        'J', 'K' -> {
                            // char ASCII value
                            //  J    74    18
                            //  K    75    19 (ASCII - 56)
                            input.code - 56
                        }
                        'm', 'n' -> {
                            // char ASCII value
                            //  M    77    20
                            //  N    78    21 (ASCII - 57)
                            input.uppercaseChar().code - 57
                        }
                        'M', 'N' -> {
                            // char ASCII value
                            //  M    77    20
                            //  N    78    21 (ASCII - 57)
                            input.code - 57
                        }
                        'O', 'o' -> {
                            // Crockford treats characters 'O' and 'o' as 0

                            // char ASCII value
                            //  0    48    0 (ASCII - 48)
                            '0'.code - 48
                        }
                        in 'p'..'t' -> {
                            // char ASCII value
                            //  P    80    22
                            //  T    84    26 (ASCII - 58)
                            input.uppercaseChar().code - 58
                        }
                        in 'P'..'T' -> {
                            // char ASCII value
                            //  P    80    22
                            //  T    84    26 (ASCII - 58)
                            input.code - 58
                        }
                        in 'v'..'z' -> {
                            // char ASCII value
                            //  V    86    27
                            //  Z    90    31 (ASCII - 59)
                            input.uppercaseChar().code - 59
                        }
                        in 'V'..'Z' -> {
                            // char ASCII value
                            //  V    86    27
                            //  Z    90    31 (ASCII - 59)
                            input.code - 59
                        }
                        '-' -> {
                            // Crockford allows for insertion of hyphens,
                            // which are to be ignored when decoding.
                            return
                        }
                        else -> {
                            if (input.isCheckSymbol()) {
                                when (val checkSymbol = config.checkSymbol?.uppercaseChar()) {
                                    input.uppercaseChar() -> {
                                        isCheckSymbolSet = true
                                        return
                                    }
                                    else -> {
                                        throw EncodingException(
                                            "Char[${input}] IS a checkSymbol, but did not match config's Checksymbol[$checkSymbol]"
                                        )
                                    }
                                }
                            }

                            throw EncodingException("Char[${input}] is not a valid Base32 Crockford character")
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

        protected override fun newEncoderFeedProtected(out: Encoder.OutFeed): Encoder<Crockford.Config>.Feed {
            return object : Encoder<Crockford.Config>.Feed() {

                private var outCount: Byte = 0
                private var outputHyphenOnNext = false

                private val buffer = EncodingBuffer(
                    out = { byte ->
                        if (outputHyphenOnNext) {
                            out.output('-')
                            outCount = 0
                            outputHyphenOnNext = false
                        }

                        out.output(byte)
                        outputHyphenOnNext = config.hyphenInterval > 0 && ++outCount == config.hyphenInterval
                    },
                    table = if (config.encodeToLowercase) {
                        CHARS_LOWER
                    } else {
                        CHARS_UPPER
                    },
                    paddingChar = null,
                )

                override fun consumeProtected(input: Byte) {
                    buffer.update(input.toInt())
                }

                override fun doFinalProtected() {
                    buffer.finalize()

                    config.checkSymbol?.let { symbol ->

                        if (outputHyphenOnNext) {
                            out.output('-')
                        }

                        if (config.encodeToLowercase) {
                            out.output(symbol.lowercaseChar())
                        } else {
                            out.output(symbol.uppercaseChar())
                        }
                    }
                }
            }
        }

        protected override fun name(): String = "Base32.Crockford"
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
     *         lineBreakInterval = 64
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
     * @see [Default.CHARS_UPPER]
     * @see [Default.CHARS_LOWER]
     * @see [EncoderDecoder]
     * */
    public class Default(config: Default.Config): Base32<Default.Config>(config) {

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
            lineBreakInterval: Byte,
            @JvmField
            public val encodeToLowercase: Boolean,
            @JvmField
            public val padEncoded: Boolean,
        ): EncoderDecoder.Config(
            isLenient = isLenient,
            lineBreakInterval = lineBreakInterval,
            paddingChar = '=',
        ) {

            protected override fun decodeOutMaxSizeProtected(encodedSize: Long): Long {
                return encodedSize.decodeOutMaxSize()
            }

            protected override fun decodeOutMaxSizeOrFailProtected(encodedSize: Int, input: DecoderInput): Int {
                return encodedSize.toLong().decodeOutMaxSize().toInt()
            }

            protected override fun encodeOutSizeProtected(unEncodedSize: Long): Long {
                return unEncodedSize.encodeOutSize(willBePadded = padEncoded)
            }

            protected override fun toStringAddSettings(): Set<Setting> {
                return buildSet {
                    add(Setting(name = "encodeToLowercase", value = encodeToLowercase))
                    add(Setting(name = "padEncoded", value = padEncoded))
                }
            }

            internal companion object {

                @JvmSynthetic
                internal fun from(builder: Base32DefaultConfigBuilder): Config {
                    return Config(
                        isLenient = builder.isLenient,
                        lineBreakInterval = builder.lineBreakInterval,
                        encodeToLowercase = builder.encodeToLowercase,
                        padEncoded = builder.padEncoded,
                    )
                }
            }
        }

        public companion object {

            /**
             * Uppercase Base32 Default encoding characters.
             * */
            public const val CHARS_UPPER: String = "ABCDEFGHIJKLMNOPQRSTUVWXYZ234567"

            /**
             * Lowercase Base32 Default encoding characters.
             * */
            public const val CHARS_LOWER: String = "abcdefghijklmnopqrstuvwxyz234567"
        }

        protected override fun newDecoderFeedProtected(out: Decoder.OutFeed): Decoder<Default.Config>.Feed {
            return object : Decoder<Default.Config>.Feed() {

                private val buffer = DecodingBuffer(out)

                @Throws(EncodingException::class)
                override fun consumeProtected(input: Char) {
                    val bits: Int = when (input) {
                        in '2'..'7' -> {
                            // char ASCII value
                            //  2    50    26
                            //  7    55    31 (ASCII - 24)
                            input.code - 24
                        }
                        in 'a'..'z' -> {
                            // char ASCII value
                            //  A    65    0
                            //  Z    90    25 (ASCII - 65)
                            input.uppercaseChar().code - 65
                        }
                        in 'A'..'Z' -> {
                            // char ASCII value
                            //  A    65    0
                            //  Z    90    25 (ASCII - 65)
                            input.code - 65
                        }
                        else -> {
                            throw EncodingException("Char[${input}] is not a valid Base32 Default character")
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

        protected override fun newEncoderFeedProtected(out: OutFeed): Encoder<Default.Config>.Feed {
            return object : Encoder<Default.Config>.Feed() {

                private val buffer = EncodingBuffer(
                    out = out,
                    table = if (config.encodeToLowercase) {
                        CHARS_LOWER
                    } else {
                        CHARS_UPPER
                    },
                    paddingChar = if (config.padEncoded) {
                        config.paddingChar
                    } else {
                        null
                    },
                )

                override fun consumeProtected(input: Byte) {
                    buffer.update(input.toInt())
                }

                override fun doFinalProtected() {
                    buffer.finalize()
                }
            }
        }

        protected override fun name(): String = "Base32.Default"
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
     *         lineBreakInterval = 64
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
     * @see [Hex.CHARS_UPPER]
     * @see [Hex.CHARS_LOWER]
     * @see [EncoderDecoder]
     * */
    public class Hex(config: Hex.Config): Base32<Hex.Config>(config) {

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
            lineBreakInterval: Byte,
            @JvmField
            public val encodeToLowercase: Boolean,
            @JvmField
            public val padEncoded: Boolean,
        ): EncoderDecoder.Config(
            isLenient = isLenient,
            lineBreakInterval = lineBreakInterval,
            paddingChar = '=',
        ) {

            protected override fun decodeOutMaxSizeProtected(encodedSize: Long): Long {
                return encodedSize.decodeOutMaxSize()
            }

            protected override fun decodeOutMaxSizeOrFailProtected(encodedSize: Int, input: DecoderInput): Int {
                return encodedSize.toLong().decodeOutMaxSize().toInt()
            }

            protected override fun encodeOutSizeProtected(unEncodedSize: Long): Long {
                return unEncodedSize.encodeOutSize(willBePadded = padEncoded)
            }

            protected override fun toStringAddSettings(): Set<Setting> {
                return buildSet {
                    add(Setting(name = "encodeToLowercase", value = encodeToLowercase))
                    add(Setting(name = "padEncoded", value = padEncoded))
                }
            }

            internal companion object {

                @JvmSynthetic
                internal fun from(builder: Base32HexConfigBuilder): Config {
                    return Config(
                        isLenient = builder.isLenient,
                        lineBreakInterval = builder.lineBreakInterval,
                        encodeToLowercase = builder.encodeToLowercase,
                        padEncoded = builder.padEncoded,
                    )
                }
            }
        }

        public companion object {

            /**
             * Uppercase Base32 Hex encoding characters.
             * */
            public const val CHARS_UPPER: String = "0123456789ABCDEFGHIJKLMNOPQRSTUV"

            /**
             * Lowercase Base32 Hex encoding characters.
             * */
            public const val CHARS_LOWER: String = "0123456789abcdefghijklmnopqrstuv"
        }

        protected override fun newDecoderFeedProtected(out: Decoder.OutFeed): Decoder<Hex.Config>.Feed {
            return object : Decoder<Hex.Config>.Feed() {

                private val buffer = DecodingBuffer(out)

                @Throws(EncodingException::class)
                override fun consumeProtected(input: Char) {
                    val bits: Int = when (input) {
                        in '0'..'9' -> {
                            // char ASCII value
                            //  0    48    0
                            //  9    57    9 (ASCII - 48)
                            input.code - 48
                        }
                        in 'a'..'v' -> {
                            // char ASCII value
                            //  A    65    10
                            //  V    86    31 (ASCII - 55)
                            input.uppercaseChar().code - 55
                        }
                        in 'A'..'V' -> {
                            // char ASCII value
                            //  A    65    10
                            //  V    86    31 (ASCII - 55)
                            input.code - 55
                        }
                        else -> {
                            throw EncodingException("Char[${input}] is not a valid Base32 Hex character")
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

        protected override fun newEncoderFeedProtected(out: OutFeed): Encoder<Hex.Config>.Feed {
            return object : Encoder<Hex.Config>.Feed() {

                private val buffer = EncodingBuffer(
                    out = out,
                    table = if (config.encodeToLowercase) {
                        CHARS_LOWER
                    } else {
                        CHARS_UPPER
                    },
                    paddingChar = if (config.padEncoded) {
                        config.paddingChar
                    } else {
                        null
                    },
                )

                override fun consumeProtected(input: Byte) {
                    buffer.update(input.toInt())
                }

                override fun doFinalProtected() {
                    buffer.finalize()
                }
            }
        }

        protected override fun name(): String = "Base32.Hex"
    }

    private inner class EncodingBuffer(
        out: Encoder.OutFeed,
        table: CharSequence,
        paddingChar: Char?,
    ): FeedBuffer(
        blockSize = 5,
        flush = { buffer ->
            var bitBuffer = 0L

            // Append each char's 8 bits to the bitBuffer
            for (bits in buffer) {
                bitBuffer =  (bitBuffer shl  8) + bits
            }

            // For every 5 chars of input, we accumulate
            // 40 bits of output. Emit 8 bytes.
            out.output(table[(bitBuffer shr 35 and 0x1fL).toInt()]) // 40-1*5 = 35
            out.output(table[(bitBuffer shr 30 and 0x1fL).toInt()]) // 40-2*5 = 30
            out.output(table[(bitBuffer shr 25 and 0x1fL).toInt()]) // 40-3*5 = 25
            out.output(table[(bitBuffer shr 20 and 0x1fL).toInt()]) // 40-4*5 = 20
            out.output(table[(bitBuffer shr 15 and 0x1fL).toInt()]) // 40-5*5 = 15
            out.output(table[(bitBuffer shr 10 and 0x1fL).toInt()]) // 40-6*5 = 10
            out.output(table[(bitBuffer shr  5 and 0x1fL).toInt()]) // 40-7*5 =  5
            out.output(table[(bitBuffer        and 0x1fL).toInt()]) // 40-8*5 =  0
        },
        finalize = { modulus, buffer ->
            var bitBuffer = 0L

            // Append each char remaining in the buffer to the bitBuffer
            for (i in 0 until modulus) {
                bitBuffer =  (bitBuffer shl  8) + buffer[i]
            }

            val padCount: Int = when (modulus) {
                0 -> { 0 }
                1 -> {
                    // 8*1 = 8 bits
                    out.output(table[(bitBuffer shr  3 and 0x1fL).toInt()]) // 8-1*5 = 3
                    out.output(table[(bitBuffer shl  2 and 0x1fL).toInt()]) // 5-3 = 2
                    6
                }
                2 -> {
                    // 8*2 = 16 bits
                    out.output(table[(bitBuffer shr 11 and 0x1fL).toInt()]) // 16-1*5 = 11
                    out.output(table[(bitBuffer shr  6 and 0x1fL).toInt()]) // 16-2*5 = 6
                    out.output(table[(bitBuffer shr  1 and 0x1fL).toInt()]) // 16-3*5 = 1
                    out.output(table[(bitBuffer shl  4 and 0x1fL).toInt()]) // 5-1 = 4
                    4
                }
                3 -> {
                    // 8*3 = 24 bits
                    out.output(table[(bitBuffer shr 19 and 0x1fL).toInt()]) // 24-1*5 = 19
                    out.output(table[(bitBuffer shr 14 and 0x1fL).toInt()]) // 24-2*5 = 14
                    out.output(table[(bitBuffer shr  9 and 0x1fL).toInt()]) // 24-3*5 = 9
                    out.output(table[(bitBuffer shr  4 and 0x1fL).toInt()]) // 24-4*5 = 4
                    out.output(table[(bitBuffer shl  1 and 0x1fL).toInt()]) // 5-4 = 1
                    3
                }
                // 4
                else -> {
                    // 8*4 = 32 bits
                    out.output(table[(bitBuffer shr 27 and 0x1fL).toInt()]) // 32-1*5 = 27
                    out.output(table[(bitBuffer shr 22 and 0x1fL).toInt()]) // 32-2*5 = 22
                    out.output(table[(bitBuffer shr 17 and 0x1fL).toInt()]) // 32-3*5 = 17
                    out.output(table[(bitBuffer shr 12 and 0x1fL).toInt()]) // 32-4*5 = 12
                    out.output(table[(bitBuffer shr  7 and 0x1fL).toInt()]) // 32-5*5 = 7
                    out.output(table[(bitBuffer shr  2 and 0x1fL).toInt()]) // 32-6*5 = 2
                    out.output(table[(bitBuffer shl  3 and 0x1fL).toInt()]) // 5-2 = 3
                    1
                }
            }

            if (paddingChar != null) {
                repeat(padCount) {
                    out.output(paddingChar)
                }
            }
        },
    )

    private inner class DecodingBuffer(out: Decoder.OutFeed): FeedBuffer(
        blockSize = 8,
        flush = { buffer ->
            // Append each char's 5 bits to the buffer
            var bitBuffer = 0L
            for (bits in buffer) {
                bitBuffer = (bitBuffer shl 5) or bits.toLong()
            }

            // For every 8 chars of input, we accumulate
            // 40 bits of output data. Emit 5 bytes.
            out.output((bitBuffer shr 32).toByte())
            out.output((bitBuffer shr 24).toByte())
            out.output((bitBuffer shr 16).toByte())
            out.output((bitBuffer shr  8).toByte())
            out.output((bitBuffer       ).toByte())
        },
        finalize = { modulus, buffer ->
            when (modulus) {
                1, 3, 6 -> {
                    // 5*1 =  5 bits. Truncated, fail.
                    // 5*3 = 15 bits. Truncated, fail.
                    // 5*6 = 30 bits. Truncated, fail.
                    throw truncatedInputEncodingException(modulus)
                }
            }

            var bitBuffer = 0L
            for (i in 0 until modulus) {
                bitBuffer = (bitBuffer shl 5) or buffer[i].toLong()
            }

            when (modulus) {
                0 -> { /* no-op */ }
                2 -> {
                    // 5*2 = 10 bits. Drop 2
                    bitBuffer = bitBuffer shr  2

                    // 8/8 = 1 byte
                    out.output((bitBuffer       ).toByte())
                }
                4 -> {
                    // 5*4 = 20 bits. Drop 4
                    bitBuffer = bitBuffer shr  4

                    // 16/8 = 2 bytes
                    out.output((bitBuffer shr  8).toByte())
                    out.output((bitBuffer       ).toByte())
                }
                5 -> {
                    // 5*5 = 25 bits. Drop 1
                    bitBuffer = bitBuffer shr  1

                    // 24/8 = 3 bytes
                    out.output((bitBuffer shr 16).toByte())
                    out.output((bitBuffer shr  8).toByte())
                    out.output((bitBuffer       ).toByte())
                }
                7 -> {
                    // 5*7 = 35 bits. Drop 3
                    bitBuffer = bitBuffer shr  3

                    // 32/8 = 4 bytes
                    out.output((bitBuffer shr 24).toByte())
                    out.output((bitBuffer shr 16).toByte())
                    out.output((bitBuffer shr  8).toByte())
                    out.output((bitBuffer       ).toByte())
                }
            }
        }
    )
}
