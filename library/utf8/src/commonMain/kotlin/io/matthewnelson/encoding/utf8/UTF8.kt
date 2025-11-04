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
import io.matthewnelson.encoding.core.EncodingException
import io.matthewnelson.encoding.core.util.DecoderInput
import io.matthewnelson.encoding.utf8.internal.build
import io.matthewnelson.encoding.utf8.internal.initializeKotlin
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
        internal var _replacementStrategy = ReplacementStrategy.KOTLIN

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
    ): EncoderDecoder.Config(null, -1, null, true /* TODO */) {

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
            return unEncodedSize
        }

        protected override fun toStringAddSettings(): Set<Setting> = buildSet(capacity = 1) {
            add(Setting("replacementStrategy", replacementStrategy))
        }

        internal companion object {

            private const val MAX_ENCODED_SIZE_64: Long = Long.MAX_VALUE / 3L
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
    public abstract class CharPreProcessor private constructor(

        /**
         * TODO
         * */
        @JvmField
        public val strategy: ReplacementStrategy,
    ) {

        public companion object {

            /**
             * TODO
             * */
            @JvmStatic
            public inline fun of(utf8: UTF8): CharPreProcessor = of(utf8.config.replacementStrategy)

            /**
             * TODO
             * */
            @JvmStatic
            public inline fun of(config: Config): CharPreProcessor = of(config.replacementStrategy)

            /**
             * TODO
             * */
            @JvmStatic
            public fun of(strategy: ReplacementStrategy): CharPreProcessor = when (strategy.size) {
                ReplacementStrategy.THROW.size -> object : CharPreProcessor(strategy) {
                    override fun sizeOrThrow(): Int {
                        throw EncodingException("Malformed UTF-8 character sequence")
                    }
                }
                else -> object : CharPreProcessor(strategy) {
                    override fun sizeOrThrow(): Int = strategy.size
                }
            }

            /**
             * TODO
             * */
            @JvmStatic
            public inline fun CharArray.sizeUTF8(utf8: UTF8): Long = sizeUTF8(utf8.config.replacementStrategy)

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
            public inline fun CharSequence.sizeUTF8(utf8: UTF8): Long = sizeUTF8(utf8.config.replacementStrategy)

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
            public inline fun Iterator<Char>.sizeUTF8(utf8: UTF8): Long = sizeUTF8(utf8.config.replacementStrategy)

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
                val pp = CharPreProcessor.of(strategy)
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
                currentSize += sizeOrThrow()
                return
            }
            if (cNext < 0xdc00 || cNext > 0xdfff) {
                buf = cNext
                currentSize += sizeOrThrow()
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
            return s + sizeOrThrow()
        }

        @Throws(EncodingException::class)
        protected abstract fun sizeOrThrow(): Int
    }

    protected final override fun name(): String = NAME

    // Chars -> Bytes
    protected final override fun newDecoderFeedProtected(out: Decoder.OutFeed): Decoder<Config>.Feed {
        // TODO: Constant Time
        return when (config.replacementStrategy.size) {
            ReplacementStrategy.U_0034.size -> object : DecoderFeed(out) {
                override fun Decoder.OutFeed.outputReplacementSequence() {
                    output('?'.code.toByte())
                }
            }
            ReplacementStrategy.U_FFFD.size -> object : DecoderFeed(out) {
                override fun Decoder.OutFeed.outputReplacementSequence() {
                    output(0xef.toByte())
                    output(0xbf.toByte())
                    output(0xbd.toByte())
                }
            }
            ReplacementStrategy.THROW.size -> object : DecoderFeed(out) {
                override fun Decoder.OutFeed.outputReplacementSequence() {
                    throw EncodingException("Malformed UTF-8 character sequence")
                }
            }
            // "Should" never make it here...
            else -> error("Unknown ${config.replacementStrategy}.size[${config.replacementStrategy.size}]")
        }
    }

    // Bytes -> Chars
    protected final override fun newEncoderFeedProtected(out: Encoder.OutFeed): Encoder<Config>.Feed {
        return when (config.replacementStrategy.size) {
            ReplacementStrategy.THROW.size  -> object : EncoderFeed(out) {
                override fun Encoder.OutFeed.outputReplacementChar() {
                    throw EncodingException("Malformed UTF-8 character sequence")
                }
            }
            else -> EncoderFeed(out)
        }
    }

    // Chars -> Bytes
    private abstract inner class DecoderFeed(private val out: Decoder.OutFeed): Decoder<Config>.Feed() {

        @Throws(EncodingException::class)
        protected abstract fun Decoder.OutFeed.outputReplacementSequence()

        private var buf = 0
        private var hasBuffered = false

        final override fun consumeProtected(input: Char) {
            if (!hasBuffered) {
                buf = input.code
                hasBuffered = true
                return
            }
            val c = buf
            val cNext = input.code

            if (c.process1()) {
                buf = cNext
                return
            }
            if (c.process2()) {
                buf = cNext
                return
            }
            if (c.process3()) {
                buf = cNext
                return
            }
            if (c > 0xdbff) {
                buf = cNext
                out.outputReplacementSequence()
                return
            }
            if (cNext < 0xdc00 || cNext > 0xdfff) {
                buf = cNext
                out.outputReplacementSequence()
                return
            }

            hasBuffered = false
            val codePoint = ((c shl 10) + cNext) + (0x010000 - (0xd800 shl 10) - 0xdc00)
            out.output((codePoint shr 18          or 0xf0).toByte())
            out.output((codePoint shr 12 and 0x3f or 0x80).toByte())
            out.output((codePoint shr  6 and 0x3f or 0x80).toByte())
            out.output((codePoint        and 0x3f or 0x80).toByte())
        }

        final override fun doFinalProtected() {
            val c = buf
            buf = 0
            if (!hasBuffered) return
            hasBuffered = false
            if (c.process1()) return
            if (c.process2()) return
            if (c.process3()) return
            out.outputReplacementSequence()
        }

        private inline fun Int.process1(): Boolean = if (this < 0x0080) {
            out.output((this                        ).toByte())
            true
        } else {
            false
        }

        private inline fun Int.process2(): Boolean = if (this < 0x0800) {
            out.output((this shr  6          or 0xc0).toByte())
            out.output((this        and 0x3f or 0x80).toByte())
            true
        } else {
            false
        }

        private inline fun Int.process3(): Boolean = if (this < 0xd800 || this > 0xdfff) {
            out.output((this shr 12          or 0xe0).toByte())
            out.output((this shr  6 and 0x3f or 0x80).toByte())
            out.output((this        and 0x3f or 0x80).toByte())
            true
        } else {
            false
        }
    }

    // Bytes -> Chars
    private open inner class EncoderFeed(private val out: Encoder.OutFeed): Encoder<Config>.Feed() {

        @Throws(EncodingException::class)
        protected open fun Encoder.OutFeed.outputReplacementChar() {
            output('\ufffd')
        }

        private val buf = IntArray(3)
        private var iBuf = 0
        private var needed = 0

        final override fun consumeProtected(input: Byte) {
            debug { "INPUT: needed[$needed] - iBuf[$iBuf] - buf[${buf[0]}, ${buf[1]}, ${buf[2]}]" }
            // bN, as in the final byte for whatever may be needed when processing input
            // e.g.
            //   needed = 2 >> buf[0] is b0, bN is b1
            //   needed = 3 >> buf[0] is b0, buf[1] is b1, bN is b2
            //   needed = 4 >> buf[0] is b0, buf[1] is b1, buf[2] is b2, bN is b3
            val bN = input.toInt()

            if (needed == 0) {
                needed = bN.process1()
                if (needed > 0) buf[iBuf++] = bN
                return
            }

            // Final input for needed is never buffered, just go straight
            // into processing everything using input as the final byte.
            if (needed - 1 > iBuf) {
                buf[iBuf++] = bN
                return // Await more input
            }

            val b0 = buf[0]
            if (needed == 2) when (bN.process2(b0 = b0).debug { "2[$it]" }) {
                // Drop b0
                0 -> return bN.processN()
                // Success
                2 -> return reset()
            }

            val b1 = buf[1]
            if (needed == 3) when (bN.process3(b0 = b0, b1 = b1).debug { "3[$it]" }) {
                // Drop b0
                0 -> when (val n = b1.process1().debug { "3 - 0[$it]" }) {
                    // Success/Replaced
                    0 -> return bN.processN()
                    // Need 2
                    2 -> when (bN.process2(b0 = b1).debug { "3 - 0 - 2[$it]" }) {
                        // Drop b0
                        0 -> return bN.processN()
                        // Success
                        2 -> return reset()
                    }
                    // Need 3 or 4
                    3, 4 -> {
                        buf[0] = b1
                        buf[1] = bN
                        return reset(iBuf = 2, needed = n)
                    }
                }
                // Drop b0, b1
                1 -> return bN.processN()
                // Success
                3 -> return reset()
            }

            val b2 = buf[2]
            if (needed == 4) when (bN.process4(b0 = b0, b1 = b1, b2 = b2).debug { "4[$it]" }) {
                // Drop b0
                0 -> when (b1.process1().debug { "4 - 0[$it]" }) {
                    // Success/Replaced
                    0 -> when (val n = b2.process1().debug { "4 - 0 - 0[$it]" }) {
                        // Success/Replaced
                        0 -> return bN.processN()
                        // Need 2
                        2 -> when (bN.process2(b0 = b2).debug { "4 - 0 - 0 - 2[$it]" }) {
                            // Drop b0
                            0 -> return bN.processN()
                            // Success
                            2 -> return reset()
                        }
                        // Need 3 or 4
                        3, 4 -> {
                            buf[0] = b2
                            buf[1] = bN
                            return reset(iBuf = 2, needed = n)
                        }
                    }
                    // Need 2
                    2 -> when (b2.process2(b0 = b1).debug { "4 - 0 - 2[$it]" }) {
                        // Drop b0
                        0 -> when (val n = b2.process1().debug { "4 - 0 - 2 - 0[$it]" }) {
                            // Success/replaced
                            0 -> return bN.processN()
                            // Need 2
                            2 -> when (bN.process2(b0 = b2).debug { "4 - 0 - 2 - 0 - 2[$it]" }) {
                                // Drop b0
                                0 -> return bN.processN()
                                // Success
                                2 -> return reset()
                            }
                            // Need 3 or 4
                            3, 4 -> {
                                buf[0] = b2
                                buf[1] = bN
                                return reset(iBuf = 2, needed = n)
                            }
                        }
                        // Success
                        2 -> return bN.processN()
                    }
                    // Need 3
                    3 -> when (bN.process3(b0 = b1, b1 = b2).debug { "4 - 0 - 3[$it]" }) {
                        // Drop b0
                        0 -> when (val n = b2.process1().debug { "4 - 0 - 3 - 0[$it]" }) {
                            // Success/Replaced
                            0 -> return bN.processN()
                            // Need 2
                            2 -> when (bN.process2(b0 = b2).debug { "4 - 0 - 3 - 2[$it]" }) {
                                // Drop b0
                                0 -> return bN.processN()
                                // Success
                                2 -> return reset()
                            }
                            // Need 3 or 4
                            3, 4 -> {
                                buf[0] = b2
                                buf[1] = bN
                                return reset(iBuf = 2, needed = n)
                            }
                        }
                        // Drop b0, b1
                        1 -> return bN.processN()
                        // Success
                        3 -> return reset()
                    }
                    // Need 4
                    4 -> {
                        buf[0] = b1
                        buf[1] = b2
                        buf[2] = bN
                        return reset(iBuf = 3, needed = 4)
                    }
                }
                // Drop b0, b1
                1 -> when (val n = b2.process1().debug { "4 - 1[$it]" }) {
                    // Success/Replaced
                    0 -> return bN.processN()
                    // Need 2
                    2 -> when (bN.process2(b0 = b2).debug { "4 - 1 - 2[$it]" }) {
                        // Drop b0
                        0 -> return bN.processN()
                        // Success
                        2 -> return reset()
                    }
                    // Need 3 or 4
                    3, 4 -> {
                        buf[0] = b2
                        buf[1] = bN
                        return reset(iBuf = 2, needed = n)
                    }
                }
                // Drop b0, b1, b2
                2 -> return bN.processN()
                // Success
                4 -> return reset()
            }

            // "Should" never make it here
            error("Illegal configuration >> needed[$needed] - iBuf[$iBuf] - buf[${buf[0]}, ${buf[1]}, ${buf[2]}] - input[$bN]")
        }

        final override fun doFinalProtected() {
            debug { "FINAL: needed[$needed] - iBuf[$iBuf] - buf[${buf[0]}, ${buf[1]}, ${buf[2]}]" }
            if (needed == 0) {
                debug { "--- FINAL ---" }
                return buf.fill(0)
            }
            if (iBuf == 1) {
                out.outputReplacementChar()
                reset()
                debug { "--- FINAL ---" }
                return buf.fill(0)
            }

            // Only need to worry about 2 or more buffered bytes at
            // this point, so that leaves us with a needed value of
            // 3 or 4 left to handle.
            val b0 = buf[0]
            val b1 = buf[1]

            // The !isContinuation() value is used as a filler byte to
            // simulate index exhaustion for process3/process4 in order to
            // kick it back to the previous processN step. This is so that
            // buffered input is checked and any necessary replacement
            // characters still get output, but process3/process4 will always
            // return early as if the next byte in the array (if one had all
            // the input and were looping over it) was not available.
            val cN = 0x80 or 0xc0

            if (needed == 3) {
                // 2 buffered bytes awaiting 3rd
                when (cN.process3(b0 = b0, b1 = b1).debug { "F3[$it]" }) {
                    // Drop b0
                    0 -> if (b1.process1() != 0) out.outputReplacementChar()
                    // Drop b0, b1
                    1 -> {} // Simulated index exhaustion hit. No more input to process.
                    // Success/other
                    else -> error("process3 unhandled return value")
                }
                reset()
                debug { "--- FINAL ---" }
                return buf.fill(0)
            }

            if (needed == 4 && iBuf > 2) {
                // 3 buffered bytes awaiting 4th
                val b2 = buf[2]
                when (cN.process4(b0 = b0, b1 = b1, b2 = b2).debug { "F4{3}[$it]" }) {
                    // Drop b0
                    0 -> when (b1.process1().debug { "F4{3} - 0[$it]" }) {
                        // Success/Replaced
                        0 -> if (b2.process1() != 0) out.outputReplacementChar()
                        // Need 2
                        2 -> when (b2.process2(b0 = b1).debug { "F4{3} - 0 - 2[$it]" }) {
                            // Drop b0
                            0 -> if (b2.process1() != 0) out.outputReplacementChar()
                            // Success
                            2 -> {}
                            else -> error("process2 unhandled return value")
                        }
                        // Need 3
                        3 -> when (cN.process3(b0 = b1, b1 = b2).debug { "F4{3} - 0 - 3[$it]" }) {
                            // Drop b0
                            0 -> if (b2.process1() != 0) out.outputReplacementChar()
                            // Drop b0, b1
                            1 -> {} // Simulated index exhaustion hit. No more input to process.
                            // Success/other
                            else -> error("process3 unhandled return value")
                        }
                        // Need 4
                        4 -> when (cN.process4(b0 = b1, b1 = b2, b2 = cN).debug { "F4{3} - 0 - 4[$it]" }) {
                            // Drop b0
                            0 -> if (b2.process1() != 0) out.outputReplacementChar()
                            // Drop b0, b1
                            1 -> {} // Simulated index exhaustion hit. No more input to process.
                            // Drop b0, b1, b2
                            2 -> error("process4 returned 2???") // Should never happen >> [b2 == cN]
                            // Success/other
                            else -> error("process4 unhandled return value")
                        }
                        // What?
                        else -> error("process1 unhandled return value")
                    }
                    // Drop b0, b1
                    1 -> if (b2.process1() != 0) out.outputReplacementChar()
                    // Drop b0, b1, b2
                    2 ->  {} // Simulated index exhaustion hit. No more input to process.
                    // Success/other
                    else -> error("process4 unhandled return value")
                }
                reset()
                debug { "--- FINAL ---" }
                return buf.fill(0)
            }

            if (needed == 4) {
                // 2 buffered bytes awaiting 3rd and 4th
                when (cN.process4(b0 = b0, b1 = b1, b2 = cN).debug { "F4{2}[$it]" }) {
                    // Drop b0
                    0 -> if (b1.process1() != 0) out.outputReplacementChar()
                    // Drop b0, b1
                    1 -> {} // Simulated index exhaustion hit. No more input to process.
                    // Drop b0, b1, b2
                    2 -> error("process4 returned 2???") // Should never happen >> [b2 == cN]
                    // Success/other
                    else -> error("process4 unhandled return value")
                }
                reset()
                debug { "--- FINAL ---" }
                return buf.fill(0)
            }

            // "Should" never make it here
            error("Illegal configuration >> needed[$needed] - iBuf[$iBuf] - buf[${buf[0]}, ${buf[1]}, ${buf[2]}]")
        }

        private inline fun reset(iBuf: Int = 0, needed: Int = 0) {
            this.iBuf = iBuf
            this.needed = needed
        }

        /**
         * Process `bN` from a `needed` when statement within [consumeProtected]
         * */
        private inline fun Int.processN() {
            iBuf = 0
            needed = process1()
            if (needed > 0) buf[iBuf++] = this
            return
        }

        /**
         * Returns:
         *  - `0`: Success or Replaced
         *      - Do nothing, or process next (if available)
         *  - `2`: Need `2`
         *      - Buffer, or process 2 (if available)
         *  - `3`: Need `3`
         *      - Buffer, or process 3 (if available)
         *  - `4`: Need `4`
         *      - Buffer
         * */
        private fun Int.process1(): Int {
            if (this >= 0) {
                debug { "P1 - 1" }
                out.output(this.toChar())
                return 0
            }
            if (this shr 5 == -2) {
                debug { "P1 - 2" }
                return 2
            }
            if (this shr 4 == -2) {
                debug { "P1 - 3" }
                return 3
            }
            if (this shr 3 == -2) {
                debug { "P1 - 4" }
                return 4
            }
            debug { "P1 - 5" }
            out.outputReplacementChar()
            return 0
        }

        /**
         * Returns:
         *  - `0`: Drop b0
         *  - `2`: Success
         * */
        private fun Int.process2(b0: Int): Int {
            val b1 = this
            if (b0 and 0x1e == 0x00) {
                debug { "P2 - 1" }
                out.outputReplacementChar()
                return 0
            }
            if (!b1.isContinuation()) {
                debug { "P2 - 2" }
                out.outputReplacementChar()
                return 0
            }
            debug { "P2 - F" }
            val codePoint = 0x0f80 xor b1 xor (b0 shl 6)
            out.output(codePoint.toChar())
            return 2
        }

        /**
         * Returns:
         *  - `0`: Drop b0
         *  - `1`: Drop b0, b1
         *  - `3`: Success
         * */
        private fun Int.process3(b0: Int, b1: Int): Int {
            val b2 = this
            when {
                b0 and 0x0f == 0x00 -> if (b1 and 0xe0 != 0xa0) {
                    debug { "P3 - 1" }
                    // Non-shortest form
                    out.outputReplacementChar()
                    return 0
                }
                b0 and 0x0f == 0x0d -> if (b1 and 0xe0 != 0x80) {
                    debug { "P3 - 2" }
                    // Partial surrogate code point
                    out.outputReplacementChar()
                    if (config.replacementStrategy.size == ReplacementStrategy.U_0034.size) {
                        // Must check b2 to see if it needs to be run through process1()
                        if (!b2.isContinuation()) {
                            debug { "P3 - 2 ------ HIT[b2]" }
                            return 1
                        }
                        // Replace all 3 bytes with single replacement character.
                        return 3
                    }
                    return 0
                }
                !b1.isContinuation() -> {
                    debug { "P3 - 3" }
                    out.outputReplacementChar()
                    return 0
                }
            }
            if (!b2.isContinuation()) {
                debug { "P3 - 4" }
                out.outputReplacementChar()
                return 1
            }
            debug { "P3 - F" }
            val codePoint = -0x01e080 xor b2 xor (b1 shl 6) xor (b0 shl 12)
            out.output(codePoint.toChar())
            return 3
        }

        /**
         * Returns:
         *  - `0`: Drop b0
         *  - `1`: Drop b0, b1
         *  - `2`: Drop b0, b1, b2
         *  - `4`: Success
         * */
        private fun Int.process4(b0: Int, b1: Int, b2: Int): Int {
            val b3 = this
            when {
                b0 and 0x0f == 0x00 -> if (b1 and 0xf0 <= 0x80) {
                    debug { "P4 - 1" }
                    // Non-shortest form
                    out.outputReplacementChar()
                    return 0
                }
                b0 and 0x0f == 0x04 -> if (b1 and 0xf0 != 0x80) {
                    debug { "P4 - 2" }
                    // Exceeds Unicode code point maximum
                    out.outputReplacementChar()
                    return 0
                }
                b0 and 0x0f > 0x04 -> {
                    debug { "P4 - 3" }
                    out.outputReplacementChar()
                    return 0
                }
            }
            if (!b1.isContinuation()) {
                debug { "P4 - 4" }
                out.outputReplacementChar()
                return 0
            }
            if (!b2.isContinuation()) {
                debug { "P4 - 5" }
                out.outputReplacementChar()
                return 1
            }
            if (!b3.isContinuation()) {
                debug { "P4 - 6" }
                out.outputReplacementChar()
                return 2
            }
            debug { "P4 - F" }
            val codePoint = 0x381f80 xor b3 xor (b2 shl 6) xor (b1 shl 12) xor (b0 shl 18)
            val hi = (codePoint  -  0x010000) shr 10 or 0xd800
            val lo = (codePoint and 0x0003ff)        or 0xdc00
            out.output(hi.toChar())
            out.output(lo.toChar())
            return 4
        }

        private inline fun Int.isContinuation(): Boolean = this and 0xc0 == 0x80

        // TODO: REMOVE
        @OptIn(ExperimentalContracts::class)
        private inline fun <T: Any> T.debug(block: (it: T) -> String): T {
            contract { callsInPlace(block, InvocationKind.AT_MOST_ONCE) }
//            println(block(this))
            return this
        }
    }

    private constructor(config: Config): super(config)
}
