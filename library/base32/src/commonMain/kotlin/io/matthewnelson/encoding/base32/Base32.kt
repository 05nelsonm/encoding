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
import io.matthewnelson.encoding.base32.internal.toBits
import io.matthewnelson.encoding.core.*
import io.matthewnelson.encoding.core.util.DecoderInput
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
     *
     *     // Alternatively, use the static implementaton instead of
     *     // configuring your own settings.
     *     val decoded = encoded.decodeToByteArray(Base32.Crockford).decodeToString()
     *     assertEquals(text, decoded)
     *
     * @see [Base32Crockford]
     * @see [Crockford.Config]
     * @see [Crockford.CHARS_UPPER]
     * @see [Crockford.CHARS_LOWER]
     * @see [Crockford.Companion]
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
            @JvmField
            public val finalizeWhenFlushed: Boolean,
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
                return LinkedHashSet<Setting>(6, 1.0f).apply {
                    add(Setting(name = "encodeToLowercase", value = encodeToLowercase))
                    add(Setting(name = "hyphenInterval", value = hyphenInterval))
                    add(Setting(name = "checkSymbol", value = checkSymbol))
                    add(Setting(name = "finalizeWhenFlushed", value = finalizeWhenFlushed))
                    add(Setting(name = "isConstantTime", value = isConstantTime))
                }
            }

            internal companion object {

                @JvmSynthetic
                internal fun from(builder: Base32CrockfordConfigBuilder): Config {
                    return Config(
                        isLenient = builder.isLenient,
                        encodeToLowercase = builder.encodeToLowercase,
                        hyphenInterval = if (builder.hyphenInterval > 0) builder.hyphenInterval else 0,
                        checkSymbol = builder.checkSymbol,
                        finalizeWhenFlushed = builder.finalizeWhenFlushed,
                    )
                }
            }

            /**
             * Implementation is always constant-time. Performance impact is negligible.
             * @suppress
             * */
            @JvmField
            public val isConstantTime: Boolean = true
        }

        /**
         * Doubles as a static implementation with default settings
         * and a hyphenInterval of 4.
         *
         * e.g.
         *
         *     val encoded = "Hello World!"
         *         .encodeToByteArray()
         *         .encodeToString(Base32.Crockford)
         *
         *     println(encoded) // 91JP-RV3F-41BP-YWKC-CGGG
         *
         * */
        public companion object: EncoderDecoder<Base32.Crockford.Config>(
            config = Base32CrockfordConfigBuilder().apply { hyphenInterval = 4 }.build()
        ) {

            /**
             * Uppercase Base32 Crockford encoding characters.
             * */
            public const val CHARS_UPPER: String = "0123456789ABCDEFGHJKMNPQRSTVWXYZ"

            /**
             * Lowercase Base32 Crockford encoding characters.
             * */
            public const val CHARS_LOWER: String = "0123456789abcdefghjkmnpqrstvwxyz"

            private val DELEGATE = Crockford(config)
            protected override fun name(): String = DELEGATE.name()
            protected override fun newDecoderFeedProtected(out: Decoder.OutFeed): Decoder<Crockford.Config>.Feed {
                return DELEGATE.newDecoderFeedProtected(out)
            }
            protected override fun newEncoderFeedProtected(out: OutFeed): Encoder<Crockford.Config>.Feed {
                return DELEGATE.newEncoderFeedProtected(out)
            }
        }

        protected override fun newDecoderFeedProtected(out: Decoder.OutFeed): Decoder<Crockford.Config>.Feed {
            return object : Decoder<Crockford.Config>.Feed() {

                private var isCheckSymbolSet = false
                private val buffer = DecodingBuffer(out)

                @Throws(EncodingException::class)
                override fun consumeProtected(input: Char) {
                    if (isCheckSymbolSet) {
                        // If the set checkByte was not intended, it's only valid as the
                        // very last character and the previous update call was invalid.
                        throw EncodingException("CheckSymbol[${config.checkSymbol}] was set")
                    }

                    // Crockford allows for insertion of hyphens,
                    // which are to be ignored when decoding.
                    if (input == '-') return

                    val code = input.code

                    val ge0: Byte = if (code >= '0'.code) 1 else 0
                    val le9: Byte = if (code <= '9'.code) 1 else 0

                    val geA: Byte = if (code >= 'A'.code) 1 else 0
                    val leH: Byte = if (code <= 'H'.code) 1 else 0
                    val eqI: Byte = if (code == 'I'.code) 1 else 0
                    val eqL: Byte = if (code == 'L'.code) 1 else 0
                    val eqJ: Byte = if (code == 'J'.code) 1 else 0
                    val eqK: Byte = if (code == 'K'.code) 1 else 0
                    val eqM: Byte = if (code == 'M'.code) 1 else 0
                    val eqN: Byte = if (code == 'N'.code) 1 else 0
                    val eqO: Byte = if (code == 'O'.code) 1 else 0
                    val geP: Byte = if (code >= 'P'.code) 1 else 0
                    val leT: Byte = if (code <= 'T'.code) 1 else 0
                    val geV: Byte = if (code >= 'V'.code) 1 else 0
                    val leZ: Byte = if (code <= 'Z'.code) 1 else 0

                    val gea: Byte = if (code >= 'a'.code) 1 else 0
                    val leh: Byte = if (code <= 'h'.code) 1 else 0
                    val eqi: Byte = if (code == 'i'.code) 1 else 0
                    val eql: Byte = if (code == 'l'.code) 1 else 0
                    val eqj: Byte = if (code == 'j'.code) 1 else 0
                    val eqk: Byte = if (code == 'k'.code) 1 else 0
                    val eqm: Byte = if (code == 'm'.code) 1 else 0
                    val eqn: Byte = if (code == 'n'.code) 1 else 0
                    val eqo: Byte = if (code == 'o'.code) 1 else 0
                    val gep: Byte = if (code >= 'p'.code) 1 else 0
                    val let: Byte = if (code <= 't'.code) 1 else 0
                    val gev: Byte = if (code >= 'v'.code) 1 else 0
                    val lez: Byte = if (code <= 'z'.code) 1 else 0

                    var diff = 0

                    // char ASCII value
                    //  0     48    0
                    //  9     57    9 (ASCII - 48)
                    diff += if (ge0 + le9 == 2) -48 else 0

                    // char ASCII value
                    //  A     65   10
                    //  H     72   17 (ASCII - 55)
                    diff += if (geA + leH == 2) -55 else 0

                    // Crockford treats characters 'I', 'i', 'L' and 'l' as 1
                    val h = 1 - code
                    diff += if (eqI + eqi + eqL + eql == 1) h else 0

                    // char ASCII value
                    //  J     74   18
                    //  K     75   19 (ASCII - 56)
                    diff += if (eqJ + eqK == 1) -56 else 0

                    // char ASCII value
                    //  M     77   20
                    //  N     78   21 (ASCII - 57)
                    diff += if (eqM + eqN == 1) -57 else 0

                    // Crockford treats characters 'O' and 'o' as 0
                    val k = 0 - code
                    diff += if (eqO + eqo == 1) k else 0

                    // char ASCII value
                    //  P     80   22
                    //  T     84   26 (ASCII - 58)
                    diff += if (geP + leT == 2) -58 else 0

                    // char ASCII value
                    //  V     86   27
                    //  Z     90   31 (ASCII - 59)
                    diff += if (geV + leZ == 2) -59 else 0

                    // char ASCII value
                    //  a     97   10
                    //  h    104   17 (ASCII - 87)
                    diff += if (gea + leh == 2) -87 else 0

                    // char ASCII value
                    //  j    106   18
                    //  k    107   19 (ASCII - 88)
                    diff += if (eqj + eqk == 1) -88 else 0

                    // char ASCII value
                    //  m    109   20
                    //  n    110   21 (ASCII - 89)
                    diff += if (eqm + eqn == 1) -89 else 0

                    // char ASCII value
                    //  p    112   22
                    //  t    116   26 (ASCII - 90)
                    diff += if (gep + let == 2) -90 else 0

                    // char ASCII value
                    //  v    118   27
                    //  z    122   31 (ASCII - 91)
                    diff += if (gev + lez == 2) -91 else 0

                    if (diff != 0) {
                        buffer.update(code + diff)
                        return
                    }

                    if (!input.isCheckSymbol()) {
                        throw EncodingException("Char[${input}] is not a valid Base32 Crockford character")
                    }

                    if (config.checkSymbol?.uppercaseChar() == input.uppercaseChar()) {
                        isCheckSymbolSet = true
                        return
                    }

                    throw EncodingException(
                        "Char[${input}] IS a checkSymbol, but did " +
                        "not match config's Checksymbol[${config.checkSymbol}]"
                    )
                }

                @Throws(EncodingException::class)
                override fun doFinalProtected() {
                    buffer.finalize()
                    isCheckSymbolSet = false
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
                    table = if (config.encodeToLowercase) CHARS_LOWER else CHARS_UPPER,
                    paddingChar = null,
                )

                override fun consumeProtected(input: Byte) { buffer.update(input.toInt()) }

                override fun doFinalProtected() {
                    buffer.finalize()

                    if (config.finalizeWhenFlushed || isClosed()) {
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

                        outCount = 0
                        outputHyphenOnNext = false
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
     *
     *     // Alternatively, use the static implementaton instead of
     *     // configuring your own settings.
     *     val decoded = encoded.decodeToByteArray(Base32.Default).decodeToString()
     *     assertEquals(text, decoded)
     *
     * @see [Base32Default]
     * @see [Default.Config]
     * @see [Default.CHARS_UPPER]
     * @see [Default.CHARS_LOWER]
     * @see [Default.Companion]
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
                return LinkedHashSet<Setting>(4, 1.0f).apply {
                    add(Setting(name = "encodeToLowercase", value = encodeToLowercase))
                    add(Setting(name = "padEncoded", value = padEncoded))
                    add(Setting(name = "isConstantTime", value = isConstantTime))
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

            /**
             * Implementation is always constant-time. Performance impact is negligible.
             * @suppress
             * */
            @JvmField
            public val isConstantTime: Boolean = true
        }

        /**
         * Doubles as a static implementation with default settings
         * and a lineBreakInterval of 64.
         *
         * e.g.
         *
         *     val encoded = "Hello World!"
         *         .encodeToByteArray()
         *         .encodeToString(Base32.Default)
         *
         *     println(encoded) // JBSWY3DPEBLW64TMMQQQ====
         *
         * */
        public companion object: EncoderDecoder<Base32.Default.Config>(
            config = Base32DefaultConfigBuilder().apply { lineBreakInterval = 64 }.build()
        ) {

            /**
             * Uppercase Base32 Default encoding characters.
             * */
            public const val CHARS_UPPER: String = "ABCDEFGHIJKLMNOPQRSTUVWXYZ234567"

            /**
             * Lowercase Base32 Default encoding characters.
             * */
            public const val CHARS_LOWER: String = "abcdefghijklmnopqrstuvwxyz234567"

            private val DELEGATE = Default(config)
            protected override fun name(): String = DELEGATE.name()
            protected override fun newDecoderFeedProtected(out: Decoder.OutFeed): Decoder<Default.Config>.Feed {
                return DELEGATE.newDecoderFeedProtected(out)
            }
            protected override fun newEncoderFeedProtected(out: OutFeed): Encoder<Default.Config>.Feed {
                return DELEGATE.newEncoderFeedProtected(out)
            }
        }

        protected override fun newDecoderFeedProtected(out: Decoder.OutFeed): Decoder<Default.Config>.Feed {
            return object : Decoder<Default.Config>.Feed() {

                private val buffer = DecodingBuffer(out)

                @Throws(EncodingException::class)
                override fun consumeProtected(input: Char) {
                    val code = input.code

                    val ge2: Byte = if (code >= '2'.code) 1 else 0
                    val le7: Byte = if (code <= '7'.code) 1 else 0
                    val geA: Byte = if (code >= 'A'.code) 1 else 0
                    val leZ: Byte = if (code <= 'Z'.code) 1 else 0
                    val gea: Byte = if (code >= 'a'.code) 1 else 0
                    val lez: Byte = if (code <= 'z'.code) 1 else 0

                    var diff = 0

                    // char ASCII value
                    //  2     50   26
                    //  7     55   31 (ASCII - 24)
                    diff += if (ge2 + le7 == 2) -24 else 0

                    // char ASCII value
                    //  A     65    0
                    //  Z     90   25 (ASCII - 65)
                    diff += if (geA + leZ == 2) -65 else 0

                    // char ASCII value
                    //  a     97   0
                    //  z    122   25 (ASCII - 97)
                    diff += if (gea + lez == 2) -97 else 0

                    if (diff == 0) {
                        throw EncodingException("Char[${input}] is not a valid Base32 Default character")
                    }

                    buffer.update(code + diff)
                }

                @Throws(EncodingException::class)
                override fun doFinalProtected() { buffer.finalize() }
            }
        }

        protected override fun newEncoderFeedProtected(out: OutFeed): Encoder<Default.Config>.Feed {
            return object : Encoder<Default.Config>.Feed() {

                private val buffer = EncodingBuffer(
                    out = out,
                    table = if (config.encodeToLowercase) CHARS_LOWER else CHARS_UPPER,
                    paddingChar = if (config.padEncoded) config.paddingChar else null,
                )

                override fun consumeProtected(input: Byte) { buffer.update(input.toInt()) }

                override fun doFinalProtected() { buffer.finalize() }
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
     *
     *     // Alternatively, use the static implementaton instead of
     *     // configuring your own settings.
     *     val decoded = encoded.decodeToByteArray(Base32.Hex).decodeToString()
     *     assertEquals(text, decoded)
     *
     * @see [Base32Hex]
     * @see [Hex.Config]
     * @see [Hex.CHARS_UPPER]
     * @see [Hex.CHARS_LOWER]
     * @see [Hex.Companion]
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
                return LinkedHashSet<Setting>(4, 1.0f).apply {
                    add(Setting(name = "encodeToLowercase", value = encodeToLowercase))
                    add(Setting(name = "padEncoded", value = padEncoded))
                    add(Setting(name = "isConstantTime", value = isConstantTime))
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

            /**
             * Implementation is always constant-time. Performance impact is negligible.
             * @suppress
             * */
            @JvmField
            public val isConstantTime: Boolean = true
        }

        /**
         * Doubles as a static implementation with default settings
         * and a lineBreakInterval of 64.
         *
         * e.g.
         *
         *     val encoded = "Hello World!"
         *         .encodeToByteArray()
         *         .encodeToString(Base32.Hex)
         *
         *     println(encoded) // 91IMOR3F41BMUSJCCGGG====
         *
         * */
        public companion object: EncoderDecoder<Base32.Hex.Config>(
            config = Base32HexConfigBuilder().apply { lineBreakInterval = 64 }.build()
        ) {

            /**
             * Uppercase Base32 Hex encoding characters.
             * */
            public const val CHARS_UPPER: String = "0123456789ABCDEFGHIJKLMNOPQRSTUV"

            /**
             * Lowercase Base32 Hex encoding characters.
             * */
            public const val CHARS_LOWER: String = "0123456789abcdefghijklmnopqrstuv"

            private val DELEGATE = Hex(config)
            override fun name(): String = DELEGATE.name()
            override fun newDecoderFeedProtected(out: Decoder.OutFeed): Decoder<Hex.Config>.Feed {
                return DELEGATE.newDecoderFeedProtected(out)
            }
            override fun newEncoderFeedProtected(out: OutFeed): Encoder<Hex.Config>.Feed {
                return DELEGATE.newEncoderFeedProtected(out)
            }
        }

        protected override fun newDecoderFeedProtected(out: Decoder.OutFeed): Decoder<Hex.Config>.Feed {
            return object : Decoder<Hex.Config>.Feed() {

                private val buffer = DecodingBuffer(out)

                @Throws(EncodingException::class)
                override fun consumeProtected(input: Char) {
                    val code = input.code

                    val ge0: Byte = if (code >= '0'.code) 1 else 0
                    val le9: Byte = if (code <= '9'.code) 1 else 0
                    val geA: Byte = if (code >= 'A'.code) 1 else 0
                    val leV: Byte = if (code <= 'V'.code) 1 else 0
                    val gea: Byte = if (code >= 'a'.code) 1 else 0
                    val lev: Byte = if (code <= 'v'.code) 1 else 0

                    var diff = 0

                    // char ASCII value
                    //  0     48    0
                    //  9     57    9 (ASCII - 48)
                    diff += if (ge0 + le9 == 2) -48 else 0

                    // char ASCII value
                    //  A     65   10
                    //  V     86   31 (ASCII - 55)
                    diff += if (geA + leV == 2) -55 else 0

                    // char ASCII value
                    //  a     97   10
                    //  v    118   31 (ASCII - 87)
                    diff += if (gea + lev == 2) -87 else 0

                    if (diff == 0) {
                        throw EncodingException("Char[${input}] is not a valid Base32 Hex character")
                    }

                    buffer.update(code + diff)
                }

                @Throws(EncodingException::class)
                override fun doFinalProtected() { buffer.finalize() }
            }
        }

        protected override fun newEncoderFeedProtected(out: OutFeed): Encoder<Hex.Config>.Feed {
            return object : Encoder<Hex.Config>.Feed() {

                private val buffer = EncodingBuffer(
                    out = out,
                    table = if (config.encodeToLowercase) CHARS_LOWER else CHARS_UPPER,
                    paddingChar = if (config.padEncoded) config.paddingChar else null,
                )

                override fun consumeProtected(input: Byte) { buffer.update(input.toInt()) }

                override fun doFinalProtected() { buffer.finalize() }
            }
        }

        protected override fun name(): String = "Base32.Hex"
    }

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
                bitBuffer =  (bitBuffer shl  8) + bits.toByte().toBits()
            }

            // For every 5 bytes of input, we accumulate
            // 40 bits of output. Emit 8 characters.
            val i1 = (bitBuffer shr 35 and 0x1fL).toInt() // 40-1*5 = 35
            val i2 = (bitBuffer shr 30 and 0x1fL).toInt() // 40-2*5 = 30
            val i3 = (bitBuffer shr 25 and 0x1fL).toInt() // 40-3*5 = 25
            val i4 = (bitBuffer shr 20 and 0x1fL).toInt() // 40-4*5 = 20
            val i5 = (bitBuffer shr 15 and 0x1fL).toInt() // 40-5*5 = 15
            val i6 = (bitBuffer shr 10 and 0x1fL).toInt() // 40-6*5 = 10
            val i7 = (bitBuffer shr  5 and 0x1fL).toInt() // 40-7*5 =  5
            val i8 = (bitBuffer        and 0x1fL).toInt() // 40-8*5 =  0

            out.output(table[i1])
            out.output(table[i2])
            out.output(table[i3])
            out.output(table[i4])
            out.output(table[i5])
            out.output(table[i6])
            out.output(table[i7])
            out.output(table[i8])
        },
        finalize = { modulus, buffer ->
            var bitBuffer = 0L

            // Append each char remaining in the buffer to the bitBuffer
            for (i in 0 until modulus) {
                bitBuffer =  (bitBuffer shl  8) + buffer[i].toByte().toBits()
            }

            val padCount: Int = when (modulus) {
                0 -> { 0 }
                1 -> {
                    // 8*1 = 8 bits
                    val i1 = (bitBuffer shr  3 and 0x1fL).toInt() // 8-1*5 = 3
                    val i2 = (bitBuffer shl  2 and 0x1fL).toInt() // 5-3 = 2

                    out.output(table[i1])
                    out.output(table[i2])

                    6
                }
                2 -> {
                    // 8*2 = 16 bits
                    val i1 = (bitBuffer shr 11 and 0x1fL).toInt() // 16-1*5 = 11
                    val i2 = (bitBuffer shr  6 and 0x1fL).toInt() // 16-2*5 = 6
                    val i3 = (bitBuffer shr  1 and 0x1fL).toInt() // 16-3*5 = 1
                    val i4 = (bitBuffer shl  4 and 0x1fL).toInt() // 5-1 = 4

                    out.output(table[i1])
                    out.output(table[i2])
                    out.output(table[i3])
                    out.output(table[i4])

                    4
                }
                3 -> {
                    // 8*3 = 24 bits
                    val i1 = (bitBuffer shr 19 and 0x1fL).toInt() // 24-1*5 = 19
                    val i2 = (bitBuffer shr 14 and 0x1fL).toInt() // 24-2*5 = 14
                    val i3 = (bitBuffer shr  9 and 0x1fL).toInt() // 24-3*5 = 9
                    val i4 = (bitBuffer shr  4 and 0x1fL).toInt() // 24-4*5 = 4
                    val i5 = (bitBuffer shl  1 and 0x1fL).toInt() // 5-4 = 1

                    out.output(table[i1])
                    out.output(table[i2])
                    out.output(table[i3])
                    out.output(table[i4])
                    out.output(table[i5])

                    3
                }
                // 4
                else -> {
                    // 8*4 = 32 bits
                    val i1 = (bitBuffer shr 27 and 0x1fL).toInt() // 32-1*5 = 27
                    val i2 = (bitBuffer shr 22 and 0x1fL).toInt() // 32-2*5 = 22
                    val i3 = (bitBuffer shr 17 and 0x1fL).toInt() // 32-3*5 = 17
                    val i4 = (bitBuffer shr 12 and 0x1fL).toInt() // 32-4*5 = 12
                    val i5 = (bitBuffer shr  7 and 0x1fL).toInt() // 32-5*5 = 7
                    val i6 = (bitBuffer shr  2 and 0x1fL).toInt() // 32-6*5 = 2
                    val i7 = (bitBuffer shl  3 and 0x1fL).toInt() // 5-2 = 3

                    out.output(table[i1])
                    out.output(table[i2])
                    out.output(table[i3])
                    out.output(table[i4])
                    out.output(table[i5])
                    out.output(table[i6])
                    out.output(table[i7])

                    1
                }
            }

            if (paddingChar != null) {
                repeat(padCount) { out.output(paddingChar) }
            }
        },
    )
}
