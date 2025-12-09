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
@file:Suppress("FunctionName", "NOTHING_TO_INLINE", "PropertyName", "RedundantModalityModifier", "RedundantVisibilityModifier", "RemoveRedundantQualifierName")

package io.matthewnelson.encoding.base32

import io.matthewnelson.encoding.base32.internal.build
import io.matthewnelson.encoding.base32.internal.decodeOutMaxSize32
import io.matthewnelson.encoding.base32.internal.decodeOutMaxSize64
import io.matthewnelson.encoding.base32.internal.encodeOutSize64
import io.matthewnelson.encoding.base32.internal.isCheckSymbol
import io.matthewnelson.encoding.core.Decoder
import io.matthewnelson.encoding.core.Encoder
import io.matthewnelson.encoding.core.EncoderDecoder
import io.matthewnelson.encoding.core.EncodingException
import io.matthewnelson.encoding.core.util.DecoderInput
import io.matthewnelson.encoding.core.util.FeedBuffer
import io.matthewnelson.encoding.core.util.LineBreakOutFeed
import kotlin.contracts.ExperimentalContracts
import kotlin.contracts.InvocationKind
import kotlin.contracts.contract
import kotlin.jvm.JvmField
import kotlin.jvm.JvmName
import kotlin.jvm.JvmStatic
import kotlin.jvm.JvmSynthetic

/**
 * Base32 encoding/decoding
 *
 * @see [Crockford]
 * @see [Default]
 * @see [Hex]
 * */
public sealed class Base32<C: EncoderDecoder.Config>(config: C): EncoderDecoder<C>(config) {

    /**
     * Base32 encoding/decoding in accordance with the [Crockford Spec](https://www.crockford.com/base32.html)
     *
     * e.g.
     *
     *     val crockford = Base32.Crockford.Builder {
     *         isLenient(enable = true)
     *         encodeLowercase(enable = false)
     *         hyphen(interval = 5)
     *         check(symbol = '~')
     *     }
     *
     *     val text = "Hello World!"
     *     val bytes = text.encodeToByteArray()
     *     val encoded = bytes.encodeToString(crockford)
     *     println(encoded) // 91JPR-V3F41-BPYWK-CCGGG~
     *
     *     // Alternatively, use the static implementation containing
     *     // pre-configured settings, instead of creating your own.
     *     val decoded = encoded.decodeToByteArray(Base32.Crockford).decodeToString()
     *     assertEquals(text, decoded)
     *
     * @see [Builder]
     * @see [Companion.Builder]
     * @see [Encoder.Companion]
     * @see [Decoder.Companion]
     * */
    public class Crockford: Base32<Crockford.Config> {

        /**
         * A Builder
         *
         * @see [Companion.Builder]
         * */
        public class Builder {

            public constructor(): this(other = null)
            public constructor(other: Config?) {
                if (other == null) return
                this._isLenient = other.isLenient ?: true
                this._encodeLowercase = other.encodeLowercase
                this._hyphenInterval = other.hyphenInterval
                this._checkSymbol = other.checkSymbol
                this._finalizeWhenFlushed = other.finalizeWhenFlushed
                this._backFillBuffers = other.backFillBuffers
            }

            @get:JvmSynthetic
            @set:JvmSynthetic
            internal var _isLenient: Boolean = true
            @get:JvmSynthetic
            @set:JvmSynthetic
            internal var _encodeLowercase: Boolean = false
            @get:JvmSynthetic
            @set:JvmSynthetic
            internal var _hyphenInterval: Byte = 0
            @get:JvmSynthetic
            @set:JvmSynthetic
            internal var _checkSymbol: Char? = null
            @get:JvmSynthetic
            @set:JvmSynthetic
            internal var _backFillBuffers: Boolean = true

            // Here for compatibility purposes with Base32CrockfordConfigBuilder
            @get:JvmSynthetic
            @set:JvmSynthetic
            internal var _finalizeWhenFlushed: Boolean = true

            /**
             * DEFAULT: `true`
             *
             * If `true`, the characters ('\n', '\r', ' ', '\t') will be skipped over (i.e.
             * allowed but ignored) during decoding operations. This is non-compliant with
             * the Crockford spec.
             *
             * If `false`, an [EncodingException] will be thrown.
             * */
            public fun isLenient(enable: Boolean): Builder = apply { _isLenient = enable }

            /**
             * DEFAULT: `false`
             *
             * If `true`, lowercase characters from table [Crockford.CHARS_LOWER] will be output
             * during encoding operations. This is non-compliant with the Crockford spec.
             *
             * If `false`, uppercase characters from table [Crockford.CHARS_UPPER] will be output
             * during encoding operations.
             *
             * **NOTE:** This does not affect decoding operations. [Crockford] is designed to accept
             * characters from both tables when decoding (as specified in the Crockford spec).
             * */
            public fun encodeLowercase(enable: Boolean): Builder = apply { _encodeLowercase = enable }

            /**
             * DEFAULT: `0` (i.e. disabled)
             *
             * If greater than `0`, when [interval] number of encoded characters have been
             * output, the next encoded character will be preceded with the hyphen character
             * `-`.
             *
             * e.g.
             *
             *     hyphen(interval = 0)
             *     // 91JPRV3F41BPYWKCCGGG
             *
             *     hyphen(interval = 5)
             *     // 91JPR-V3F41-BPYWK-CCGGG
             *
             *     hyphen(interval = 4)
             *     // 91JP-RV3F-41BP-YWKC-CGGG
             * */
            public fun hyphen(interval: Byte): Builder = apply { _hyphenInterval = interval }

            /**
             * DEFAULT: `null` (i.e. no check symbol)
             *
             * Specify a check symbol ('*', '~', '$', '=', 'U', 'u') to be appended to encoded
             * output, and verified when decoding.
             *
             * If `null`, no check symbol will be appended to encoded output, and decoding of
             * any input containing a check symbol will fail due to misconfiguration.
             *
             * @throws [IllegalArgumentException] If not `null`, or a valid symbol.
             * */
            public fun check(symbol: Char?): Builder {
                if (symbol == null || symbol.isCheckSymbol()) {
                    _checkSymbol = symbol
                    return this
                }
                throw IllegalArgumentException(
                    "CheckSymbol[$symbol] not recognized.\n" +
                    "Must be one of the following characters: *, ~, \$, =, U, u\n" +
                    "OR null to omit"
                )
            }

            /**
             * DEFAULT: `true`
             *
             * @see [EncoderDecoder.Config.backFillBuffers]
             * */
            public fun backFillBuffers(enable: Boolean): Builder = apply { _backFillBuffers = enable }

            /**
             * Helper for configuring the builder with settings which are compliant with the
             * Crockford specification.
             *
             *  - [isLenient] will be set to `false`.
             *  - [encodeLowercase] will be set to `false`.
             * */
            public fun strictSpec(): Builder = apply {
                _isLenient = false
                _encodeLowercase = false
            }

            /**
             * Commits configured options to [Config], creating the [Crockford] instance.
             * */
            public fun build(): Crockford = Config.build(this)
        }

        /**
         * Holder of a configuration for the [Crockford] encoded/decoder instance.
         *
         * @se [Builder]
         * @see [Companion.Builder]
         * */
        public class Config private constructor(
            isLenient: Boolean,
            @JvmField
            public val encodeLowercase: Boolean,
            @JvmField
            public val hyphenInterval: Byte,
            @JvmField
            public val checkSymbol: Char?,

            // Deprecated option from old Base32CrockfordConfigBuilder
            // which is no-longer available from Crockford.Builder and
            // always defaults to true.
            @JvmField
            public val finalizeWhenFlushed: Boolean,
            backFillBuffers: Boolean,
        ): EncoderDecoder.Config(
            isLenient,
            lineBreakInterval = 0,
            lineBreakResetOnFlush = true, // TODO
            paddingChar = null,
            maxDecodeEmit = 5,
            backFillBuffers,
        ) {

            protected override fun decodeOutMaxSizeProtected(encodedSize: Long): Long {
                return decodeOutMaxSize64(encodedSize)
            }

            protected override fun decodeOutMaxSizeOrFailProtected(encodedSize: Int, input: DecoderInput): Int {
                var outSize = encodedSize

                // encodeSize will always be greater than 0
                val cLast = input[encodedSize - 1]

                if (checkSymbol != null) {
                    // Uppercase them so that little 'u' is always compared as big 'U'.
                    val upperExpected = checkSymbol.uppercaseChar()
                    val upperCLast = cLast.uppercaseChar()

                    if (upperCLast != upperExpected) {
                        // Wrong or no symbol
                        val msg = if (cLast.isCheckSymbol()) {
                            "Wrong check symbol. Expected[$checkSymbol] vs Actual[$cLast]"
                        } else {
                            "Missing check symbol. Expected[$checkSymbol]"
                        }
                        throw EncodingException(msg)
                    } else {
                        outSize--
                    }
                } else {
                    // Mine as well check it here before actually decoding.
                    if (cLast.isCheckSymbol()) {
                        throw EncodingException(
                            "Decoder Misconfiguration.\n" +
                            "Encoded data has CheckSymbol[$cLast], but the " +
                            "decoder is configured to reject check symbols."
                        )
                    }
                }

                return decodeOutMaxSize32(outSize)
            }

            protected override fun encodeOutSizeProtected(unEncodedSize: Long): Long {
                var outSize = encodeOutSize64(unEncodedSize, willBePadded = false)

                // checkByte will be appended if present
                if (checkSymbol != null && ++outSize < 0L) {
                    throw outSizeExceedsMaxEncodingSizeException(unEncodedSize, Long.MAX_VALUE)
                }

                if (hyphenInterval > 0) {
                    var hyphenCount: Double = (outSize.toDouble() / hyphenInterval) - 1.0

                    if (hyphenCount > 0.0) {
                        if (hyphenCount.rem(1.0) > 0.0) {
                            hyphenCount++
                        }

                        if (outSize > (Long.MAX_VALUE - hyphenCount)) {
                            throw outSizeExceedsMaxEncodingSizeException(unEncodedSize, Long.MAX_VALUE)
                        }

                        outSize += hyphenCount.toLong()
                    }
                }

                return outSize
            }

            protected override fun toStringAddSettings(): Set<Setting> = buildSet(capacity = 5) {
                add(Setting(name = "encodeLowercase", value = encodeLowercase))
                add(Setting(name = "hyphenInterval", value = hyphenInterval))
                add(Setting(name = "checkSymbol", value = checkSymbol))
                add(Setting(name = "finalizeWhenFlushed", value = finalizeWhenFlushed))
                add(Setting(name = "isConstantTime", value = isConstantTime))
            }

            internal companion object {

                @JvmSynthetic
                internal fun build(b: Builder): Crockford = ::Config.build(b, ::Crockford)

                @get:JvmSynthetic
                internal val DEFAULT: Config = Config(
                    isLenient = true,
                    encodeLowercase = false,
                    hyphenInterval = 4,
                    checkSymbol = null,
                    finalizeWhenFlushed = true,
                    backFillBuffers = true,
                )
            }

            /**
             * DEPRECATED since `2.6.0`
             * @suppress
             * */
            @JvmField
            @Deprecated(
                message = "Variable name changed.",
                replaceWith = ReplaceWith("encodeLowercase"),
                level = DeprecationLevel.WARNING,
            )
            public val encodeToLowercase: Boolean = encodeLowercase
            /** @suppress */
            @JvmField
            public val isConstantTime: Boolean = true
        }

        /**
         * A static instance of [EncoderDecoder] configured with a [Crockford.Builder.hyphen]
         * interval of `4`, and remaining [Crockford.Builder] `DEFAULT` values.
         * */
        public companion object: EncoderDecoder<Crockford.Config>(config = Crockford.Config.DEFAULT) {

            /**
             * Uppercase Base32 Crockford encoding characters.
             * */
            public const val CHARS_UPPER: String = "0123456789ABCDEFGHJKMNPQRSTVWXYZ"

            /**
             * Lowercase Base32 Crockford encoding characters.
             * */
            public const val CHARS_LOWER: String = "0123456789abcdefghjkmnpqrstvwxyz"

            /**
             * Syntactic sugar for Kotlin consumers that like lambdas.
             * */
            @JvmStatic
            @JvmName("-Builder")
            @OptIn(ExperimentalContracts::class)
            public inline fun Builder(block: Builder.() -> Unit): Crockford {
                contract { callsInPlace(block, InvocationKind.EXACTLY_ONCE) }
                return Builder(other = null, block)
            }

            /**
             * Syntactic sugar for Kotlin consumers that like lambdas.
             * */
            @JvmStatic
            @JvmName("-Builder")
            @OptIn(ExperimentalContracts::class)
            public inline fun Builder(other: Crockford.Config?, block: Builder.() -> Unit): Crockford {
                contract { callsInPlace(block, InvocationKind.EXACTLY_ONCE) }
                return Builder(other).apply(block).build()
            }

            @get:JvmSynthetic
            internal val DELEGATE = Crockford(config, unused = null)
            protected override fun name(): String = DELEGATE.name()
            protected override fun newDecoderFeedProtected(out: Decoder.OutFeed): Decoder<Crockford.Config>.Feed {
                return DELEGATE.newDecoderFeedProtected(out)
            }
            protected override fun newEncoderFeedProtected(out: Encoder.OutFeed): Encoder<Crockford.Config>.Feed {
                return DELEGATE.newEncoderFeedProtected(out)
            }

            private const val NAME = "Base32.Crockford"
        }

        protected final override fun name(): String = NAME

        protected final override fun newDecoderFeedProtected(out: Decoder.OutFeed): Decoder<Crockford.Config>.Feed {
            return CrockfordDecoderFeed(out)
        }

        protected final override fun newEncoderFeedProtected(out: Encoder.OutFeed): Encoder<Crockford.Config>.Feed {
            val _out = if (config.hyphenInterval <= 0) out else HyphenOutFeed(config.hyphenInterval, out)

            return if (config.encodeLowercase) {
                object : EncoderFeed(_out) {
                    override fun Encoder.OutFeed.output1(i: Int) {
                        output(CHARS_LOWER[i])
                    }
                    override fun Encoder.OutFeed.outputPadding(n: Int) {
                        if (isClosed() || config.finalizeWhenFlushed) {
                            config.checkSymbol?.let { symbol ->
                                output(symbol.lowercaseChar())
                            }
                            (this as? HyphenOutFeed)?.reset()
                        }
                    }
                }
            } else {
                object : EncoderFeed(_out) {
                    override fun Encoder.OutFeed.output1(i: Int) {
                        output(CHARS_UPPER[i])
                    }
                    override fun Encoder.OutFeed.outputPadding(n: Int) {
                        if (isClosed() || config.finalizeWhenFlushed) {
                            config.checkSymbol?.let { symbol ->
                                output(symbol.uppercaseChar())
                            }
                            (this as? HyphenOutFeed)?.reset()
                        }
                    }
                }
            }
        }

        /**
         * DEPRECATED since `2.6.0`
         * @suppress
         * */
        @Deprecated(
            message = "This constructor is scheduled for removal. Use Base32.Crockford.Builder or Base32.Crockford.Companion.Builder.",
            level = DeprecationLevel.WARNING,
        )
        public constructor(config: Config): this(config, unused = null)

        @Suppress("UNUSED_PARAMETER")
        private constructor(config: Config, unused: Any?): super(config)
    }

    /**
     * Base32 encoding/decoding in accordance with [RFC 4648 section 6](https://www.ietf.org/rfc/rfc4648.html#section-6)
     *
     * e.g.
     *
     *     val default = Base32.Default.Builder {
     *         isLenient(enable = true)
     *         lineBreak(interval = 64)
     *         encodeLowercase(enable = false)
     *         padEncoded(enable = true)
     *     }
     *
     *     val text = "Hello World!"
     *     val bytes = text.encodeToByteArray()
     *     val encoded = bytes.encodeToString(default)
     *     println(encoded) // JBSWY3DPEBLW64TMMQQQ====
     *
     *     // Alternatively, use the static implementation containing
     *     // pre-configured settings, instead of creating your own.
     *     val decoded = encoded.decodeToByteArray(Base32.Default).decodeToString()
     *     assertEquals(text, decoded)
     *
     * @see [Builder]
     * @see [Companion.Builder]
     * @see [Encoder.Companion]
     * @see [Decoder.Companion]
     * */
    public class Default: Base32<Default.Config> {

        /**
         * A Builder
         *
         * @see [Companion.Builder]
         * */
        public class Builder {

            public constructor(): this(other = null)
            public constructor(other: Config?) {
                if (other == null) return
                this._isLenient = other.isLenient ?: true
                this._lineBreakInterval = other.lineBreakInterval
                this._encodeLowercase = other.encodeLowercase
                this._padEncoded = other.padEncoded
                this._backFillBuffers = other.backFillBuffers
            }

            @get:JvmSynthetic
            @set:JvmSynthetic
            internal var _isLenient: Boolean = true
            @get:JvmSynthetic
            @set:JvmSynthetic
            internal var _lineBreakInterval: Byte = 0
            @get:JvmSynthetic
            @set:JvmSynthetic
            internal var _encodeLowercase: Boolean = false
            @get:JvmSynthetic
            @set:JvmSynthetic
            internal var _padEncoded: Boolean = true
            @get:JvmSynthetic
            @set:JvmSynthetic
            internal var _backFillBuffers: Boolean = true

            /**
             * DEFAULT: `true`
             *
             * If `true`, the characters ('\n', '\r', ' ', '\t') will be skipped over (i.e.
             * allowed but ignored) during decoding operations. This is non-compliant with
             * `RFC 4648`.
             *
             * If `false`, an [EncodingException] will be thrown.
             * */
            public fun isLenient(enable: Boolean): Builder = apply { _isLenient = enable }

            /**
             * DEFAULT: `0` (i.e. disabled)
             *
             * If greater than `0`, when [interval] number of encoded characters have been
             * output, the next encoded character will be preceded with the new line character
             * `\n`. This is non-compliant with `RFC 4648`.
             *
             * A great value is `64`, and is what [Default.Companion.config] uses.
             *
             * **NOTE:** This setting is ignored if [isLenient] is set to `false`.
             *
             * e.g.
             *
             *     isLenient(enable = true)
             *     lineBreak(interval = 0)
             *     // JBSWY3DPEBLW64TMMQQQ====
             *
             *     isLenient(enable = true)
             *     lineBreak(interval = 16)
             *     // JBSWY3DPEBLW64TM
             *     // MQQQ====
             *
             *     isLenient(enable = false)
             *     lineBreak(interval = 16)
             *     // JBSWY3DPEBLW64TMMQQQ====
             *
             * @see [EncoderDecoder.Config.lineBreakInterval]
             * */
            public fun lineBreak(interval: Byte): Builder = apply { _lineBreakInterval = interval }

            /**
             * DEFAULT: `false`
             *
             * If `true`, lowercase characters from table [Default.CHARS_LOWER] will be output
             * during encoding operations. This is non-compliant with `RFC 4648`.
             *
             * If `false`, uppercase characters from table [Default.CHARS_UPPER] will be output
             * during encoding operations.
             *
             * **NOTE:** This does not affect decoding operations. [Default] is designed to accept
             * characters from both tables when decoding (as specified in `RFC 4648`).
             * */
            public fun encodeLowercase(enable: Boolean): Builder = apply { _encodeLowercase = enable }

            /**
             * DEFAULT: `true`
             *
             * If `true`, encoded output will have the appropriate number of padding character(s)
             * `=` appended to it.
             *
             * If `false`, padding character(s) will be omitted from output. This is non-compliant
             * with `RFC 4648`.
             * */
            public fun padEncoded(enable: Boolean): Builder = apply { _padEncoded = enable }

            /**
             * DEFAULT: `true`
             *
             * @see [EncoderDecoder.Config.backFillBuffers]
             * */
            public fun backFillBuffers(enable: Boolean): Builder = apply { _backFillBuffers = enable }

            /**
             * Helper for configuring the builder with settings which are compliant with the
             * `RFC 4648` specification.
             *
             *  - [isLenient] will be set to `false`.
             *  - [lineBreak] will be set to `0`.
             *  - [encodeLowercase] will be set to `false`.
             *  - [padEncoded] will be set to `true`.
             * */
            public fun strictSpec(): Builder = apply {
                _isLenient = false
                _lineBreakInterval = 0
                _encodeLowercase = false
                _padEncoded = true
            }

            /**
             * Commits configured options to [Config], creating the [Default] instance.
             * */
            public fun build(): Default = Config.build(this)
        }

        /**
         * Holder of a configuration for the [Default] encoder/decoder instance.
         *
         * @see [Builder]
         * @see [Companion.Builder]
         * */
        public class Config private constructor(
            isLenient: Boolean,
            lineBreakInterval: Byte,
            @JvmField
            public val encodeLowercase: Boolean,
            @JvmField
            public val padEncoded: Boolean,
            backFillBuffers: Boolean,
        ): EncoderDecoder.Config(
            isLenient,
            lineBreakInterval,
            lineBreakResetOnFlush = true, // TODO
            paddingChar = '=',
            maxDecodeEmit = 5,
            backFillBuffers,
        ) {

            protected override fun decodeOutMaxSizeProtected(encodedSize: Long): Long {
                return decodeOutMaxSize64(encodedSize)
            }

            protected override fun decodeOutMaxSizeOrFailProtected(encodedSize: Int, input: DecoderInput): Int {
                return decodeOutMaxSize32(encodedSize)
            }

            protected override fun encodeOutSizeProtected(unEncodedSize: Long): Long {
                return encodeOutSize64(unEncodedSize, willBePadded = padEncoded)
            }

            protected override fun toStringAddSettings(): Set<Setting> = buildSet(capacity = 3) {
                add(Setting(name = "encodeLowercase", value = encodeLowercase))
                add(Setting(name = "padEncoded", value = padEncoded))
                add(Setting(name = "isConstantTime", value = isConstantTime))
            }

            internal companion object {

                @JvmSynthetic
                internal fun build(b: Builder): Default = ::Config.build(b, ::Default)

                @get:JvmSynthetic
                internal val DEFAULT: Config = Config(
                    isLenient = true,
                    lineBreakInterval = 64,
                    encodeLowercase = false,
                    padEncoded = true,
                    backFillBuffers = true,
                )
            }

            /**
             * DEPRECATED since `2.6.0`
             * @suppress
             * */
            @JvmField
            @Deprecated(
                message = "Variable name changed.",
                replaceWith = ReplaceWith("encodeLowercase"),
                level = DeprecationLevel.WARNING,
            )
            public val encodeToLowercase: Boolean = encodeLowercase
            /** @suppress */
            @JvmField
            public val isConstantTime: Boolean = true
        }

        /**
         * A static instance of [EncoderDecoder] configured with a [Default.Builder.lineBreak]
         * interval of `64`, and remaining [Default.Builder] `DEFAULT` values.
         * */
        public companion object: EncoderDecoder<Default.Config>(config = Default.Config.DEFAULT) {

            /**
             * Uppercase Base32 Default encoding characters.
             * */
            public const val CHARS_UPPER: String = "ABCDEFGHIJKLMNOPQRSTUVWXYZ234567"

            /**
             * Lowercase Base32 Default encoding characters.
             * */
            public const val CHARS_LOWER: String = "abcdefghijklmnopqrstuvwxyz234567"

            /**
             * Syntactic sugar for Kotlin consumers that like lambdas.
             * */
            @JvmStatic
            @JvmName("-Builder")
            @OptIn(ExperimentalContracts::class)
            public inline fun Builder(block: Builder.() -> Unit): Default {
                contract { callsInPlace(block, InvocationKind.EXACTLY_ONCE) }
                return Builder(other = null, block)
            }

            /**
             * Syntactic sugar for Kotlin consumers that like lambdas.
             * */
            @JvmStatic
            @JvmName("-Builder")
            @OptIn(ExperimentalContracts::class)
            public inline fun Builder(other: Default.Config?, block: Builder.() -> Unit): Default {
                contract { callsInPlace(block, InvocationKind.EXACTLY_ONCE) }
                return Builder(other).apply(block).build()
            }

            @get:JvmSynthetic
            internal val DELEGATE = Default(config, unused = null)
            protected override fun name(): String = DELEGATE.name()
            protected override fun newDecoderFeedProtected(out: Decoder.OutFeed): Decoder<Default.Config>.Feed {
                return DELEGATE.newDecoderFeedProtected(out)
            }
            protected override fun newEncoderFeedProtected(out: Encoder.OutFeed): Encoder<Default.Config>.Feed {
                return DELEGATE.newEncoderFeedProtected(out)
            }

            private const val NAME = "Base32.Default"
        }

        protected final override fun name(): String = NAME

        protected final override fun newDecoderFeedProtected(out: Decoder.OutFeed): Decoder<Default.Config>.Feed {
            return DefaultDecoderFeed(out)
        }

        protected final override fun newEncoderFeedProtected(out: Encoder.OutFeed): Encoder<Default.Config>.Feed {
            return if (config.encodeLowercase) {
                object : EncoderFeed(out) {
                    override fun Encoder.OutFeed.output1(i: Int) {
                        output(CHARS_LOWER[i])
                    }
                    override fun Encoder.OutFeed.outputPadding(n: Int) {
                        if (!config.padEncoded) return
                        val c = config.paddingChar ?: return
                        repeat(n) { output(c) }
                    }
                }
            } else {
                object : EncoderFeed(out) {
                    override fun Encoder.OutFeed.output1(i: Int) {
                        output(CHARS_UPPER[i])
                    }
                    override fun Encoder.OutFeed.outputPadding(n: Int) {
                        if (!config.padEncoded) return
                        val c = config.paddingChar ?: return
                        repeat(n) { output(c) }
                    }
                }
            }
        }

        /**
         * DEPRECATED since `2.6.0`
         * @suppress
         * */
        @Deprecated(
            message = "This constructor is scheduled for removal. Use Base32.Default.Builder or Base32.Default.Companion.Builder.",
            level = DeprecationLevel.WARNING,
        )
        public constructor(config: Config): this(config, unused = null)

        @Suppress("UNUSED_PARAMETER")
        private constructor(config: Config, unused: Any?): super(config)
    }

    /**
     * Base32 encoding/decoding in accordance with [RFC 4648 section 7](https://www.ietf.org/rfc/rfc4648.html#section-7)
     *
     * e.g.
     *
     *     val hex = Base32.Hex.Builder {
     *         isLenient(enable = true)
     *         lineBreak(interval = 64)
     *         encodeLowercase(enable = false)
     *         padEncoded(enable = true)
     *     }
     *
     *     val text = "Hello World!"
     *     val bytes = text.encodeToByteArray()
     *     val encoded = bytes.encodeToString(hex)
     *     println(encoded) // 91IMOR3F41BMUSJCCGGG====
     *
     *     // Alternatively, use the static implementation containing
     *     // pre-configured settings, instead of creating your own.
     *     val decoded = encoded.decodeToByteArray(Base32.Hex).decodeToString()
     *     assertEquals(text, decoded)
     *
     * @see [Builder]
     * @see [Companion.Builder]
     * @see [Encoder.Companion]
     * @see [Decoder.Companion]
     * */
    public class Hex: Base32<Hex.Config> {

        /**
         * A Builder
         *
         * @see [Companion.Builder]
         * */
        public class Builder {

            public constructor(): this(other = null)
            public constructor(other: Config?) {
                if (other == null) return
                this._isLenient = other.isLenient ?: true
                this._lineBreakInterval = other.lineBreakInterval
                this._encodeLowercase = other.encodeLowercase
                this._padEncoded = other.padEncoded
                this._backFillBuffers = other.backFillBuffers
            }

            @get:JvmSynthetic
            @set:JvmSynthetic
            internal var _isLenient: Boolean = true
            @get:JvmSynthetic
            @set:JvmSynthetic
            internal var _lineBreakInterval: Byte = 0
            @get:JvmSynthetic
            @set:JvmSynthetic
            internal var _encodeLowercase: Boolean = false
            @get:JvmSynthetic
            @set:JvmSynthetic
            internal var _padEncoded: Boolean = true
            @get:JvmSynthetic
            @set:JvmSynthetic
            internal var _backFillBuffers: Boolean = true

            /**
             * DEFAULT: `true`
             *
             * If `true`, the characters ('\n', '\r', ' ', '\t') will be skipped over (i.e.
             * allowed but ignored) during decoding operations. This is non-compliant with
             * `RFC 4648`.
             *
             * If `false`, an [EncodingException] will be thrown.
             * */
            public fun isLenient(enable: Boolean): Builder = apply { _isLenient = enable }

            /**
             * DEFAULT: `0` (i.e. disabled)
             *
             * If greater than `0`, when [interval] number of encoded characters have been
             * output, the next encoded character will be preceded with the new line character
             * `\n`. This is non-compliant with `RFC 4648`.
             *
             * A great value is `64`, and is what [Hex.Companion.config] uses.
             *
             * **NOTE:** This setting is ignored if [isLenient] is set to `false`.
             *
             * e.g.
             *
             *     isLenient(enable = true)
             *     lineBreak(interval = 0)
             *     // 91IMOR3F41BMUSJCCGGG====
             *
             *     isLenient(enable = true)
             *     lineBreak(interval = 16)
             *     // 91IMOR3F41BMUSJC
             *     // CGGG====
             *
             *     isLenient(enable = false)
             *     lineBreak(interval = 16)
             *     // 91IMOR3F41BMUSJCCGGG====
             *
             * @see [EncoderDecoder.Config.lineBreakInterval]
             * */
            public fun lineBreak(interval: Byte): Builder = apply { _lineBreakInterval = interval }

            /**
             * DEFAULT: `false`
             *
             * If `true`, lowercase characters from table [Hex.CHARS_LOWER] will be output
             * during encoding operations. This is non-compliant with `RFC 4648`.
             *
             * If `false`, uppercase characters from table [Hex.CHARS_UPPER] will be output
             * during encoding operations.
             *
             * **NOTE:** This does not affect decoding operations. [Hex] is designed to accept
             * characters from both tables when decoding (as specified in `RFC 4648`).
             * */
            public fun encodeLowercase(enable: Boolean): Builder = apply { _encodeLowercase = enable }

            /**
             * DEFAULT: `true`
             *
             * If `true`, encoded output will have the appropriate number of padding character(s)
             * `=` appended to it.
             *
             * If `false`, padding character(s) will be omitted from output. This is non-compliant
             * with `RFC 4648`.
             * */
            public fun padEncoded(enable: Boolean): Builder = apply { _padEncoded = enable }

            /**
             * DEFAULT: `true`
             *
             * @see [EncoderDecoder.Config.backFillBuffers]
             * */
            public fun backFillBuffers(enable: Boolean): Builder = apply { _backFillBuffers = enable }

            /**
             * Helper for configuring the builder with settings which are compliant with the
             * `RFC 4648` specification.
             *
             *  - [isLenient] will be set to `false`.
             *  - [lineBreak] will be set to `0`.
             *  - [encodeLowercase] will be set to `false`.
             *  - [padEncoded] will be set to `true`.
             * */
            public fun strictSpec(): Builder = apply {
                _isLenient = false
                _lineBreakInterval = 0
                _encodeLowercase = false
                _padEncoded = true
            }

            /**
             * Commits configured options to [Config], creating the [Hex] instance.
             * */
            public fun build(): Hex = Config.build(this)
        }

        /**
         * Holder of a configuration for the [Hex] encoder/decoder instance.
         *
         * @see [Builder]
         * @see [Companion.Builder]
         * */
        public class Config private constructor(
            isLenient: Boolean,
            lineBreakInterval: Byte,
            @JvmField
            public val encodeLowercase: Boolean,
            @JvmField
            public val padEncoded: Boolean,
            backFillBuffers: Boolean,
        ): EncoderDecoder.Config(
            isLenient,
            lineBreakInterval,
            lineBreakResetOnFlush = true, // TODO
            paddingChar = '=',
            maxDecodeEmit = 5,
            backFillBuffers,
        ) {

            protected override fun decodeOutMaxSizeProtected(encodedSize: Long): Long {
                return decodeOutMaxSize64(encodedSize)
            }

            protected override fun decodeOutMaxSizeOrFailProtected(encodedSize: Int, input: DecoderInput): Int {
                return decodeOutMaxSize32(encodedSize)
            }

            protected override fun encodeOutSizeProtected(unEncodedSize: Long): Long {
                return encodeOutSize64(unEncodedSize, willBePadded = padEncoded)
            }

            protected override fun toStringAddSettings(): Set<Setting> = buildSet(capacity = 3) {
                add(Setting(name = "encodeLowercase", value = encodeLowercase))
                add(Setting(name = "padEncoded", value = padEncoded))
                add(Setting(name = "isConstantTime", value = isConstantTime))
            }

            internal companion object {

                @JvmSynthetic
                internal fun build(b: Builder): Hex = ::Config.build(b, ::Hex)

                @get:JvmSynthetic
                internal val DEFAULT: Config = Config(
                    isLenient = true,
                    lineBreakInterval = 64,
                    encodeLowercase = false,
                    padEncoded = true,
                    backFillBuffers = true,
                )
            }

            /**
             * DEPRECATED since `2.6.0`
             * @suppress
             * */
            @JvmField
            @Deprecated(
                message = "Variable name changed.",
                replaceWith = ReplaceWith("encodeLowercase"),
                level = DeprecationLevel.WARNING,
            )
            public val encodeToLowercase: Boolean = encodeLowercase
            /** @suppress */
            @JvmField
            public val isConstantTime: Boolean = true
        }

        /**
         * A static instance of [EncoderDecoder] configured with a [Hex.Builder.lineBreak]
         * interval of `64`, and remaining [Hex.Builder] `DEFAULT` values.
         * */
        public companion object: EncoderDecoder<Hex.Config>(config = Hex.Config.DEFAULT) {

            /**
             * Uppercase Base32 Hex encoding characters.
             * */
            public const val CHARS_UPPER: String = "0123456789ABCDEFGHIJKLMNOPQRSTUV"

            /**
             * Lowercase Base32 Hex encoding characters.
             * */
            public const val CHARS_LOWER: String = "0123456789abcdefghijklmnopqrstuv"

            /**
             * Syntactic sugar for Kotlin consumers that like lambdas.
             * */
            @JvmStatic
            @JvmName("-Builder")
            @OptIn(ExperimentalContracts::class)
            public inline fun Builder(block: Builder.() -> Unit): Hex {
                contract { callsInPlace(block, InvocationKind.EXACTLY_ONCE) }
                return Builder(other = null, block)
            }

            /**
             * Syntactic sugar for Kotlin consumers that like lambdas.
             * */
            @JvmStatic
            @JvmName("-Builder")
            @OptIn(ExperimentalContracts::class)
            public inline fun Builder(other: Hex.Config?, block: Builder.() -> Unit): Hex {
                contract { callsInPlace(block, InvocationKind.EXACTLY_ONCE) }
                return Builder(other).apply(block).build()
            }

            @get:JvmSynthetic
            internal val DELEGATE = Hex(config, unused = null)
            override fun name(): String = DELEGATE.name()
            override fun newDecoderFeedProtected(out: Decoder.OutFeed): Decoder<Hex.Config>.Feed {
                return DELEGATE.newDecoderFeedProtected(out)
            }
            override fun newEncoderFeedProtected(out: Encoder.OutFeed): Encoder<Hex.Config>.Feed {
                return DELEGATE.newEncoderFeedProtected(out)
            }

            private const val NAME = "Base32.Hex"
        }

        protected final override fun name(): String = NAME

        protected final override fun newDecoderFeedProtected(out: Decoder.OutFeed): Decoder<Hex.Config>.Feed {
            return HexDecoderFeed(out)
        }

        protected final override fun newEncoderFeedProtected(out: Encoder.OutFeed): Encoder<Hex.Config>.Feed {
            return if (config.encodeLowercase) {
                object : EncoderFeed(out) {
                    override fun Encoder.OutFeed.output1(i: Int) {
                        output(CHARS_LOWER[i])
                    }
                    override fun Encoder.OutFeed.outputPadding(n: Int) {
                        if (!config.padEncoded) return
                        val c = config.paddingChar ?: return
                        repeat(n) { output(c) }
                    }
                }
            } else {
                object : EncoderFeed(out) {
                    override fun Encoder.OutFeed.output1(i: Int) {
                        output(CHARS_UPPER[i])
                    }
                    override fun Encoder.OutFeed.outputPadding(n: Int) {
                        if (!config.padEncoded) return
                        val c = config.paddingChar ?: return
                        repeat(n) { output(c) }
                    }
                }
            }
        }

        /**
         * DEPRECATED since `2.6.0`
         * @suppress
         * */
        @Deprecated(
            message = "This constructor is scheduled for removal. Use Base32.Hex.Builder or Base32.Hex.Companion.Builder.",
            level = DeprecationLevel.WARNING,
        )
        public constructor(config: Config): this(config, unused = null)

        @Suppress("UNUSED_PARAMETER")
        private constructor(config: Config, unused: Any?): super(config)
    }

    private abstract inner class AbstractDecoderFeed(out: Decoder.OutFeed): Decoder<C>.Feed(_out = out) {

        protected abstract fun Int.decodeDiff(): Int

        private val buf = IntArray(7)
        private var iBuf = 0

        override fun consumeProtected(input: Char) {
            val code = input.code
            val diff = code.decodeDiff()
            if (diff == 0) {
                throw Diff0EncodingException("Char[$input] is not a valid Base32 character")
            }

            if (iBuf < 7) {
                buf[iBuf++] = code + diff
                return // Await more input
            }

            // Append each character's 5 bits to the word
            var word: Long = buf[0].toLong()
            word = word shl 5 or buf[1].toLong()
            word = word shl 5 or buf[2].toLong()
            word = word shl 5 or buf[3].toLong()
            word = word shl 5 or buf[4].toLong()
            word = word shl 5 or buf[5].toLong()
            word = word shl 5 or buf[6].toLong()
            word = word shl 5 or (code + diff).toLong()
            iBuf = 0

            // For every 8 characters of input, 40 bits of output are accumulated. Emit 5 bytes.
            _out.output((word shr 32).toByte())
            _out.output((word shr 24).toByte())
            _out.output((word shr 16).toByte())
            _out.output((word shr  8).toByte())
            _out.output((word       ).toByte())
        }

        override fun doFinalProtected() {
            if (iBuf == 0) return buf.fill(0)

            if (iBuf == 1) {
                // 5*1 =  5 bits. Truncated, fail.
                iBuf = 0
                buf.fill(0)
                throw FeedBuffer.truncatedInputEncodingException(1)
            }
            if (iBuf == 3) {
                // 5*3 = 15 bits. Truncated, fail.
                iBuf = 0
                buf.fill(0)
                throw FeedBuffer.truncatedInputEncodingException(3)
            }
            if (iBuf == 6) {
                // 5*6 = 30 bits. Truncated, fail.
                iBuf = 0
                buf.fill(0)
                throw FeedBuffer.truncatedInputEncodingException(6)
            }

            // iBuf == 2, 4, 5 or 7
            // Append each character's 5 bits to the word
            var word: Long = buf[0].toLong()
            word = word shl 5 or buf[1].toLong()
            if (iBuf == 2) {
                iBuf = 0
                buf.fill(0)
                // 5*2 = 10 bits. Drop 2
                word = word shr 2

                // 8/8 = 1 byte
                _out.output((word       ).toByte())
                return
            }

            word = word shl 5 or buf[2].toLong()
            word = word shl 5 or buf[3].toLong()
            if (iBuf == 4) {
                iBuf = 0
                buf.fill(0)
                // 5*4 = 20 bits. Drop 4
                word = word shr 4

                // 16/8 = 2 bytes
                _out.output((word shr  8).toByte())
                _out.output((word       ).toByte())
                return
            }

            word = word shl 5 or buf[4].toLong()
            if (iBuf == 5) {
                iBuf = 0
                buf.fill(0)
                // 5*5 = 25 bits. Drop 1
                word = word shr 1

                // 24/8 = 3 bytes
                _out.output((word shr 16).toByte())
                _out.output((word shr  8).toByte())
                _out.output((word       ).toByte())
                return
            }

            word = word shl 5 or buf[5].toLong()
            word = word shl 5 or buf[6].toLong()
            if (iBuf == 7) {
                iBuf = 0
                buf.fill(0)
                // 5*7 = 35 bits. Drop 3
                word = word shr 3

                // 32/8 = 4 bytes
                _out.output((word shr 24).toByte())
                _out.output((word shr 16).toByte())
                _out.output((word shr  8).toByte())
                _out.output((word       ).toByte())
                return
            }

            // "Should" never make it here
            error("Illegal configuration >> iBuf[$iBuf] - buf[${buf[0]}, ${buf[1]}, ${buf[2]}, ${buf[3]}, ${buf[4]}, ${buf[5]}, ${buf[6]}]")
        }
    }

    private abstract inner class EncoderFeed(out: Encoder.OutFeed): Encoder<C>.Feed(_out = out) {

        protected abstract fun Encoder.OutFeed.output1(i: Int)
        protected abstract fun Encoder.OutFeed.outputPadding(n: Int)

        private val buf = ByteArray(4)
        private var iBuf = 0

        final override fun consumeProtected(input: Byte) {
            if (iBuf < 4) {
                buf[iBuf++] = input
                return // Await more input
            }

            // Append each character's 8 bits to the word
            var word: Long = buf[0].toBits()
            word = (word shl 8) + buf[1].toBits()
            word = (word shl 8) + buf[2].toBits()
            word = (word shl 8) + buf[3].toBits()
            word = (word shl 8) + input.toBits()
            iBuf = 0

            // For every 5 bytes of input, 40 bits of output are accumulated. Emit 8 characters.
            _out.output1(i = (word shr 35 and 0x1fL).toInt()) // 40-1*5 = 35
            _out.output1(i = (word shr 30 and 0x1fL).toInt()) // 40-2*5 = 30
            _out.output1(i = (word shr 25 and 0x1fL).toInt()) // 40-3*5 = 25
            _out.output1(i = (word shr 20 and 0x1fL).toInt()) // 40-4*5 = 20
            _out.output1(i = (word shr 15 and 0x1fL).toInt()) // 40-5*5 = 15
            _out.output1(i = (word shr 10 and 0x1fL).toInt()) // 40-6*5 = 10
            _out.output1(i = (word shr  5 and 0x1fL).toInt()) // 40-7*5 =  5
            _out.output1(i = (word        and 0x1fL).toInt()) // 40-8*5 =  0
        }

        final override fun doFinalProtected() {
            if (iBuf == 0) {
                buf.fill(0)
                // Still call with 0 b/c Crockford uses to append its check symbol
                return _out.outputPadding(n = 0)
            }

            var word: Long = buf[0].toBits()
            if (iBuf == 1) {
                iBuf = 0
                buf.fill(0)
                // 8*1 = 8 bits
                _out.output1(i = (word shr  3 and 0x1fL).toInt()) //  8-1*5 =  3
                _out.output1(i = (word shl  2 and 0x1fL).toInt()) //  5-3   =  2
                return _out.outputPadding(n = 6)
            }

            word = (word shl 8) + buf[1].toBits()
            if (iBuf == 2) {
                iBuf = 0
                buf.fill(0)
                // 8*2 = 16 bits
                _out.output1(i = (word shr 11 and 0x1fL).toInt()) // 16-1*5 = 11
                _out.output1(i = (word shr  6 and 0x1fL).toInt()) // 16-2*5 =  6
                _out.output1(i = (word shr  1 and 0x1fL).toInt()) // 16-3*5 =  1
                _out.output1(i = (word shl  4 and 0x1fL).toInt()) //  5-1   =  4
                return _out.outputPadding(n = 4)
            }

            word = (word shl 8) + buf[2].toBits()
            if (iBuf == 3) {
                iBuf = 0
                buf.fill(0)
                // 8*3 = 24 bits
                _out.output1(i = (word shr 19 and 0x1fL).toInt()) // 24-1*5 = 19
                _out.output1(i = (word shr 14 and 0x1fL).toInt()) // 24-2*5 = 14
                _out.output1(i = (word shr  9 and 0x1fL).toInt()) // 24-3*5 =  9
                _out.output1(i = (word shr  4 and 0x1fL).toInt()) // 24-4*5 =  4
                _out.output1(i = (word shl  1 and 0x1fL).toInt()) //  5-4   =  1
                return _out.outputPadding(n = 3)
            }

            word = (word shl 8) + buf[3].toBits()
            if (iBuf == 4) {
                iBuf = 0
                buf.fill(0)
                // 8*4 = 32 bits
                _out.output1(i = (word shr 27 and 0x1fL).toInt()) // 32-1*5 = 27
                _out.output1(i = (word shr 22 and 0x1fL).toInt()) // 32-2*5 = 22
                _out.output1(i = (word shr 17 and 0x1fL).toInt()) // 32-3*5 = 17
                _out.output1(i = (word shr 12 and 0x1fL).toInt()) // 32-4*5 = 12
                _out.output1(i = (word shr  7 and 0x1fL).toInt()) // 32-5*5 =  7
                _out.output1(i = (word shr  2 and 0x1fL).toInt()) // 32-6*5 =  2
                _out.output1(i = (word shl  3 and 0x1fL).toInt()) //  5-2   =  3
                return _out.outputPadding(n = 1)
            }

            // "Should" never make it here
            error("Illegal configuration >> iBuf[$iBuf] - buf[${buf[0]}, ${buf[1]}, ${buf[2]}, ${buf[3]}]")
        }

        private inline fun Byte.toBits(): Long = if (this < 0) this + 256L else toLong()
    }

    private inner class CrockfordDecoderFeed(out: Decoder.OutFeed): AbstractDecoderFeed(out) {

        private val _config = config as Crockford.Config
        private var hadInput = false
        private var isCheckSymbolSet = false

        override fun Int.decodeDiff(): Int {
            val ge0: Byte = if (this >= '0'.code) 1 else 0
            val le9: Byte = if (this <= '9'.code) 1 else 0

            val geA: Byte = if (this >= 'A'.code) 1 else 0
            val leH: Byte = if (this <= 'H'.code) 1 else 0
            val eqI: Byte = if (this == 'I'.code) 1 else 0
            val eqL: Byte = if (this == 'L'.code) 1 else 0
            val eqJ: Byte = if (this == 'J'.code) 1 else 0
            val eqK: Byte = if (this == 'K'.code) 1 else 0
            val eqM: Byte = if (this == 'M'.code) 1 else 0
            val eqN: Byte = if (this == 'N'.code) 1 else 0
            val eqO: Byte = if (this == 'O'.code) 1 else 0
            val geP: Byte = if (this >= 'P'.code) 1 else 0
            val leT: Byte = if (this <= 'T'.code) 1 else 0
            val geV: Byte = if (this >= 'V'.code) 1 else 0
            val leZ: Byte = if (this <= 'Z'.code) 1 else 0

            val gea: Byte = if (this >= 'a'.code) 1 else 0
            val leh: Byte = if (this <= 'h'.code) 1 else 0
            val eqi: Byte = if (this == 'i'.code) 1 else 0
            val eql: Byte = if (this == 'l'.code) 1 else 0
            val eqj: Byte = if (this == 'j'.code) 1 else 0
            val eqk: Byte = if (this == 'k'.code) 1 else 0
            val eqm: Byte = if (this == 'm'.code) 1 else 0
            val eqn: Byte = if (this == 'n'.code) 1 else 0
            val eqo: Byte = if (this == 'o'.code) 1 else 0
            val gep: Byte = if (this >= 'p'.code) 1 else 0
            val let: Byte = if (this <= 't'.code) 1 else 0
            val gev: Byte = if (this >= 'v'.code) 1 else 0
            val lez: Byte = if (this <= 'z'.code) 1 else 0

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
            val h = 1 - this
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
            val k = 0 - this
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

            return diff
        }

        override fun consumeProtected(input: Char) {
            if (isCheckSymbolSet) {
                // If the set checkSymbol was not intended, it's only valid as the
                // very last character and the previous update call was invalid.
                throw EncodingException("CheckSymbol[${_config.checkSymbol}] was set")
            }

            // Crockford allows for insertion of hyphens, which are to be ignored.
            if (input == '-') return

            try {
                super.consumeProtected(input)
                hadInput = true
            } catch (e: Diff0EncodingException) {
                // decodeDiff returned 0. See if it's a check symbol.
                if (!input.isCheckSymbol()) throw e

                // Have a check symbol
                if (_config.checkSymbol?.uppercaseChar() == input.uppercaseChar()) {
                    isCheckSymbolSet = true
                    return
                }

                // Have the wrong check symbol
                throw EncodingException("Char[$input] IS a check symbol, but did not match config's CheckSymbol[${_config.checkSymbol}]", e)
            }
        }

        override fun doFinalProtected() {
            if (isClosed() || _config.finalizeWhenFlushed) {
                if (hadInput && _config.checkSymbol != null && !isCheckSymbolSet) {
                    throw EncodingException("Missing check symbol. Expected[${_config.checkSymbol}]")
                }
                isCheckSymbolSet = false
                hadInput = false
            }
            super.doFinalProtected()
        }
    }

    private inner class DefaultDecoderFeed(out: Decoder.OutFeed): AbstractDecoderFeed(out) {
        override fun Int.decodeDiff(): Int {
            val ge2: Byte = if (this >= '2'.code) 1 else 0
            val le7: Byte = if (this <= '7'.code) 1 else 0
            val geA: Byte = if (this >= 'A'.code) 1 else 0
            val leZ: Byte = if (this <= 'Z'.code) 1 else 0
            val gea: Byte = if (this >= 'a'.code) 1 else 0
            val lez: Byte = if (this <= 'z'.code) 1 else 0

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

            return diff
        }
    }

    private inner class HexDecoderFeed(out: Decoder.OutFeed): AbstractDecoderFeed(out) {
        override fun Int.decodeDiff(): Int {
            val ge0: Byte = if (this >= '0'.code) 1 else 0
            val le9: Byte = if (this <= '9'.code) 1 else 0
            val geA: Byte = if (this >= 'A'.code) 1 else 0
            val leV: Byte = if (this <= 'V'.code) 1 else 0
            val gea: Byte = if (this >= 'a'.code) 1 else 0
            val lev: Byte = if (this <= 'v'.code) 1 else 0

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

            return diff
        }
    }

    // Thrown by AbstractDecoderFeed when decodeDiff returns 0.
    // Is for Crockford in order to check input for a check symbol.
    private class Diff0EncodingException(message: String): EncodingException(message)

    private class HyphenOutFeed(
        private val interval: Byte,
        private val out: Encoder.OutFeed,
    ): Encoder.OutFeed {

        init {
            require(interval > 0) { "interval must be greater than 0" }
            require(out !is HyphenOutFeed) { "out cannot be an instance of HyphenOutFeed" }
        }

        private var count: Byte = 0

        fun reset() {
            count = 0
            // TODO: Check resetOnFlush
            if (out is LineBreakOutFeed) out.reset()
        }

        override fun output(encoded: Char) {
            if (count == interval) {
                out.output('-')
                count = 0
            }
            out.output(encoded)
            count++
        }
    }
}
