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
@file:Suppress("ConvertTwoComparisonsToRangeCheck", "NOTHING_TO_INLINE", "PropertyName", "RemoveRedundantQualifierName", "RedundantVisibilityModifier")

package io.matthewnelson.encoding.utf8

import io.matthewnelson.encoding.core.Decoder
import io.matthewnelson.encoding.core.Encoder
import io.matthewnelson.encoding.core.EncoderDecoder
import io.matthewnelson.encoding.core.EncodingException
import io.matthewnelson.encoding.core.util.DecoderInput
import io.matthewnelson.encoding.utf8.internal.initKotlin
import kotlin.jvm.JvmField
import kotlin.jvm.JvmName
import kotlin.jvm.JvmStatic
import kotlin.jvm.JvmSynthetic

/**
 * TODO
 *
 * @see [Builder]
 * @see [Default]
 * @see [ThrowOnInvalid]
 * */
public open class UTF8 private constructor(config: Config): EncoderDecoder<UTF8.Config>(config) {

    /**
     * TODO
     * */
    public companion object Default: UTF8(Config.DEFAULT) {

        // TODO: @JvmField public val CT: UTF8 = UTF8(Config.DEFAULT_CT)

        private const val NAME = "UTF-8"
    }

    /**
     * TODO
     * */
    public object ThrowOnInvalid: UTF8(Config.THROW) {

        // TODO: @JvmField public val CT: UTF8 = UTF8(Config.THROW_CT)
    }

    /**
     * TODO
     * */
    public class Builder {

        public constructor(): this(other = null)
        public constructor(other: Config?) {
            if (other == null) return
            _replacementStrategy = other.replacementStrategy
        }

        internal var _replacementStrategy = Config.DEFAULT.replacementStrategy

        /**
         * TODO
         * */
        public fun replacement(strategy: ReplacementStrategy): Builder {
            _replacementStrategy = strategy
            return this
        }

        // TODO: constantTime

        /**
         * TODO
         * */
        public fun build(): UTF8 = Config.build(this)
    }

    /**
     * TODO
     * */
    public abstract class ReplacementStrategy private constructor(

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
            public val U_0034: ReplacementStrategy = object : ReplacementStrategy(size = 1) {
                override fun doOutput(out: Decoder.OutFeed) {
                    out.output('?'.code.toByte())
                }
                override fun toString(): String = "UTF8.ReplacementStrategy[U+0034]"
            }

            /**
             * TODO
             * */
            @JvmField
            public val U_FFFD: ReplacementStrategy = object : ReplacementStrategy(size = 3) {
                override fun doOutput(out: Decoder.OutFeed) {
                    out.output(0xef.toByte())
                    out.output(0xbf.toByte())
                    out.output(0xbd.toByte())
                }
                override fun toString(): String = "UTF8.ReplacementStrategy[U+FFFD]"
            }

            /**
             * TODO
             * */
            @JvmField
            public val KOTLIN: ReplacementStrategy = initKotlin(U_0034, U_FFFD)

            /**
             * TODO
             * */
            @JvmField
            public val THROW: ReplacementStrategy = object : ReplacementStrategy(size = 0) {
                override fun doOutput(out: Decoder.OutFeed) {
                    throw EncodingException("Malformed UTF-8 character sequence")
                }
                override fun sizeOrThrow(): Int {
                    throw EncodingException("Malformed UTF-8 character sequence")
                }
                override fun toString(): String = "UTF8.ReplacementStrategy[THROW]"
            }
        }

        @Throws(EncodingException::class)
        internal abstract fun doOutput(out: Decoder.OutFeed)
        @Throws(EncodingException::class)
        internal open fun sizeOrThrow(): Int = size

        /** @suppress */
        public abstract override fun toString(): String
    }

    /**
     * TODO
     * */
    public class Config private constructor(

        /**
         * TODO
         * */
        @JvmField
        public val replacementStrategy: ReplacementStrategy,
    ): EncoderDecoder.Config(isLenient = null, lineBreakInterval = -1, paddingChar = null) {

        protected override fun decodeOutMaxSizeProtected(
            encodedSize: Long,
        ): Long = if (encodedSize > MAX_CHAR_LEN) Long.MIN_VALUE else encodedSize * 3

        protected override fun decodeOutMaxSizeOrFailProtected(
            encodedSize: Int,
            input: DecoderInput,
        ): Int {
            val s = decodeOutMaxSizeProtected(encodedSize.toLong())
            return if (s in 0L..Int.MAX_VALUE.toLong()) s.toInt() else Int.MIN_VALUE
        }

        protected override fun encodeOutSizeProtected(
            unEncodedSize: Long,
        ): Long {
            TODO("Not yet implemented")
        }

        protected override fun toStringAddSettings(): Set<Setting> = buildSet {
            add(Setting("replacementStrategy", replacementStrategy))
        }

        internal companion object {

            private const val MAX_CHAR_LEN: Long = Long.MAX_VALUE / 3

            @JvmSynthetic
            internal fun build(b: Builder): UTF8 {
                val replacementStrategy = b._replacementStrategy
                // TODO: If constantTime, ensure alwaysUsePreProcessing = false

                if (replacementStrategy == DEFAULT.replacementStrategy) return Default
                if (replacementStrategy == THROW.replacementStrategy) return ThrowOnInvalid

                val config = Config(
                    replacementStrategy,
                )

                return UTF8(config)
            }

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
}
