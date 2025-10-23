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
 * TODO
 * */
public class Base16: EncoderDecoder<Base16.Config> {

    /**
     * TODO
     * */
    public class Builder {

        public constructor(): this(other = null)
        public constructor(other: Config?) {
            if (other == null) return
            this._isLenient = other.isLenient ?: true
            this._lineBreakInterval = other.lineBreakInterval
            this._encodeToLowercase = other.encodeToLowercase
        }

        @JvmSynthetic
        internal var _isLenient: Boolean = true
        @JvmSynthetic
        internal var _lineBreakInterval: Byte = 0
        @JvmSynthetic
        internal var _encodeToLowercase: Boolean = false

        /**
         * TODO
         * */
        public fun isLenient(enable: Boolean): Builder = apply { _isLenient = enable }

        /**
         * TODO
         * */
        public fun lineBreak(interval: Byte): Builder = apply { _lineBreakInterval = interval }

        /**
         * TODO
         * */
        public fun encodeToLowercase(enable: Boolean): Builder = apply { _encodeToLowercase = enable }

        /**
         * TODO
         * */
        public fun strict(): Builder = apply {
            _isLenient = false
            _lineBreakInterval = 0
            _encodeToLowercase = false
        }

        /**
         * TODO
         * */
        public fun build(): Base16 = Config.build(this)
    }

    /**
     * TODO
     * */
    public class Config private constructor(
        isLenient: Boolean,
        lineBreakInterval: Byte,
        @JvmField
        public val encodeToLowercase: Boolean,
    ): EncoderDecoder.Config(isLenient, lineBreakInterval, null) {

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

        protected override fun toStringAddSettings(): Set<Setting> = buildSet {
            add(Setting(name = "encodeToLowercase", value = encodeToLowercase))
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
                encodeToLowercase = false,
            )
        }

        /** @suppress */
        @JvmField
        public val isConstantTime: Boolean = true
    }

    /**
     * TODO
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
         * TODO
         * */
        @JvmStatic
        @JvmName("-Builder")
        @OptIn(ExperimentalContracts::class)
        public inline fun Builder(block: Builder.() -> Unit): Base16 {
            contract { callsInPlace(block, InvocationKind.EXACTLY_ONCE) }
            return Builder(other = null, block)
        }

        /**
         * TODO
         * */
        @JvmStatic
        @JvmName("-Builder")
        @OptIn(ExperimentalContracts::class)
        public inline fun Builder(other: Base16.Config?, block: Builder.() -> Unit): Base16 {
            contract { callsInPlace(block, InvocationKind.EXACTLY_ONCE) }
            return Builder(other).apply(block).build()
        }

        @get:JvmSynthetic
        internal val DELEGATE = Base16(config)
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
        return object : Decoder<Config>.Feed() {

            private val buffer = DecodingBuffer(out)

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

                buffer.update(code + diff)
            }

            override fun doFinalProtected() { buffer.finalize() }
        }
    }

    protected final override fun newEncoderFeedProtected(out: Encoder.OutFeed): Encoder<Config>.Feed {
        return object : Encoder<Config>.Feed() {

            private val table = if (config.encodeToLowercase) CHARS_LOWER else CHARS_UPPER

            override fun consumeProtected(input: Byte) {
                // A FeedBuffer is not necessary here as every 1
                // byte of input, 2 characters are output.
                val bits = input.toInt() and 0xff

                val i1 = bits shr 4
                val i2 = bits and 0x0f

                out.output(table[i1])
                out.output(table[i2])
            }

            override fun doFinalProtected() { /* no-op */ }
        }
    }

    private inner class DecodingBuffer(out: Decoder.OutFeed): FeedBuffer(
        blockSize = 2,
        flush = { buffer ->
            var bitBuffer = 0
            for (bits in buffer) {
                bitBuffer = (bitBuffer shl 4) or bits
            }
            out.output(bitBuffer.toByte())
        },
        finalize = { modulus, _->
            if (modulus != 0) {
                // 4*1 = 4 bits. Truncated, fail.
                throw truncatedInputEncodingException(modulus)
            }
        }
    )

    // TODO: Deprecate & replace (Issue #172)
    public constructor(config: Config): super(config)
}
