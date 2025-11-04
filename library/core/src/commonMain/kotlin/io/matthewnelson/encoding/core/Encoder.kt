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

package io.matthewnelson.encoding.core

import io.matthewnelson.encoding.core.internal.closedException
import io.matthewnelson.encoding.core.internal.encode
import io.matthewnelson.encoding.core.internal.encodeOutMaxSizeOrFail
import io.matthewnelson.encoding.core.util.LineBreakOutFeed
import kotlin.jvm.JvmField
import kotlin.jvm.JvmStatic

/**
 * Encode things.
 *
 * @see [EncoderDecoder]
 * @see [encodeToString]
 * @see [encodeToCharArray]
 * @see [encodeToByteArray]
 * @see [Encoder.Feed]
 * */
public sealed class Encoder<C: EncoderDecoder.Config>(config: C): Decoder<C>(config) {

    /**
     * Creates a new [Encoder.Feed], outputting encoded data to the supplied [Encoder.OutFeed].
     *
     * **NOTE:** The supplied [Encoder.OutFeed] will be wrapped in [LineBreakOutFeed] (if not
     * already one) when [EncoderDecoder.Config.lineBreakInterval] is greater than `0`.
     *
     * e.g.
     *
     *     val sb = StringBuilder()
     *     myEncoder.newEncoderFeed { encodedChar ->
     *         sb.append(encodedChar)
     *     }.use { feed ->
     *         "Hello World!"
     *             .encodeToByteArray()
     *             .forEach { b -> feed.consume(b) }
     *     }
     *     println(sb.toString())
     *
     * @see [Encoder.Feed]
     * @see [LineBreakOutFeed]
     * */
    public fun newEncoderFeed(out: Encoder.OutFeed): Encoder<C>.Feed {
        val outFeed = if (config.lineBreakInterval > 0 && out !is LineBreakOutFeed) {
            LineBreakOutFeed(config.lineBreakInterval, out)
        } else {
            out
        }
        return newEncoderFeedProtected(outFeed)
    }

    protected abstract fun newEncoderFeedProtected(out: Encoder.OutFeed): Encoder<C>.Feed

    /**
     * Data to encode is fed into [consume] and, as the [Encoder.Feed]'s
     * buffer fills, encoded data is output to the supplied
     * [Encoder.OutFeed]. This allows for a "lazy" encode, or streaming
     * of encoded data.
     *
     * Once all data has been fed through [consume], call [doFinal] to
     * process remaining data in the [Encoder.Feed] buffer. Alternatively,
     * utilize the [use] extension function (highly recommended)
     * which will call [doFinal] (or [close] if there was an error with
     * encoding) for you.
     *
     * @see [newEncoderFeed]
     * @see [use]
     * @see [EncoderDecoder.Feed]
     * @see [EncoderDecoder.Feed.doFinal]
     * */
    public abstract inner class Feed: EncoderDecoder.Feed<C> {

        private val out: Encoder.OutFeed

        // TODO: Deprecate?
        public constructor(): this(out = Encoder.OutFeed.NoOp)
        public constructor(out: Encoder.OutFeed): super(this@Encoder.config) { this.out = out }

        private var isClosed = false

        /**
         * Updates the [Encoder.Feed] with a new byte to encode.
         *
         * @throws [EncodingException] if [isClosed] is true.
         * */
        @Throws(EncodingException::class)
        public fun consume(input: Byte) {
            if (isClosed) throw closedException()

            try {
                // should not throw exception, but just
                // in case, we close the Feed.
                consumeProtected(input)
            } catch (t: Throwable) {
                close()
                throw t
            }
        }

        /**
         * Flushes the buffered input and performs any final encoding
         * operations without closing the [Feed].
         *
         * @see [EncoderDecoder.Feed.flush]
         * @throws [EncodingException] if [isClosed] is true.
         * */
        @Throws(EncodingException::class)
        public final override fun flush() {
            if (isClosed) throw closedException()

            try {
                // should not throw exception, but just
                // in case, we close the Feed.
                doFinalProtected()
                if (out is LineBreakOutFeed) out.reset()
            } catch (t: Throwable) {
                close()
                throw t
            }
        }

        public final override fun close() { isClosed = true }
        public final override fun isClosed(): Boolean = isClosed

        protected abstract fun consumeProtected(input: Byte)
        protected abstract override fun doFinalProtected()

        /** @suppress */
        public final override fun toString(): String = "${this@Encoder}.Encoder.Feed@${hashCode()}"
    }

    /**
     * A callback for returning encoded characters as they
     * are produced by [Encoder.Feed].
     *
     * @see [newEncoderFeed]
     * */
    public fun interface OutFeed {

        public fun output(encoded: Char)

        public companion object {

            /**
             * A static, non-operational instance of [OutFeed].
             * */
            @JvmField
            public val NoOp: OutFeed = OutFeed {}
        }
    }

    public companion object {

        /**
         * Encodes a [ByteArray] for the provided [encoder] and
         * returns the encoded data in the form of a [String].
         *
         * @throws [EncodingSizeException] if the encoded output
         *   exceeds [Int.MAX_VALUE].
         * */
        @JvmStatic
        @Throws(EncodingSizeException::class)
        public fun ByteArray.encodeToString(encoder: Encoder<*>): String {
            return encoder.encodeOutMaxSizeOrFail(size) { maxSize ->
                val sb = StringBuilder(maxSize)
                encoder.encode(this, sb::append)
                val length = sb.length
                val result = sb.toString()
                if (encoder.config.backFillBuffers) {
                    // Some implementations of StringBuilder do not overwrite buffered
                    // data when clear() is used. Must set to 0 length and do manually.
                    sb.setLength(0)
                    repeat(length) { sb.append(' ') }
                }
                result
            }
        }

        /**
         * Encodes a [ByteArray] for the provided [encoder] and
         * returns the encoded data in the form of a [CharArray].
         *
         * @throws [EncodingSizeException] if the encoded output
         *   exceeds [Int.MAX_VALUE].
         * */
        @JvmStatic
        @Throws(EncodingSizeException::class)
        public fun ByteArray.encodeToCharArray(encoder: Encoder<*>): CharArray {
            return encoder.encodeOutMaxSizeOrFail(size) block@ { maxSize ->
                var i = 0
                val a = CharArray(maxSize)
                encoder.encode(this) { c -> a[i++] = c }
                if (i == maxSize) return@block a
                val copy = a.copyOf(i)
                if (encoder.config.backFillBuffers) {
                    a.fill(' ', 0, i)
                }
                copy
            }
        }

        /**
         * DEPRECATED
         * @suppress
         * */
        @JvmStatic
        @Throws(EncodingSizeException::class)
        @Deprecated(
            message = "Should not utilize. Underlying Char to Byte conversion can produce incorrect results",
            level = DeprecationLevel.ERROR,
        )
        public fun ByteArray.encodeToByteArray(encoder: Encoder<*>): ByteArray {
            return encoder.encodeOutMaxSizeOrFail(size) block@ { maxSize ->
                var i = 0
                val a = ByteArray(maxSize)
                encoder.encode(this) { char -> a[i++] = char.code.toByte() }
                if (i == maxSize) return@block a
                val copy = a.copyOf(i)
                if (encoder.config.backFillBuffers) {
                    copy.fill(0, 0, i)
                }
                copy
            }
        }
    }
}
