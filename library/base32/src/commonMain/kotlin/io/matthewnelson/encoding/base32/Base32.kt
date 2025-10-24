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
@file:Suppress("FunctionName", "LocalVariableName", "PropertyName", "RedundantModalityModifier", "RedundantVisibilityModifier", "RemoveRedundantQualifierName")

package io.matthewnelson.encoding.base32

import io.matthewnelson.encoding.base32.internal.build
import io.matthewnelson.encoding.base32.internal.decodeOutMaxSize
import io.matthewnelson.encoding.base32.internal.encodeOutSize
import io.matthewnelson.encoding.base32.internal.isCheckSymbol
import io.matthewnelson.encoding.base32.internal.toBits
import io.matthewnelson.encoding.core.Decoder
import io.matthewnelson.encoding.core.Encoder
import io.matthewnelson.encoding.core.EncoderDecoder
import io.matthewnelson.encoding.core.EncodingException
import io.matthewnelson.encoding.core.util.DecoderInput
import io.matthewnelson.encoding.core.util.FeedBuffer
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
                this._finalizeOnFlush = other.finalizeOnFlush
            }

            @JvmSynthetic
            internal var _isLenient: Boolean = true
            @JvmSynthetic
            internal var _encodeLowercase: Boolean = false
            @JvmSynthetic
            internal var _hyphenInterval: Byte = 0
            @JvmSynthetic
            internal var _checkSymbol: Char? = null
            @JvmSynthetic
            internal var _finalizeOnFlush: Boolean = false

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
             *
             * @see [finalizeOnFlush]
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
             * @see [finalizeOnFlush]
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
             * DEFAULT: `false`
             *
             * If `true`, whenever [Encoder.Feed.flush] is called, in addition to processing
             * any buffered input, the [check] symbol will be appended and counter for [hyphen]
             * interval will be reset.
             *
             * If `false`, whenever [Encoder.Feed.flush] is called, only processing of buffered
             * input will occur; no [check] symbol will be appended, and the counter for [hyphen]
             * interval will not be reset.
             *
             * **NOTE:** This setting is ignored if neither [hyphen] interval nor [check] symbol
             * are configured.
             *
             * e.g. (Behavior when `true`)
             *
             *     val sb = StringBuilder()
             *     Base32.Crockford.Builder {
             *         hyphen(interval = 4)
             *         check(symbol = '*')
             *         finalizeOnFlush(enable = true)
             *     }.newEncoderFeed { encodedChar ->
             *         sb.append(encodedChar)
             *     }.use { feed ->
             *         bytes1.forEach { b -> feed.consume(b) }
             *         feed.flush()
             *         bytes2.forEach { b -> feed.consume(b) }
             *     }
             *     println(sb.toString())
             *     // 91JP-RV3F-*41BP-YWKC-CGGG-*
             *
             * e.g. (Behavior when `false`)
             *
             *     val sb = StringBuilder()
             *     Base32.Crockford.Builder {
             *         hyphen(interval = 4)
             *         check(symbol = '*')
             *         finalizeOnFlush(enable = false)
             *     }.newEncoderFeed { encodedChar ->
             *         sb.append(encodedChar)
             *     }.use { feed ->
             *         bytes1.forEach { b -> feed.consume(b) }
             *         feed.flush()
             *         bytes2.forEach { b -> feed.consume(b) }
             *     }
             *     println(sb.toString())
             *     // 91JP-RV3F-41BP-YWKC-CGGG-*
             * */
            public fun finalizeOnFlush(enable: Boolean): Builder = apply { _finalizeOnFlush = enable }

            /**
             * Helper for configuring the builder with settings which are compliant with the
             * Crockford specification.
             *
             *  - [isLenient] will be set to `false`.
             *  - [encodeLowercase] will be set to `false`.
             *  - [finalizeOnFlush] will be set to `false`.
             * */
            public fun strictSpec(): Builder = apply {
                _isLenient = false
                _encodeLowercase = false
                _finalizeOnFlush = false
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
            @JvmField
            public val finalizeOnFlush: Boolean,
        ): EncoderDecoder.Config(isLenient, 0, null) {

            protected override fun decodeOutMaxSizeProtected(encodedSize: Long): Long {
                // TODO: Check for overflow?
                return encodedSize.decodeOutMaxSize()
            }

            protected override fun decodeOutMaxSizeOrFailProtected(encodedSize: Int, input: DecoderInput): Int {
                var outSize = encodedSize

                val actual = input[encodedSize - 1]

                if (checkSymbol != null) {
                    // Uppercase them so that little 'u' is always compared as big 'U'.
                    val expectedUpper = checkSymbol.uppercaseChar()
                    val actualUpper = actual.uppercaseChar()

                    if (actualUpper != expectedUpper) {
                        // Wrong, or no symbol
                        if (actual.isCheckSymbol()) {
                            throw EncodingException(
                                "Check symbol did not match. Expected[$checkSymbol] vs Actual[$actual]"
                            )
                        } else {
                            throw EncodingException(
                                "Check symbol not found. Expected[$checkSymbol]"
                            )
                        }
                    } else {
                        outSize--
                    }
                } else {
                    // Mine as well check it here before actually decoding.
                    if (actual.isCheckSymbol()) {
                        throw EncodingException(
                            "Decoder Misconfiguration.\n" +
                            "Encoded data has CheckSymbol[$actual], but the " +
                            "decoder is configured to reject check symbols."
                        )
                    }
                }

                // TODO: Check for overflow?
                return outSize.toLong().decodeOutMaxSize().toInt()
            }

            protected override fun encodeOutSizeProtected(unEncodedSize: Long): Long {
                // TODO: Check for overflow?
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

                // TODO: Check for overflow?
                return outSize
            }

            protected override fun toStringAddSettings(): Set<Setting> = buildSet {
                add(Setting(name = "encodeLowercase", value = encodeLowercase))
                add(Setting(name = "hyphenInterval", value = hyphenInterval))
                add(Setting(name = "checkSymbol", value = checkSymbol))
                add(Setting(name = "finalizeOnFlush", value = finalizeOnFlush))
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
                    finalizeOnFlush = false,
                )
            }

            /** @suppress */
            @JvmField
            @Deprecated("Variable name changed.", ReplaceWith("finalizeOnFlush"))
            public val finalizeWhenFlushed: Boolean = finalizeOnFlush
            /** @suppress */
            @JvmField
            @Deprecated("Variable name changed.", ReplaceWith("encodeLowercase"))
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
            return CrockfordDecoder(config, out)
        }

        protected final override fun newEncoderFeedProtected(out: Encoder.OutFeed): Encoder<Crockford.Config>.Feed {
            return CrockfordEncoder(config, out)
        }

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
            }

            @JvmSynthetic
            internal var _isLenient: Boolean = true
            @JvmSynthetic
            internal var _lineBreakInterval: Byte = 0
            @JvmSynthetic
            internal var _encodeLowercase: Boolean = false
            @JvmSynthetic
            internal var _padEncoded: Boolean = true

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
        ): EncoderDecoder.Config(isLenient, lineBreakInterval, '=') {

            protected override fun decodeOutMaxSizeProtected(encodedSize: Long): Long {
                // TODO: Check for overflow?
                return encodedSize.decodeOutMaxSize()
            }

            protected override fun decodeOutMaxSizeOrFailProtected(encodedSize: Int, input: DecoderInput): Int {
                // TODO: Check for overflow?
                return encodedSize.toLong().decodeOutMaxSize().toInt()
            }

            protected override fun encodeOutSizeProtected(unEncodedSize: Long): Long {
                // TODO: Check for overflow?
                return unEncodedSize.encodeOutSize(willBePadded = padEncoded)
            }

            protected override fun toStringAddSettings(): Set<Setting> = buildSet {
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
                )
            }

            /** @suppress */
            @JvmField
            @Deprecated("Variable name changed.", ReplaceWith("encodeLowercase"))
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
            return DefaultDecoder(out)
        }

        protected final override fun newEncoderFeedProtected(out: Encoder.OutFeed): Encoder<Default.Config>.Feed {
            return DefaultEncoder(config, out)
        }

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
            }

            @JvmSynthetic
            internal var _isLenient: Boolean = true
            @JvmSynthetic
            internal var _lineBreakInterval: Byte = 0
            @JvmSynthetic
            internal var _encodeLowercase: Boolean = false
            @JvmSynthetic
            internal var _padEncoded: Boolean = true

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
        ): EncoderDecoder.Config(isLenient, lineBreakInterval, '=') {

            protected override fun decodeOutMaxSizeProtected(encodedSize: Long): Long {
                // TODO: Check for overflow?
                return encodedSize.decodeOutMaxSize()
            }

            protected override fun decodeOutMaxSizeOrFailProtected(encodedSize: Int, input: DecoderInput): Int {
                // TODO: Check for overflow?
                return encodedSize.toLong().decodeOutMaxSize().toInt()
            }

            protected override fun encodeOutSizeProtected(unEncodedSize: Long): Long {
                // TODO: Check for overflow?
                return unEncodedSize.encodeOutSize(willBePadded = padEncoded)
            }

            protected override fun toStringAddSettings(): Set<Setting> = buildSet {
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
                )
            }

            /** @suppress */
            @JvmField
            @Deprecated("Variable name changed.", ReplaceWith("encodeLowercase"))
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
            return HexDecoder(out)
        }

        protected final override fun newEncoderFeedProtected(out: Encoder.OutFeed): Encoder<Hex.Config>.Feed {
            return HexEncoder(config, out)
        }

        @Deprecated(
            message = "This constructor is scheduled for removal. Use Base32.Hex.Builder or Base32.Hex.Companion.Builder.",
            level = DeprecationLevel.WARNING,
        )
        public constructor(config: Config): this(config, unused = null)

        @Suppress("UNUSED_PARAMETER")
        private constructor(config: Config, unused: Any?): super(config)
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

    private inner class CrockfordDecoder(
        private val _config: Crockford.Config,
        out: Decoder.OutFeed,
    ): Decoder<C>.Feed() {

        private var isCheckSymbolSet = false
        private val buffer = DecodingBuffer(out)

        override fun consumeProtected(input: Char) {
            if (isCheckSymbolSet) {
                // If the set checkByte was not intended, it's only valid as the
                // very last character and the previous update call was invalid.
                throw EncodingException("CheckSymbol[${_config.checkSymbol}] was set")
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

            if (_config.checkSymbol?.uppercaseChar() == input.uppercaseChar()) {
                isCheckSymbolSet = true
                return
            }

            throw EncodingException(
                "Char[${input}] IS a check symbol, but did not" +
                " match config's CheckSymbol[${_config.checkSymbol}]"
            )
        }

        override fun doFinalProtected() {
            // TODO: If _config.checkSymbol is not null, check
            //  isCheckSymbolSet and fail if it is not. (Issue #175)
            buffer.finalize()
            isCheckSymbolSet = false
        }
    }

    private inner class CrockfordEncoder(
        private val _config: Crockford.Config,
        private val out: Encoder.OutFeed,
    ): Encoder<C>.Feed() {

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
                outputHyphenOnNext = _config.hyphenInterval > 0 && ++outCount == _config.hyphenInterval
            },
            table = if (_config.encodeLowercase) Crockford.CHARS_LOWER else Crockford.CHARS_UPPER,
            paddingChar = null,
        )

        override fun consumeProtected(input: Byte) { buffer.update(input.toInt()) }

        override fun doFinalProtected() {
            buffer.finalize()

            if (_config.finalizeOnFlush || isClosed()) {
                _config.checkSymbol?.let { symbol ->

                    if (outputHyphenOnNext) {
                        out.output('-')
                    }

                    if (_config.encodeLowercase) {
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

    private inner class DefaultDecoder(out: Decoder.OutFeed): Decoder<C>.Feed() {

        private val buffer = DecodingBuffer(out)

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

        override fun doFinalProtected() { buffer.finalize() }
    }

    private inner class DefaultEncoder(_config: Default.Config, out: Encoder.OutFeed): Encoder<C>.Feed() {

        private val buffer = EncodingBuffer(
            out = out,
            table = if (_config.encodeLowercase) Default.CHARS_LOWER else Default.CHARS_UPPER,
            paddingChar = if (_config.padEncoded) _config.paddingChar else null,
        )

        override fun consumeProtected(input: Byte) { buffer.update(input.toInt()) }

        override fun doFinalProtected() { buffer.finalize() }
    }

    private inner class HexDecoder(out: Decoder.OutFeed): Decoder<C>.Feed() {

        private val buffer = DecodingBuffer(out)

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

        override fun doFinalProtected() { buffer.finalize() }
    }

    private inner class HexEncoder(_config: Hex.Config, out: Encoder.OutFeed): Encoder<C>.Feed() {

        private val buffer = EncodingBuffer(
            out = out,
            table = if (_config.encodeLowercase) Hex.CHARS_LOWER else Hex.CHARS_UPPER,
            paddingChar = if (_config.padEncoded) _config.paddingChar else null,
        )

        override fun consumeProtected(input: Byte) { buffer.update(input.toInt()) }

        override fun doFinalProtected() { buffer.finalize() }
    }
}
