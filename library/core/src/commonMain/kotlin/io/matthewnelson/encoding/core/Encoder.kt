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
@file:Suppress("LocalVariableName", "PropertyName", "RemoveRedundantQualifierName", "RedundantVisibilityModifier")

package io.matthewnelson.encoding.core

import io.matthewnelson.encoding.core.internal.closedException
import io.matthewnelson.encoding.core.internal.encode
import io.matthewnelson.encoding.core.internal.encodeOutMaxSizeOrFail
import io.matthewnelson.encoding.core.util.LineBreakOutFeed
import io.matthewnelson.encoding.core.util.wipe
import kotlin.jvm.JvmField
import kotlin.jvm.JvmName
import kotlin.jvm.JvmStatic
import kotlin.jvm.JvmSynthetic

/**
 * Encode things.
 *
 * @see [EncoderDecoder]
 * @see [encodeToString]
 * @see [encodeToCharArray]
 * @see [Encoder.Feed]
 * */
public sealed class Encoder<C: EncoderDecoder.Config>(config: C): Decoder<C>(config) {

    /**
     * Creates a new [Encoder.Feed], outputting encoded data to the supplied [Encoder.OutFeed].
     *
     * **NOTE:** The supplied [Encoder.OutFeed] will be wrapped in a [LineBreakOutFeed] (if not
     * already one) when [EncoderDecoder.Config.lineBreakInterval] is greater than `0`.
     *
     * e.g.
     *
     *     val sb = StringBuilder()
     *
     *     // Alternatively use newEncoderFeed(sb::append)
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
     * @see [EncoderDecoder.Config.lineBreakInterval]
     * @see [EncoderDecoder.Config.lineBreakResetOnFlush]
     * */
    public fun newEncoderFeed(out: Encoder.OutFeed): Encoder<C>.Feed {
        val _out = if (config.lineBreakInterval > 0 && out !is LineBreakOutFeed) {
            LineBreakOutFeed(config.lineBreakInterval, config.lineBreakResetOnFlush, out)
        } else {
            out
        }
        return newEncoderFeedProtected(_out)
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
     * @see [use]
     * @see [newEncoderFeed]
     * @see [EncoderDecoder.Feed]
     * @see [EncoderDecoder.Feed.doFinal]
     * */
    public abstract inner class Feed: EncoderDecoder.Feed<C> {

        private var _isClosed = false

        /**
         * For implementations to pass in as a constructor argument and reference
         * while performing encoding operations. Upon [close] being called, this
         * is set to the [Encoder.OutFeed.NoOp] instance which ensures any local
         * object references that the initial [OutFeed] has are not leaked and can
         * be promptly GCd.
         *
         * **NOTE:** [LineBreakOutFeed.resetOnFlush] functionality relies on [Encoder]
         * implementations instantiating their [Feed] with the [Encoder.OutFeed]
         * provided to [newEncoderFeedProtected].
         * */
        @get:JvmName("_out")
        protected var _out: Encoder.OutFeed
            private set

        // pass Encoder.OutFeed.NoOp if not wanting to utilize _out at all.
        public constructor(_out: Encoder.OutFeed): super(this@Encoder.config) { this._out = _out }

        public final override fun isClosed(): Boolean = _isClosed

        /**
         * Updates the [Encoder.Feed] with a new byte to encode.
         *
         * @throws [EncodingException] If [isClosed] is `true`, or the [Feed] is configured
         *   to reject something, such as `UTF-8` byte to text transformations rejecting
         *   invalid byte sequences.
         * */
        @Throws(EncodingException::class)
        public fun consume(input: Byte) {
            if (_isClosed) throw closedException()

            try {
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
         *
         * @throws [EncodingException] If [isClosed] is `true`, or the [Feed] is configured
         *   to reject something, such as `UTF-8` byte to text transformations rejecting
         *   invalid byte sequences.
         * */
        @Throws(EncodingException::class)
        public final override fun flush() {
            if (_isClosed) throw closedException()

            try {
                doFinalProtected()
                val lbf = (_out as? LineBreakOutFeed) ?: return
                if (lbf.resetOnFlush) lbf.reset()
            } catch (t: Throwable) {
                close()
                throw t
            }
        }

        public final override fun close() {
            _isClosed = true
            _out = OutFeed.NoOp
        }

        protected abstract fun consumeProtected(input: Byte)
        protected abstract override fun doFinalProtected()

        @JvmSynthetic
        internal fun markAsClosed() { _isClosed = true }

        /** @suppress */
        public final override fun toString(): String = "${this@Encoder}.Encoder.Feed@${hashCode()}"

        /**
         * DEPRECATED since `2.6.0`
         * @suppress
         * */
        @Deprecated(
            message = "Parameter _out: Encoder.OutFeed was added. Use the new constructor.",
            level = DeprecationLevel.WARNING,
        )
        public constructor(): this(_out = Encoder.OutFeed.NoOp)
    }

    /**
     * A callback for returning encoded characters as they
     * are produced by [Encoder.Feed].
     *
     * @see [newEncoderFeed]
     * @see [NoOp]
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
         * Encode a [ByteArray].
         *
         * @param [encoder] The [Encoder] to use.
         *
         * @return The [String] of encoded data.
         *
         * @see [encodeToCharArray]
         *
         * @throws [EncodingException] If the [encoder] is configured to reject something,
         *   such as `UTF-8` byte to text transformations rejecting invalid byte sequences.
         * @throws [EncodingSizeException] If the encoded output would exceed [Int.MAX_VALUE].
         * */
        @JvmStatic
        public fun ByteArray.encodeToString(encoder: Encoder<*>): String {
            return encoder.encodeOutMaxSizeOrFail(size) { maxSize ->
                val sb = StringBuilder(maxSize)
                encoder.encode(this, sb::append)
                val result = sb.toString()
                if (encoder.config.backFillBuffers) sb.wipe()
                result
            }
        }

        /**
         * Encode a [ByteArray].
         *
         * @param [encoder] The [Encoder] to use.
         *
         * @return The [CharArray] of encoded data.
         *
         * @see [encodeToString]
         *
         * @throws [EncodingException] If the [encoder] is configured to reject something,
         *   such as `UTF-8` byte to text transformations rejecting invalid byte sequences.
         * @throws [EncodingSizeException] If the encoded output exceeds [Int.MAX_VALUE].
         * */
        @JvmStatic
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
         * DEPRECATED since `2.3.0`
         * @throws [EncodingException] If the [encoder] is configured to reject something,
         *   such as `UTF-8` byte to text transformations rejecting invalid byte sequences.
         * @throws [EncodingSizeException] if the encoded output exceeds [Int.MAX_VALUE].
         * @suppress
         * */
        @JvmStatic
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
                    a.fill(0, 0, i)
                }
                copy
            }
        }
    }
}
