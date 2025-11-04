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
@file:Suppress("FunctionName", "PropertyName", "RedundantModalityModifier", "RedundantVisibilityModifier", "RemoveRedundantQualifierName")

package io.matthewnelson.encoding.base16

import io.matthewnelson.encoding.base16.internal.build
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
 * Base16 (aka "hex") encoding/decoding in accordance with [RFC 4648 section 8](https://www.ietf.org/rfc/rfc4648.html#section-8).
 *
 * e.g.
 *
 *     val base16 = Base16.Builder {
 *         isLenient(enable = true)
 *         lineBreak(interval = 64)
 *         encodeLowercase(enable = true)
 *     }
 *
 *     val text = "Hello World!"
 *     val bytes = text.encodeToByteArray()
 *     val encoded = bytes.encodeToString(base16)
 *     println(encoded) // 48656c6c6f20576f726c6421
 *
 *     // Alternatively, use the static implementation containing
 *     // pre-configured settings, instead of creating your own.
 *     val decoded = encoded.decodeToByteArray(Base16).decodeToString()
 *     assertEquals(text, decoded)
 *
 * @see [Builder]
 * @see [Companion.Builder]
 * @see [Encoder.Companion]
 * @see [Decoder.Companion]
 * */
public class Base16: EncoderDecoder<Base16.Config> {

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
         * A great value is `64`, and is what [Base16.Companion.config] uses.
         *
         * **NOTE:** This setting is ignored if [isLenient] is set to `false`.
         *
         * e.g.
         *
         *     isLenient(enable = true)
         *     lineBreak(interval = 0)
         *     // 48656C6C6F20576F726C6421
         *
         *     isLenient(enable = true)
         *     lineBreak(interval = 16)
         *     // 48656C6C6F20576F
         *     // 726C6421
         *
         *     isLenient(enable = false)
         *     lineBreak(interval = 16)
         *     // 48656C6C6F20576F726C6421
         * */
        public fun lineBreak(interval: Byte): Builder = apply { _lineBreakInterval = interval }

        /**
         * DEFAULT: `false`
         *
         * If `true`, lowercase characters from table [Base16.CHARS_LOWER] will be output
         * during encoding operations. This is non-compliant with `RFC 4648`.
         *
         * If `false`, uppercase characters from table [Base16.CHARS_UPPER] will be output
         * during encoding operations.
         *
         * **NOTE:** This does not affect decoding operations. [Base16] is designed to accept
         * characters from both tables when decoding (as specified in `RFC 4648`).
         * */
        public fun encodeLowercase(enable: Boolean): Builder = apply { _encodeLowercase = enable }

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
         * */
        public fun strictSpec(): Builder = apply {
            _isLenient = false
            _lineBreakInterval = 0
            _encodeLowercase = false
        }

        /**
         * Commits configured options to [Config], creating the [Base16] instance.
         * */
        public fun build(): Base16 = Config.build(this)
    }

    /**
     * Holder of a configuration for the [Base16] encoder/decoder instance.
     *
     * @see [Builder]
     * @see [Companion.Builder]
     * */
    public class Config private constructor(
        isLenient: Boolean,
        lineBreakInterval: Byte,
        @JvmField
        public val encodeLowercase: Boolean,
        backFillBuffers: Boolean,
    ): EncoderDecoder.Config(isLenient, lineBreakInterval, null, backFillBuffers) {

        protected override fun decodeOutMaxSizeProtected(encodedSize: Long): Long {
            return encodedSize / 2L
        }

        protected override fun decodeOutMaxSizeOrFailProtected(encodedSize: Int, input: DecoderInput): Int {
            return encodedSize / 2
        }

        protected override fun encodeOutSizeProtected(unEncodedSize: Long): Long {
            if (unEncodedSize > MAX_UNENCODED_SIZE) {
                throw outSizeExceedsMaxEncodingSizeException(unEncodedSize, Long.MAX_VALUE)
            }
            return unEncodedSize * 2L
        }

        protected override fun toStringAddSettings(): Set<Setting> = buildSet(capacity = 2) {
            add(Setting(name = "encodeLowercase", value = encodeLowercase))
            add(Setting(name = "isConstantTime", value = isConstantTime))
        }

        internal companion object {

            private const val MAX_UNENCODED_SIZE: Long = Long.MAX_VALUE / 2

            @JvmSynthetic
            internal fun build(b: Builder): Base16 = ::Config.build(b, ::Base16)

            @get:JvmSynthetic
            internal val DEFAULT: Config = Config(
                isLenient = true,
                lineBreakInterval = 64,
                encodeLowercase = false,
                backFillBuffers = true,
            )
        }

        /**
         * DEPRECATED
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
     * A static instance of [EncoderDecoder] configured with a [Base16.Builder.lineBreak]
     * interval of `64`, and remaining [Base16.Builder] `DEFAULT` values.
     * */
    public companion object: EncoderDecoder<Base16.Config>(config = Base16.Config.DEFAULT) {

        /**
         * Uppercase Base16 encoding characters.
         * */
        public const val CHARS_UPPER: String = "0123456789ABCDEF"

        /**
         * Lowercase Base16 encoding characters.
         * */
        public const val CHARS_LOWER: String = "0123456789abcdef"

        /**
         * Syntactic sugar for Kotlin consumers that like lambdas.
         * */
        @JvmStatic
        @JvmName("-Builder")
        @OptIn(ExperimentalContracts::class)
        public inline fun Builder(block: Builder.() -> Unit): Base16 {
            contract { callsInPlace(block, InvocationKind.EXACTLY_ONCE) }
            return Builder(other = null, block)
        }

        /**
         * Syntactic sugar for Kotlin consumers that like lambdas.
         * */
        @JvmStatic
        @JvmName("-Builder")
        @OptIn(ExperimentalContracts::class)
        public inline fun Builder(other: Base16.Config?, block: Builder.() -> Unit): Base16 {
            contract { callsInPlace(block, InvocationKind.EXACTLY_ONCE) }
            return Builder(other).apply(block).build()
        }

        @get:JvmSynthetic
        internal val DELEGATE = Base16(config, unused = null)
        protected override fun name(): String = DELEGATE.name()
        protected override fun newDecoderFeedProtected(out: Decoder.OutFeed): Decoder<Base16.Config>.Feed {
            return DELEGATE.newDecoderFeedProtected(out)
        }
        protected override fun newEncoderFeedProtected(out: Encoder.OutFeed): Encoder<Base16.Config>.Feed {
            return DELEGATE.newEncoderFeedProtected(out)
        }

        private const val NAME = "Base16"
    }

    protected final override fun name(): String = NAME

    protected final override fun newDecoderFeedProtected(out: Decoder.OutFeed): Decoder<Config>.Feed {
        return DecoderFeed(out)
    }

    protected final override fun newEncoderFeedProtected(out: Encoder.OutFeed): Encoder<Config>.Feed {
        return if (config.encodeLowercase) {
            object : EncoderFeed(out) {
                override fun Encoder.OutFeed.output2(i1: Int, i2: Int) {
                    output(CHARS_LOWER[i1])
                    output(CHARS_LOWER[i2])
                }
            }
        } else {
            object : EncoderFeed(out) {
                override fun Encoder.OutFeed.output2(i1: Int, i2: Int) {
                    output(CHARS_UPPER[i1])
                    output(CHARS_UPPER[i2])
                }
            }
        }
    }

    private inner class DecoderFeed(private val out: Decoder.OutFeed): Decoder<Config>.Feed() {

        private var buf = 0
        private var hasBuffered = false

        override fun consumeProtected(input: Char) {
            val code = input.code

            val ge0: Byte = if (code >= '0'.code) 1 else 0
            val le9: Byte = if (code <= '9'.code) 1 else 0
            val geA: Byte = if (code >= 'A'.code) 1 else 0
            val leF: Byte = if (code <= 'F'.code) 1 else 0
            val gea: Byte = if (code >= 'a'.code) 1 else 0
            val lef: Byte = if (code <= 'f'.code) 1 else 0

            var diff = 0

            // char ASCII value
            //  0     48    0
            //  9     57    9 (ASCII - 48)
            diff += if (ge0 + le9 == 2) -48 else 0

            // char ASCII value
            //  A     65   10
            //  F     70   15 (ASCII - 55)
            diff += if (geA + leF == 2) -55 else 0

            // char ASCII value
            //  a     97   10
            //  f    102   15 (ASCII - 87)
            diff += if (gea + lef == 2) -87 else 0

            if (diff == 0) {
                throw EncodingException("Char[${input}] is not a valid Base16 character")
            }

            if (!hasBuffered) {
                buf = code + diff
                hasBuffered = true
                return
            }

            hasBuffered = false
            out.output(((buf shl 4) + code + diff).toByte())
        }

        override fun doFinalProtected() {
            buf = 0
            if (!hasBuffered) return
            hasBuffered = false
            // 4*1 = 4 bits. Truncated, fail.
            throw FeedBuffer.truncatedInputEncodingException(1)
        }
    }

    private abstract inner class EncoderFeed(private val out: Encoder.OutFeed): Encoder<Config>.Feed() {

        protected abstract fun Encoder.OutFeed.output2(i1: Int, i2: Int)

        final override fun consumeProtected(input: Byte) {
            val bits = input.toInt() and 0xff
            out.output2(i1 = bits shr 0x04, i2 = bits and 0x0f)
        }
        final override fun doFinalProtected() { /* no-op */ }
    }

    @Deprecated(
        message = "This constructor is scheduled for removal. Use Base16.Builder or Base16.Companion.Builder.",
        level = DeprecationLevel.WARNING,
    )
    public constructor(config: Config): this(config, unused = null)

    @Suppress("UNUSED_PARAMETER")
    private constructor(config: Config, unused: Any?): super(config)
}
