/*
 * Copyright (c) 2025 Matthew Nelson
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 **/
@file:Suppress("ConvertTwoComparisonsToRangeCheck", "FunctionName", "NOTHING_TO_INLINE", "PropertyName", "RedundantVisibilityModifier", "RemoveRedundantQualifierName")

package io.matthewnelson.encoding.utf8

import io.matthewnelson.encoding.core.Decoder
import io.matthewnelson.encoding.core.Encoder
import io.matthewnelson.encoding.core.EncoderDecoder
import io.matthewnelson.encoding.core.util.DecoderInput
import io.matthewnelson.encoding.utf8.internal.build
import io.matthewnelson.encoding.utf8.internal.doOutput
import io.matthewnelson.encoding.utf8.internal.initializeKotlin
import io.matthewnelson.encoding.utf8.internal.sizeOrThrow
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
public open class UTF8: EncoderDecoder<UTF8.Config> {

    /**
     * TODO
     * */
    public companion object Default: UTF8(config = Config.DEFAULT) {

        // TODO: @JvmField public val CT: UTF8 = UTF8(Config.DEFAULT_CT)

        /**
         * TODO
         * */
        @JvmStatic
        @JvmName("-Builder")
        @OptIn(ExperimentalContracts::class)
        public inline fun Builder(block: Builder.() -> Unit): UTF8 {
            contract { callsInPlace(block, InvocationKind.EXACTLY_ONCE) }
            return Builder(other = null, block)
        }

        /**
         * TODO
         * */
        @JvmStatic
        @JvmName("-Builder")
        @OptIn(ExperimentalContracts::class)
        public inline fun Builder(other: UTF8.Config?, block: Builder.() -> Unit): UTF8 {
            contract { callsInPlace(block, InvocationKind.EXACTLY_ONCE) }
            return Builder(other).apply(block).build()
        }

        private const val NAME = "UTF-8"
    }

    /**
     * TODO
     * */
    public object ThrowOnInvalid: UTF8(config = Config.THROW) {

        // TODO: @JvmField public val CT: UTF8 = UTF8(Config.THROW_CT)
    }

    /**
     * TODO
     * */
    public class Builder {

        public constructor(): this(other = null)
        public constructor(other: Config?) {
            if (other == null) return
            this._replacementStrategy = other.replacementStrategy
        }

        @get:JvmSynthetic
        @set:JvmSynthetic
        internal var _replacementStrategy = Config.DEFAULT.replacementStrategy

        /**
         * TODO
         * */
        public fun replacement(strategy: ReplacementStrategy): Builder = apply { _replacementStrategy = strategy }

        // TODO: constantTime

        /**
         * TODO
         * */
        public fun build(): UTF8 = Config.build(this)
    }

    /**
     * TODO
     * */
    public class ReplacementStrategy private constructor(

        /**
         * TODO
         * */
        @JvmField
        public val size: Int,
    ) {

        public companion object {

            /**
             * TODO
             * */
            @JvmField
            public val U_0034: ReplacementStrategy = ReplacementStrategy(size = 1)

            /**
             * TODO
             * */
            @JvmField
            public val U_FFFD: ReplacementStrategy = ReplacementStrategy(size = 3)

            /**
             * TODO
             * */
            @JvmField
            public val KOTLIN: ReplacementStrategy = initializeKotlin(U_0034, U_FFFD)

            /**
             * TODO
             * */
            @JvmField
            public val THROW: ReplacementStrategy = ReplacementStrategy(size = 0)
        }

        /** @suppress */
        public override fun toString(): String = when (size) {
            U_0034.size -> "UTF8.ReplacementStrategy[U+0034]"
            U_FFFD.size -> "UTF8.ReplacementStrategy[U+FFFD]"
            else        -> "UTF8.ReplacementStrategy[THROW]"
        }
    }

    /**
     * TODO
     * */
    public class Config private constructor(
        @JvmField
        public val replacementStrategy: ReplacementStrategy,
    ): EncoderDecoder.Config(null, -1, null) {

        // Chars -> Bytes
        protected override fun decodeOutMaxSizeProtected(encodedSize: Long): Long {
            if (encodedSize > MAX_ENCODED_SIZE_64) {
                throw outSizeExceedsMaxEncodingSizeException(encodedSize, Long.MAX_VALUE)
            }
            return encodedSize * 3L
        }

        // Chars -> Bytes
        protected override fun decodeOutMaxSizeOrFailProtected(encodedSize: Int, input: DecoderInput): Int {
            if (encodedSize > MAX_ENCODED_SIZE_32) {
                throw outSizeExceedsMaxEncodingSizeException(encodedSize, Int.MAX_VALUE)
            }
            return encodedSize * 3
        }

        // Bytes -> Chars
        protected override fun encodeOutSizeProtected(unEncodedSize: Long): Long {
            TODO("Not yet implemented")
        }

        protected override fun toStringAddSettings(): Set<Setting> = buildSet {
            add(Setting("replacementStrategy", replacementStrategy))
        }

        internal companion object {

            private const val MAX_ENCODED_SIZE_64: Long = Long.MAX_VALUE / 3
            private const val MAX_ENCODED_SIZE_32: Int = Int.MAX_VALUE / 3

            @JvmSynthetic
            internal fun build(b: Builder): UTF8 = ::Config.build(b, ::UTF8)

            @get:JvmSynthetic
            internal val DEFAULT: Config = Config(
                replacementStrategy = ReplacementStrategy.KOTLIN,
            )

            @get:JvmSynthetic
            internal val THROW: Config = Config(
                replacementStrategy = ReplacementStrategy.THROW,
            )
        }
    }

    /**
     * TODO
     * */
    public class CharPreProcessor {

        private val strategy: ReplacementStrategy

        public constructor(utf8: UTF8): this(utf8.config.replacementStrategy)
        public constructor(config: Config): this(config.replacementStrategy)
        public constructor(strategy: ReplacementStrategy) { this.strategy = strategy }

        public companion object {

            /**
             * TODO
             * */
            @JvmStatic
            public inline fun CharArray.sizeUTF8(utf8: UTF8): Long = sizeUTF8(utf8.config)

            /**
             * TODO
             * */
            @JvmStatic
            public inline fun CharArray.sizeUTF8(config: Config): Long = sizeUTF8(config.replacementStrategy)

            /**
             * TODO
             * */
            @JvmStatic
            public inline fun CharArray.sizeUTF8(strategy: ReplacementStrategy): Long = iterator().sizeUTF8(strategy)

            /**
             * TODO
             * */
            @JvmStatic
            public inline fun CharSequence.sizeUTF8(utf8: UTF8): Long = sizeUTF8(utf8.config)

            /**
             * TODO
             * */
            @JvmStatic
            public inline fun CharSequence.sizeUTF8(config: Config): Long = sizeUTF8(config.replacementStrategy)

            /**
             * TODO
             * */
            @JvmStatic
            public inline fun CharSequence.sizeUTF8(strategy: ReplacementStrategy): Long = iterator().sizeUTF8(strategy)

            /**
             * TODO
             * */
            @JvmStatic
            public inline fun Iterator<Char>.sizeUTF8(utf8: UTF8): Long = sizeUTF8(utf8.config)

            /**
             * TODO
             * */
            @JvmStatic
            public inline fun Iterator<Char>.sizeUTF8(config: Config): Long = sizeUTF8(config.replacementStrategy)

            /**
             * TODO
             * */
            @JvmStatic
            public fun Iterator<Char>.sizeUTF8(strategy: ReplacementStrategy): Long {
                val pp = CharPreProcessor(strategy)
                while (hasNext()) { pp + next() }
                return pp.doFinal()
            }
        }

        /**
         * TODO
         * */
        @get:JvmName("currentSize")
        public var currentSize: Long = 0L
            private set

        private var buf = 0
        private var hasBuffered = false

        /**
         * TODO
         * */
        public operator fun plus(input: Char) {
            if (!hasBuffered) {
                buf = input.code
                hasBuffered = true
                return
            }
            val c = buf
            val cNext = input.code

            if (c < 0x0080) {
                buf = cNext
                currentSize += 1
                return
            }
            if (c < 0x0800) {
                buf = cNext
                currentSize += 2
                return
            }
            if (c < 0xd800 || c > 0xdfff) {
                buf = cNext
                currentSize += 3
                return
            }
            if (c > 0xdbff) {
                buf = cNext
                currentSize += strategy.sizeOrThrow()
                return
            }
            if (cNext < 0xdc00 || cNext > 0xdfff) {
                buf = cNext
                currentSize += strategy.sizeOrThrow()
                return
            }

            hasBuffered = false
            currentSize += 4
        }

        /**
         * TODO
         * */
        public fun doFinal(): Long {
            val c = buf
            val s = currentSize
            buf = 0
            currentSize = 0
            if (!hasBuffered) return s
            hasBuffered = false
            if (c < 0x0080) return s + 1
            if (c < 0x0800) return s + 2
            if (c < 0xd800 || c > 0xdfff) return s + 3
            return s + strategy.sizeOrThrow()
        }
    }

    protected final override fun name(): String = NAME

    protected final override fun newDecoderFeedProtected(out: Decoder.OutFeed): Decoder<Config>.Feed {
        // TODO: Constant Time
        return DecoderFeed(out)
    }

    protected final override fun newEncoderFeedProtected(out: Encoder.OutFeed): Encoder<Config>.Feed {
        TODO("Not yet implemented")
    }

    private inner class DecoderFeed(private val out: Decoder.OutFeed): Decoder<Config>.Feed() {

        private var buf = 0
        private var hasBuffered = false

        override fun consumeProtected(input: Char) {
            if (!hasBuffered) {
                buf = input.code
                hasBuffered = true
                return
            }
            val c = buf
            val cNext = input.code

            if (c < 0x0080) {
                buf = cNext
                out.output(c.toByte())
                return
            }
            if (c < 0x0800) {
                buf = cNext
                out.output((c  shr  6          or 0xc0).toByte())
                out.output((c         and 0x3f or 0x80).toByte())
                return
            }
            if (c < 0xd800 || c > 0xdfff) {
                buf = cNext
                out.output((c  shr 12          or 0xe0).toByte())
                out.output((c  shr  6 and 0x3f or 0x80).toByte())
                out.output((c         and 0x3f or 0x80).toByte())
                return
            }
            if (c > 0xdbff) {
                buf = cNext
                config.replacementStrategy.doOutput(out)
                return
            }
            if (cNext < 0xdc00 || cNext > 0xdfff) {
                buf = cNext
                config.replacementStrategy.doOutput(out)
                return
            }

            hasBuffered = false
            val cp = ((c shl 10) + cNext) + (0x010000 - (0xd800 shl 10) - 0xdc00)
            out.output((cp shr 18          or 0xf0).toByte())
            out.output((cp shr 12 and 0x3f or 0x80).toByte())
            out.output((cp shr  6 and 0x3f or 0x80).toByte())
            out.output((cp        and 0x3f or 0x80).toByte())
        }

        override fun doFinalProtected() {
            val c = buf
            buf = 0
            if (!hasBuffered) return
            hasBuffered = false
            if (c < 0x0080) {
                out.output(c.toByte())
                return
            }
            if (c < 0x0800) {
                out.output((c  shr  6          or 0xc0).toByte())
                out.output((c         and 0x3f or 0x80).toByte())
                return
            }
            if (c < 0xd800 || c > 0xdfff) {
                out.output((c  shr 12          or 0xe0).toByte())
                out.output((c  shr  6 and 0x3f or 0x80).toByte())
                out.output((c         and 0x3f or 0x80).toByte())
                return
            }
//            if (c > 0xdbff) {
//                config.invalidSequenceStrategy.doOutput(out)
//                return
//            }

            config.replacementStrategy.doOutput(out)
        }
    }

    private constructor(config: Config): super(config)
}
