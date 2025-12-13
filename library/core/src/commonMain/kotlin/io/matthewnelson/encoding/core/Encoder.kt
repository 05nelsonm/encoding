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
@file:Suppress("LocalVariableName", "NOTHING_TO_INLINE", "PropertyName", "RemoveRedundantQualifierName", "RedundantVisibilityModifier")

package io.matthewnelson.encoding.core

import io.matthewnelson.encoding.core.Encoder.OutFeed
import io.matthewnelson.encoding.core.EncoderDecoder.Companion.DEFAULT_BUFFER_SIZE
import io.matthewnelson.encoding.core.internal.checkBounds
import io.matthewnelson.encoding.core.internal.closedException
import io.matthewnelson.encoding.core.internal.encodeUnsafe
import io.matthewnelson.encoding.core.internal.encodeBufferedUnsafe
import io.matthewnelson.encoding.core.util.LineBreakOutFeed
import io.matthewnelson.encoding.core.util.wipe
import kotlin.coroutines.cancellation.CancellationException
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
 * @see [encodeBuffered]
 * @see [encodeBufferedAsync]
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
         * @see [encodeBuffered]
         * @see [encodeBufferedAsync]
         *
         * @throws [EncodingException] If the [encoder] is configured to reject something,
         *   such as `UTF-8` byte to text transformations rejecting invalid byte sequences.
         * @throws [EncodingSizeException] If [EncoderDecoder.Config.encodeOutMaxSize]
         *   throws an exception (i.e. output would exceed [Int.MAX_VALUE]).
         * */
        @JvmStatic
        public fun ByteArray.encodeToString(encoder: Encoder<*>): String {
            return encodeToStringUnsafe(encoder, 0, size)
        }

        /**
         * Encode [len] number of bytes from the array, starting at index [offset].
         *
         * @param [encoder] The [Encoder] to use.
         * @param [offset] The index in the array to start at.
         * @param [len] The number of bytes, starting at index [offset].
         *
         * @return The [String] of encoded data.
         *
         * @see [encodeToCharArray]
         * @see [encodeBuffered]
         * @see [encodeBufferedAsync]
         *
         * @throws [EncodingException] If the [encoder] is configured to reject something,
         *   such as `UTF-8` byte to text transformations rejecting invalid byte sequences.
         * @throws [EncodingSizeException] If [EncoderDecoder.Config.encodeOutMaxSize]
         *   throws an exception (i.e. output would exceed [Int.MAX_VALUE]).
         * @throws [IndexOutOfBoundsException] If [offset] or [len] are inappropriate.
         * */
        @JvmStatic
        public fun ByteArray.encodeToString(encoder: Encoder<*>, offset: Int, len: Int): String {
            checkBounds(offset, len)
            return encodeToStringUnsafe(encoder, offset, len)
        }

        /**
         * Encode a [ByteArray].
         *
         * @param [encoder] The [Encoder] to use.
         *
         * @return The [CharArray] of encoded data.
         *
         * @see [encodeToString]
         * @see [encodeBuffered]
         * @see [encodeBufferedAsync]
         *
         * @throws [EncodingException] If the [encoder] is configured to reject something,
         *   such as `UTF-8` byte to text transformations rejecting invalid byte sequences.
         * @throws [EncodingSizeException] If [EncoderDecoder.Config.encodeOutMaxSize]
         *   throws an exception (i.e. output would exceed [Int.MAX_VALUE]).
         * */
        @JvmStatic
        public fun ByteArray.encodeToCharArray(encoder: Encoder<*>): CharArray {
            return encodeToCharArrayUnsafe(encoder, 0, size)
        }

        /**
         * Encode [len] number of bytes from the array, starting at index [offset].
         *
         * @param [encoder] The [Encoder] to use.
         * @param [offset] The index in the array to start at.
         * @param [len] The number of bytes, starting at index [offset].
         *
         * @return The [CharArray] of encoded data.
         *
         * @see [encodeToCharArray]
         * @see [encodeBuffered]
         * @see [encodeBufferedAsync]
         *
         * @throws [EncodingException] If the [encoder] is configured to reject something,
         *   such as `UTF-8` byte to text transformations rejecting invalid byte sequences.
         * @throws [EncodingSizeException] If [EncoderDecoder.Config.encodeOutMaxSize]
         *   throws an exception (i.e. output would exceed [Int.MAX_VALUE]).
         * @throws [IndexOutOfBoundsException] If [offset] or [len] are inappropriate.
         * */
        @JvmStatic
        public fun ByteArray.encodeToCharArray(encoder: Encoder<*>, offset: Int, len: Int): CharArray {
            checkBounds(offset, len)
            return encodeToCharArrayUnsafe(encoder, offset, len)
        }

        /**
         * Encode a [ByteArray] using a buffer of maximum size [DEFAULT_BUFFER_SIZE].
         *
         * The encoding operation will allocate a single buffer, streaming encoded
         * characters to it and flushing to [action] when needed. If the
         * pre-calculated size returned by [EncoderDecoder.Config.encodeOutMaxSize]
         * is less than or equal to the [DEFAULT_BUFFER_SIZE], then a buffer of that
         * size will be allocated and [action] is only invoked once (single-shot
         * encoding). In the event that [EncoderDecoder.Config.encodeOutMaxSize]
         * throws its [EncodingSizeException] (i.e. encoding would exceed [Int.MAX_VALUE])
         * while [throwOnOverflow] is `false`, or its return value is greater than
         * [DEFAULT_BUFFER_SIZE], then this function will always stream encode to
         * a buffer while flushing to [action] until the encoding operation has
         * completed.
         *
         * **NOTE:** Documented exceptions thrown by this function do not include those
         * for which [action] may throw.
         *
         * **NOTE:** The [Encoder] implementation must be compatible with version `2.6.0+`
         * APIs and define a [EncoderDecoder.Config.maxEncodeEmit]. If the value is `-1` (i.e.
         * it has not updated to the new API yet), then this function will fail with an
         * [EncodingException]. All implementations provided by this library have been updated
         * to meet the API requirement; only [EncoderDecoder] implementations external to this
         * library that have not updated yet may fail when using them with [encodeBuffered]
         * and [encodeBufferedAsync] APIs.
         *
         * @param [encoder] The [Encoder] to use.
         * @param [throwOnOverflow] If `true` and [EncoderDecoder.Config.encodeOutMaxSize]
         *   throws an [EncodingSizeException], it will be re-thrown. If `false`, the exception
         *   is ignored and stream encoding to the buffer will continue.
         * @param [action] The function to flush the buffer to; a destination to "write"
         *   encoded data to whereby `len` is the number of characters within `buf`, starting
         *   at index `offset`, to "write".
         *
         * @return The number of encoded characters.
         *
         * @see [encodeToString]
         * @see [encodeToCharArray]
         * @see [encodeBufferedAsync]
         *
         * @throws [EncodingException] If the [encoder] is configured to reject something,
         *   such as `UTF-8` byte to text transformations rejecting invalid byte sequences.
         * @throws [EncodingSizeException] If [EncoderDecoder.Config.encodeOutMaxSize]
         *   threw its exception (i.e. output would exceed [Int.MAX_VALUE]) and
         *   [throwOnOverflow] is `true`.
         * */
        @JvmStatic
        public inline fun ByteArray.encodeBuffered(
            encoder: Encoder<*>,
            throwOnOverflow: Boolean,
            noinline action: (buf: CharArray, offset: Int, len: Int) -> Unit,
        ): Long = encodeBuffered(encoder, throwOnOverflow, DEFAULT_BUFFER_SIZE, action)

        /**
         * Encode [len] number of bytes from the array, starting at index [offset], using
         * a buffer of maximum size [DEFAULT_BUFFER_SIZE].
         *
         * The encoding operation will allocate a single buffer, streaming encoded
         * characters to it and flushing to [action] when needed. If the pre-calculated
         * size returned by [EncoderDecoder.Config.encodeOutMaxSize] is less than or
         * equal to the [DEFAULT_BUFFER_SIZE], then a buffer of that size will be allocated
         * and [action] is only invoked once (single-shot encoding). In the event that
         * [EncoderDecoder.Config.encodeOutMaxSize] throws its [EncodingSizeException]
         * (i.e. encoding would exceed [Int.MAX_VALUE]) while [throwOnOverflow] is `false`,
         * or its return value is greater than [DEFAULT_BUFFER_SIZE], then this function
         * will always stream encode to a buffer while flushing to [action] until the
         * encoding operation has completed.
         *
         * **NOTE:** Documented exceptions thrown by this function do not include those
         * for which [action] may throw.
         *
         * **NOTE:** The [Encoder] implementation must be compatible with version `2.6.0+`
         * APIs and define a [EncoderDecoder.Config.maxEncodeEmit]. If the value is `-1` (i.e.
         * it has not updated to the new API yet), then this function will fail with an
         * [EncodingException]. All implementations provided by this library have been updated
         * to meet the API requirement; only [EncoderDecoder] implementations external to this
         * library that have not updated yet may fail when using them with [encodeBuffered]
         * and [encodeBufferedAsync] APIs.
         *
         * @param [encoder] The [Encoder] to use.
         * @param [throwOnOverflow] If `true` and [EncoderDecoder.Config.encodeOutMaxSize]
         *   throws an [EncodingSizeException], it will be re-thrown. If `false`, the exception
         *   is ignored and stream encoding to the buffer will continue.
         * @param [offset] The index in the array to start at.
         * @param [len] The number of bytes, starting at index [offset].
         * @param [action] The function to flush the buffer to; a destination to "write"
         *   encoded data to whereby `len` is the number of characters within `buf`, starting
         *   at index `offset`, to "write".
         *
         * @return The number of encoded characters.
         *
         * @see [encodeToString]
         * @see [encodeToCharArray]
         * @see [encodeBufferedAsync]
         *
         * @throws [EncodingException] If the [encoder] is configured to reject something,
         *   such as `UTF-8` byte to text transformations rejecting invalid byte sequences.
         * @throws [EncodingSizeException] If [EncoderDecoder.Config.encodeOutMaxSize]
         *   threw its exception (i.e. output would exceed [Int.MAX_VALUE]) and
         *   [throwOnOverflow] is `true`.
         * @throws [IndexOutOfBoundsException] If [offset] or [len] are inappropriate.
         * */
        @JvmStatic
        public inline fun ByteArray.encodeBuffered(
            encoder: Encoder<*>,
            throwOnOverflow: Boolean,
            offset: Int,
            len: Int,
            noinline action: (buf: CharArray, offset: Int, len: Int) -> Unit,
        ): Long = encodeBuffered(encoder, throwOnOverflow, offset, len, DEFAULT_BUFFER_SIZE, action)

        /**
         * Encode a [ByteArray] using a buffer of maximum size [maxBufSize].
         *
         * The encoding operation will allocate a single buffer, streaming encoded
         * characters to it and flushing to [action] when needed. If the
         * pre-calculated size returned by [EncoderDecoder.Config.encodeOutMaxSize]
         * is less than or equal to the [maxBufSize], then a buffer of that
         * size will be allocated and [action] is only invoked once (single-shot
         * encoding). In the event that [EncoderDecoder.Config.encodeOutMaxSize]
         * throws its [EncodingSizeException] (i.e. encoding would exceed [Int.MAX_VALUE])
         * while [throwOnOverflow] is `false`, or its return value is greater than
         * [maxBufSize], then this function will always stream encode to
         * a buffer while flushing to [action] until the encoding operation has
         * completed.
         *
         * **NOTE:** Documented exceptions thrown by this function do not include those
         * for which [action] may throw.
         *
         * **NOTE:** The [Encoder] implementation must be compatible with version `2.6.0+`
         * APIs and define a [EncoderDecoder.Config.maxEncodeEmit]. If the value is `-1` (i.e.
         * it has not updated to the new API yet), then this function will fail with an
         * [EncodingException]. All implementations provided by this library have been updated
         * to meet the API requirement; only [EncoderDecoder] implementations external to this
         * library that have not updated yet may fail when using them with [encodeBuffered]
         * and [encodeBufferedAsync] APIs.
         *
         * @param [encoder] The [Encoder] to use.
         * @param [throwOnOverflow] If `true` and [EncoderDecoder.Config.encodeOutMaxSize]
         *   throws an [EncodingSizeException], it will be re-thrown. If `false`, the exception
         *   is ignored and stream encoding to the buffer will continue.
         * @param [maxBufSize] The maximum size buffer this function will allocate. Must
         *   be greater than [EncoderDecoder.Config.maxEncodeEmitWithLineBreak].
         * @param [action] The function to flush the buffer to; a destination to "write"
         *   encoded data to whereby `len` is the number of characters within `buf`, starting
         *   at index `offset`, to "write".
         *
         * @return The number of encoded characters.
         *
         * @see [encodeToString]
         * @see [encodeToCharArray]
         * @see [encodeBufferedAsync]
         *
         * @throws [EncodingException] If the [encoder] is configured to reject something,
         *   such as `UTF-8` byte to text transformations rejecting invalid byte sequences.
         * @throws [EncodingSizeException] If [EncoderDecoder.Config.encodeOutMaxSize]
         *   threw its exception (i.e. output would exceed [Int.MAX_VALUE]) and
         *   [throwOnOverflow] is `true`.
         * @throws [IllegalArgumentException] If [maxBufSize] is less than or equal to
         *   [EncoderDecoder.Config.maxEncodeEmitWithLineBreak].
         * */
        @JvmStatic
        public fun ByteArray.encodeBuffered(
            encoder: Encoder<*>,
            throwOnOverflow: Boolean,
            maxBufSize: Int,
            action: (buf: CharArray, offset: Int, len: Int) -> Unit,
        ): Long = encoder.encodeBufferedUnsafe(
            data = this,
            offset = 0,
            len = size,
            buf = null,
            maxBufSize = maxBufSize,
            throwOnOverflow = throwOnOverflow,
            _action = action,
        )

        /**
         * Encode [len] number of bytes from the array, starting at index [offset], using
         * a buffer of maximum size [maxBufSize].
         *
         * The encoding operation will allocate a single buffer, streaming encoded characters
         * to it and flushing to [action] when needed. If the pre-calculated size returned
         * by [EncoderDecoder.Config.encodeOutMaxSize] is less than or equal to the [maxBufSize],
         * then a buffer of that size will be allocated and [action] is only invoked once
         * (single-shot encoding). In the event that [EncoderDecoder.Config.encodeOutMaxSize]
         * throws its [EncodingSizeException] (i.e. encoding would exceed [Int.MAX_VALUE])
         * while [throwOnOverflow] is `false`, or its return value is greater than [maxBufSize],
         * then this function will always stream encode to a buffer while flushing to [action]
         * until the encoding operation has completed.
         *
         * **NOTE:** Documented exceptions thrown by this function do not include those
         * for which [action] may throw.
         *
         * **NOTE:** The [Encoder] implementation must be compatible with version `2.6.0+`
         * APIs and define a [EncoderDecoder.Config.maxEncodeEmit]. If the value is `-1` (i.e.
         * it has not updated to the new API yet), then this function will fail with an
         * [EncodingException]. All implementations provided by this library have been updated
         * to meet the API requirement; only [EncoderDecoder] implementations external to this
         * library that have not updated yet may fail when using them with [encodeBuffered]
         * and [encodeBufferedAsync] APIs.
         *
         * @param [encoder] The [Encoder] to use.
         * @param [throwOnOverflow] If `true` and [EncoderDecoder.Config.encodeOutMaxSize]
         *   throws an [EncodingSizeException], it will be re-thrown. If `false`, the exception
         *   is ignored and stream encoding to the buffer will continue.
         * @param [offset] The index in the array to start at.
         * @param [len] The number of bytes, starting at index [offset].
         * @param [maxBufSize] The maximum size buffer this function will allocate. Must
         *   be greater than [EncoderDecoder.Config.maxEncodeEmitWithLineBreak].
         * @param [action] The function to flush the buffer to; a destination to "write"
         *   encoded data to whereby `len` is the number of characters within `buf`, starting
         *   at index `offset`, to "write".
         *
         * @return The number of encoded characters.
         *
         * @see [encodeToString]
         * @see [encodeToCharArray]
         * @see [encodeBufferedAsync]
         *
         * @throws [EncodingException] If the [encoder] is configured to reject something,
         *   such as `UTF-8` byte to text transformations rejecting invalid byte sequences.
         * @throws [EncodingSizeException] If [EncoderDecoder.Config.encodeOutMaxSize]
         *   threw its exception (i.e. output would exceed [Int.MAX_VALUE]) and
         *   [throwOnOverflow] is `true`.
         * @throws [IllegalArgumentException] If [maxBufSize] is less than or equal to
         *   [EncoderDecoder.Config.maxEncodeEmitWithLineBreak].
         * @throws [IndexOutOfBoundsException] If [offset] or [len] are inappropriate.
         * */
        @JvmStatic
        public fun ByteArray.encodeBuffered(
            encoder: Encoder<*>,
            throwOnOverflow: Boolean,
            offset: Int,
            len: Int,
            maxBufSize: Int,
            action: (buf: CharArray, offset: Int, len: Int) -> Unit,
        ): Long {
            checkBounds(offset, len)
            return encoder.encodeBufferedUnsafe(
                data = this,
                offset = offset,
                len = len,
                buf = null,
                maxBufSize = maxBufSize,
                throwOnOverflow = throwOnOverflow,
                _action = action,
            )
        }

        /**
         * Encode a [ByteArray] using the provided pre-allocated, reusable, [buf] array.
         *
         * The encoding operation will stream encoded characters to the provided [buf],
         * flushing to [action] when needed. If the pre-calculated size returned by
         * [EncoderDecoder.Config.encodeOutMaxSize] is less than or equal to the [buf]
         * size, then [action] is only invoked once (single-shot encoding). In the event
         * that [EncoderDecoder.Config.encodeOutMaxSize] throws its [EncodingSizeException]
         * (i.e. encoding would exceed [Int.MAX_VALUE]) while [throwOnOverflow] is `false`,
         * or its return value is greater than [buf] size, then this function will always
         * stream encode to a buffer while flushing to [action] until the encoding operation
         * has completed.
         *
         * **NOTE:** Documented exceptions thrown by this function do not include those
         * for which [action] may throw.
         *
         * **NOTE:** If [EncoderDecoder.Config.backFillBuffers] is `true`, provided [buf]
         * array will be back-filled with null character `\u0000` upon encoding completion.
         *
         * **NOTE:** The [Encoder] implementation must be compatible with version `2.6.0+`
         * APIs and define a [EncoderDecoder.Config.maxEncodeEmit]. If the value is `-1` (i.e.
         * it has not updated to the new API yet), then this function will fail with an
         * [EncodingException]. All implementations provided by this library have been updated
         * to meet the API requirement; only [EncoderDecoder] implementations external to this
         * library that have not updated yet may fail when using them with [encodeBuffered]
         * and [encodeBufferedAsync] APIs.
         *
         * @param [encoder] The [Encoder] to use.
         * @param [throwOnOverflow] If `true` and [EncoderDecoder.Config.encodeOutMaxSize]
         *   throws an [EncodingSizeException], it will be re-thrown. If `false`, the exception
         *   is ignored and stream encoding to the buffer will continue.
         * @param [buf] The pre-allocated array to use as the buffer. Its size must
         *   be greater than [EncoderDecoder.Config.maxEncodeEmitWithLineBreak].
         * @param [action] The function to flush the buffer to; a destination to "write"
         *   encoded data to whereby `len` is the number of characters within `buf`, starting
         *   at index `offset`, to "write".
         *
         * @return The number of encoded characters.
         *
         * @see [encodeToString]
         * @see [encodeToCharArray]
         * @see [encodeBufferedAsync]
         *
         * @throws [EncodingException] If the [encoder] is configured to reject something,
         *   such as `UTF-8` byte to text transformations rejecting invalid byte sequences.
         * @throws [EncodingSizeException] If [EncoderDecoder.Config.encodeOutMaxSize]
         *   threw its exception (i.e. output would exceed [Int.MAX_VALUE]) and
         *   [throwOnOverflow] is `true`.
         * @throws [IllegalArgumentException] If [buf] size is less than or equal to
         *   [EncoderDecoder.Config.maxEncodeEmitWithLineBreak].
         * */
        @JvmStatic
        public fun ByteArray.encodeBuffered(
            encoder: Encoder<*>,
            throwOnOverflow: Boolean,
            buf: CharArray,
            action: (buf: CharArray, offset: Int, len: Int) -> Unit,
        ): Long = encoder.encodeBufferedUnsafe(
            data = this,
            offset = 0,
            len = size,
            buf = buf,
            maxBufSize = buf.size,
            throwOnOverflow = throwOnOverflow,
            _action = action,
        )

        /**
         * Encode [len] number of bytes from the array, starting at index [offset], using
         * the provided pre-allocated, reusable, [buf] array.
         *
         * The encoding operation will stream encoded characters to the provided [buf],
         * flushing to [action] when needed. If the pre-calculated size returned by
         * [EncoderDecoder.Config.encodeOutMaxSize] is less than or equal to the [buf]
         * size, then [action] is only invoked once (single-shot encoding). In the event
         * that [EncoderDecoder.Config.encodeOutMaxSize] throws its [EncodingSizeException]
         * (i.e. encoding would exceed [Int.MAX_VALUE]) while [throwOnOverflow] is `false`,
         * or its return value is greater than [buf] size, then this function will always
         * stream encode to a buffer while flushing to [action] until the encoding operation
         * has completed.
         *
         * **NOTE:** Documented exceptions thrown by this function do not include those
         * for which [action] may throw.
         *
         * **NOTE:** If [EncoderDecoder.Config.backFillBuffers] is `true`, provided [buf]
         * array will be back-filled with null character `\u0000` upon encoding completion.
         *
         * **NOTE:** The [Encoder] implementation must be compatible with version `2.6.0+`
         * APIs and define a [EncoderDecoder.Config.maxEncodeEmit]. If the value is `-1` (i.e.
         * it has not updated to the new API yet), then this function will fail with an
         * [EncodingException]. All implementations provided by this library have been updated
         * to meet the API requirement; only [EncoderDecoder] implementations external to this
         * library that have not updated yet may fail when using them with [encodeBuffered]
         * and [encodeBufferedAsync] APIs.
         *
         * @param [encoder] The [Encoder] to use.
         * @param [throwOnOverflow] If `true` and [EncoderDecoder.Config.encodeOutMaxSize]
         *   throws an [EncodingSizeException], it will be re-thrown. If `false`, the exception
         *   is ignored and stream encoding to the buffer will continue.
         * @param [offset] The index in the array to start at.
         * @param [len] The number of bytes, starting at index [offset].
         * @param [buf] The pre-allocated array to use as the buffer. Its size must
         *   be greater than [EncoderDecoder.Config.maxEncodeEmitWithLineBreak].
         * @param [action] The function to flush the buffer to; a destination to "write"
         *   encoded data to whereby `len` is the number of characters within `buf`, starting
         *   at index `offset`, to "write".
         *
         * @return The number of encoded characters.
         *
         * @see [encodeToString]
         * @see [encodeToCharArray]
         * @see [encodeBufferedAsync]
         *
         * @throws [EncodingException] If the [encoder] is configured to reject something,
         *   such as `UTF-8` byte to text transformations rejecting invalid byte sequences.
         * @throws [EncodingSizeException] If [EncoderDecoder.Config.encodeOutMaxSize]
         *   threw its exception (i.e. output would exceed [Int.MAX_VALUE]) and
         *   [throwOnOverflow] is `true`.
         * @throws [IllegalArgumentException] If [buf] size is less than or equal to
         *   [EncoderDecoder.Config.maxEncodeEmitWithLineBreak].
         * @throws [IndexOutOfBoundsException] If [offset] or [len] are inappropriate.
         * */
        @JvmStatic
        public fun ByteArray.encodeBuffered(
            encoder: Encoder<*>,
            throwOnOverflow: Boolean,
            offset: Int,
            len: Int,
            buf: CharArray,
            action: (buf: CharArray, offset: Int, len: Int) -> Unit,
        ): Long {
            checkBounds(offset, len)
            return encoder.encodeBufferedUnsafe(
                data = this,
                offset = offset,
                len = len,
                buf = buf,
                maxBufSize = buf.size,
                throwOnOverflow = throwOnOverflow,
                _action = action,
            )
        }

        /**
         * Encode a [ByteArray] using a buffer of maximum size [DEFAULT_BUFFER_SIZE].
         *
         * The encoding operation will allocate a single buffer, streaming encoded
         * characters to it and flushing to [action] when needed. If the
         * pre-calculated size returned by [EncoderDecoder.Config.encodeOutMaxSize]
         * is less than or equal to the [DEFAULT_BUFFER_SIZE], then a buffer of that
         * size will be allocated and [action] is only invoked once (single-shot
         * encoding). In the event that [EncoderDecoder.Config.encodeOutMaxSize]
         * throws its [EncodingSizeException] (i.e. encoding would exceed [Int.MAX_VALUE])
         * while [throwOnOverflow] is `false`, or its return value is greater than
         * [DEFAULT_BUFFER_SIZE], then this function will always stream encode to
         * a buffer while flushing to [action] until the encoding operation has
         * completed.
         *
         * **NOTE:** Documented exceptions thrown by this function do not include those
         * for which [action] may throw.
         *
         * **NOTE:** The [Encoder] implementation must be compatible with version `2.6.0+`
         * APIs and define a [EncoderDecoder.Config.maxEncodeEmit]. If the value is `-1` (i.e.
         * it has not updated to the new API yet), then this function will fail with an
         * [EncodingException]. All implementations provided by this library have been updated
         * to meet the API requirement; only [EncoderDecoder] implementations external to this
         * library that have not updated yet may fail when using them with [encodeBuffered]
         * and [encodeBufferedAsync] APIs.
         *
         * @param [encoder] The [Encoder] to use.
         * @param [throwOnOverflow] If `true` and [EncoderDecoder.Config.encodeOutMaxSize]
         *   throws an [EncodingSizeException], it will be re-thrown. If `false`, the exception
         *   is ignored and stream encoding to the buffer will continue.
         * @param [action] The suspend function to flush the buffer to; a destination to
         *   "write" encoded data to whereby `len` is the number of characters within `buf`,
         *   starting at index `offset`, to "write".
         *
         * @return The number of encoded characters.
         *
         * @see [encodeToString]
         * @see [encodeToCharArray]
         * @see [encodeBuffered]
         *
         * @throws [CancellationException]
         * @throws [EncodingException] If the [encoder] is configured to reject something,
         *   such as `UTF-8` byte to text transformations rejecting invalid byte sequences.
         * @throws [EncodingSizeException] If [EncoderDecoder.Config.encodeOutMaxSize]
         *   threw its exception (i.e. output would exceed [Int.MAX_VALUE]) and
         *   [throwOnOverflow] is `true`.
         *   and [throwOnOverflow] is `true`.
         * */
        @JvmStatic
        public suspend inline fun ByteArray.encodeBufferedAsync(
            encoder: Encoder<*>,
            throwOnOverflow: Boolean,
            noinline action: suspend (buf: CharArray, offset: Int, len: Int) -> Unit,
        ): Long = encodeBufferedAsync(encoder, throwOnOverflow, DEFAULT_BUFFER_SIZE, action)

        /**
         * Encode [len] number of bytes from the array, starting at index [offset], using a
         * buffer of maximum size [DEFAULT_BUFFER_SIZE].
         *
         * The encoding operation will allocate a single buffer, streaming encoded characters
         * to it and flushing to [action] when needed. If the pre-calculated size returned by
         * [EncoderDecoder.Config.encodeOutMaxSize] is less than or equal to the [DEFAULT_BUFFER_SIZE],
         * then a buffer of that size will be allocated and [action] is only invoked once
         * (single-shot encoding). In the event that [EncoderDecoder.Config.encodeOutMaxSize]
         * throws its [EncodingSizeException] (i.e. encoding would exceed [Int.MAX_VALUE]) while
         * [throwOnOverflow] is `false`, or its return value is greater than [DEFAULT_BUFFER_SIZE],
         * then this function will always stream encode to a buffer while flushing to [action]
         * until the encoding operation has completed.
         *
         * **NOTE:** Documented exceptions thrown by this function do not include those
         * for which [action] may throw.
         *
         * **NOTE:** The [Encoder] implementation must be compatible with version `2.6.0+`
         * APIs and define a [EncoderDecoder.Config.maxEncodeEmit]. If the value is `-1` (i.e.
         * it has not updated to the new API yet), then this function will fail with an
         * [EncodingException]. All implementations provided by this library have been updated
         * to meet the API requirement; only [EncoderDecoder] implementations external to this
         * library that have not updated yet may fail when using them with [encodeBuffered]
         * and [encodeBufferedAsync] APIs.
         *
         * @param [encoder] The [Encoder] to use.
         * @param [throwOnOverflow] If `true` and [EncoderDecoder.Config.encodeOutMaxSize]
         *   throws an [EncodingSizeException], it will be re-thrown. If `false`, the exception
         *   is ignored and stream encoding to the buffer will continue.
         * @param [offset] The index in the array to start at.
         * @param [len] The number of bytes, starting at index [offset].
         * @param [action] The suspend function to flush the buffer to; a destination to
         *   "write" encoded data to whereby `len` is the number of characters within `buf`,
         *   starting at index `offset`, to "write".
         *
         * @return The number of encoded characters.
         *
         * @see [encodeToString]
         * @see [encodeToCharArray]
         * @see [encodeBuffered]
         *
         * @throws [CancellationException]
         * @throws [EncodingException] If the [encoder] is configured to reject something,
         *   such as `UTF-8` byte to text transformations rejecting invalid byte sequences.
         * @throws [EncodingSizeException] If [EncoderDecoder.Config.encodeOutMaxSize]
         *   threw its exception (i.e. output would exceed [Int.MAX_VALUE]) and
         *   [throwOnOverflow] is `true`.
         *   and [throwOnOverflow] is `true`.
         * @throws [IndexOutOfBoundsException] If [offset] or [len] are inappropriate.
         * */
        @JvmStatic
        public suspend inline fun ByteArray.encodeBufferedAsync(
            encoder: Encoder<*>,
            throwOnOverflow: Boolean,
            offset: Int,
            len: Int,
            noinline action: suspend (buf: CharArray, offset: Int, len: Int) -> Unit,
        ): Long = encodeBufferedAsync(encoder, throwOnOverflow, offset, len, DEFAULT_BUFFER_SIZE, action)

        /**
         * Encode a [ByteArray] using a buffer of maximum size [maxBufSize].
         *
         * The encoding operation will allocate a single buffer, streaming encoded
         * characters to it and flushing to [action] when needed. If the
         * pre-calculated size returned by [EncoderDecoder.Config.encodeOutMaxSize]
         * is less than or equal to the [maxBufSize], then a buffer of that
         * size will be allocated and [action] is only invoked once (single-shot
         * encoding). In the event that [EncoderDecoder.Config.encodeOutMaxSize]
         * throws its [EncodingSizeException] (i.e. encoding would exceed [Int.MAX_VALUE])
         * while [throwOnOverflow] is `false`, or its return value is greater than
         * [maxBufSize], then this function will always stream encode to
         * a buffer while flushing to [action] until the encoding operation has
         * completed.
         *
         * **NOTE:** Documented exceptions thrown by this function do not include those
         * for which [action] may throw.
         *
         * **NOTE:** The [Encoder] implementation must be compatible with version `2.6.0+`
         * APIs and define a [EncoderDecoder.Config.maxEncodeEmit]. If the value is `-1` (i.e.
         * it has not updated to the new API yet), then this function will fail with an
         * [EncodingException]. All implementations provided by this library have been updated
         * to meet the API requirement; only [EncoderDecoder] implementations external to this
         * library that have not updated yet may fail when using them with [encodeBuffered]
         * and [encodeBufferedAsync] APIs.
         *
         * @param [encoder] The [Encoder] to use.
         * @param [throwOnOverflow] If `true` and [EncoderDecoder.Config.encodeOutMaxSize]
         *   throws an [EncodingSizeException], it will be re-thrown. If `false`, the exception
         *   is ignored and stream encoding to the buffer will continue.
         * @param [maxBufSize] The maximum size buffer this function will allocate. Must
         *   be greater than [EncoderDecoder.Config.maxEncodeEmitWithLineBreak].
         * @param [action] The suspend function to flush the buffer to; a destination to
         *   "write" encoded data to whereby `len` is the number of characters within `buf`,
         *   starting at index `offset`, to "write".
         *
         * @return The number of encoded characters.
         *
         * @see [encodeToString]
         * @see [encodeToCharArray]
         * @see [encodeBuffered]
         *
         * @throws [CancellationException]
         * @throws [EncodingException] If the [encoder] is configured to reject something,
         *   such as `UTF-8` byte to text transformations rejecting invalid byte sequences.
         * @throws [EncodingSizeException] If [EncoderDecoder.Config.encodeOutMaxSize]
         *   threw its exception (i.e. output would exceed [Int.MAX_VALUE]) and
         *   [throwOnOverflow] is `true`.
         * @throws [IllegalArgumentException] If [maxBufSize] is less than or equal to
         *   [EncoderDecoder.Config.maxEncodeEmitWithLineBreak].
         * */
        @JvmStatic
        public suspend fun ByteArray.encodeBufferedAsync(
            encoder: Encoder<*>,
            throwOnOverflow: Boolean,
            maxBufSize: Int,
            action: suspend (buf: CharArray, offset: Int, len: Int) -> Unit,
        ): Long = encoder.encodeBufferedUnsafe(
            data = this,
            offset = 0,
            len = size,
            buf = null,
            maxBufSize = maxBufSize,
            throwOnOverflow = throwOnOverflow,
            _action = { _buf, _offset, _len -> action(_buf, _offset, _len) },
        )

        /**
         * Encode [len] number of bytes from the array, starting at index [offset], using
         * a buffer of maximum size [maxBufSize].
         *
         * The encoding operation will allocate a single buffer, streaming encoded characters
         * to it and flushing to [action] when needed. If the pre-calculated size returned by
         * [EncoderDecoder.Config.encodeOutMaxSize] is less than or equal to the [maxBufSize],
         * then a buffer of that size will be allocated and [action] is only invoked once
         * (single-shot encoding). In the event that [EncoderDecoder.Config.encodeOutMaxSize]
         * throws its [EncodingSizeException] (i.e. encoding would exceed [Int.MAX_VALUE])
         * while [throwOnOverflow] is `false`, or its return value is greater than [maxBufSize],
         * then this function will always stream encode to a buffer while flushing to [action]
         * until the encoding operation has completed.
         *
         * **NOTE:** Documented exceptions thrown by this function do not include those
         * for which [action] may throw.
         *
         * **NOTE:** The [Encoder] implementation must be compatible with version `2.6.0+`
         * APIs and define a [EncoderDecoder.Config.maxEncodeEmit]. If the value is `-1` (i.e.
         * it has not updated to the new API yet), then this function will fail with an
         * [EncodingException]. All implementations provided by this library have been updated
         * to meet the API requirement; only [EncoderDecoder] implementations external to this
         * library that have not updated yet may fail when using them with [encodeBuffered]
         * and [encodeBufferedAsync] APIs.
         *
         * @param [encoder] The [Encoder] to use.
         * @param [throwOnOverflow] If `true` and [EncoderDecoder.Config.encodeOutMaxSize]
         *   throws an [EncodingSizeException], it will be re-thrown. If `false`, the exception
         *   is ignored and stream encoding to the buffer will continue.
         * @param [offset] The index in the array to start at.
         * @param [len] The number of bytes, starting at index [offset].
         * @param [maxBufSize] The maximum size buffer this function will allocate. Must
         *   be greater than [EncoderDecoder.Config.maxEncodeEmitWithLineBreak].
         * @param [action] The suspend function to flush the buffer to; a destination to
         *   "write" encoded data to whereby `len` is the number of characters within `buf`,
         *   starting at index `offset`, to "write".
         *
         * @return The number of encoded characters.
         *
         * @see [encodeToString]
         * @see [encodeToCharArray]
         * @see [encodeBuffered]
         *
         * @throws [CancellationException]
         * @throws [EncodingException] If the [encoder] is configured to reject something,
         *   such as `UTF-8` byte to text transformations rejecting invalid byte sequences.
         * @throws [EncodingSizeException] If [EncoderDecoder.Config.encodeOutMaxSize]
         *   threw its exception (i.e. output would exceed [Int.MAX_VALUE]) and
         *   [throwOnOverflow] is `true`.
         * @throws [IllegalArgumentException] If [maxBufSize] is less than or equal to
         *   [EncoderDecoder.Config.maxEncodeEmitWithLineBreak].
         * @throws [IndexOutOfBoundsException] If [offset] or [len] are inappropriate.
         * */
        @JvmStatic
        public suspend fun ByteArray.encodeBufferedAsync(
            encoder: Encoder<*>,
            throwOnOverflow: Boolean,
            offset: Int,
            len: Int,
            maxBufSize: Int,
            action: suspend (buf: CharArray, offset: Int, len: Int) -> Unit,
        ): Long {
            checkBounds(offset, len)
            return encoder.encodeBufferedUnsafe(
                data = this,
                offset = offset,
                len = len,
                buf = null,
                maxBufSize = maxBufSize,
                throwOnOverflow = throwOnOverflow,
                _action = { _buf, _offset, _len -> action(_buf, _offset, _len) },
            )
        }

        /**
         * Encode a [ByteArray] using the provided pre-allocated, reusable, [buf] array.
         *
         * The encoding operation will stream encoded characters to the provided [buf],
         * flushing to [action] when needed. If the pre-calculated size returned by
         * [EncoderDecoder.Config.encodeOutMaxSize] is less than or equal to the [buf]
         * size, then [action] is only invoked once (single-shot encoding). In the event
         * that [EncoderDecoder.Config.encodeOutMaxSize] throws its [EncodingSizeException]
         * (i.e. encoding would exceed [Int.MAX_VALUE]) while [throwOnOverflow] is `false`,
         * or its return value is greater than [buf] size, then this function will always
         * stream encode to a buffer while flushing to [action] until the encoding operation
         * has completed.
         *
         * **NOTE:** Documented exceptions thrown by this function do not include those
         * for which [action] may throw.
         *
         * **NOTE:** If [EncoderDecoder.Config.backFillBuffers] is `true`, provided [buf]
         * array will be back-filled with null character `\u0000` upon encoding completion.
         *
         * **NOTE:** The [Encoder] implementation must be compatible with version `2.6.0+`
         * APIs and define a [EncoderDecoder.Config.maxEncodeEmit]. If the value is `-1` (i.e.
         * it has not updated to the new API yet), then this function will fail with an
         * [EncodingException]. All implementations provided by this library have been updated
         * to meet the API requirement; only [EncoderDecoder] implementations external to this
         * library that have not updated yet may fail when using them with [encodeBuffered]
         * and [encodeBufferedAsync] APIs.
         *
         * @param [encoder] The [Encoder] to use.
         * @param [throwOnOverflow] If `true` and [EncoderDecoder.Config.encodeOutMaxSize]
         *   throws an [EncodingSizeException], it will be re-thrown. If `false`, the exception
         *   is ignored and stream encoding to the buffer will continue.
         * @param [buf] The pre-allocated array to use as the buffer. Its size must
         *   be greater than [EncoderDecoder.Config.maxEncodeEmitWithLineBreak].
         * @param [action] The suspend function to flush the buffer to; a destination to
         *   "write" encoded data to whereby `len` is the number of characters within `buf`,
         *   starting at index `offset`, to "write".
         *
         * @return The number of encoded characters.
         *
         * @see [encodeToString]
         * @see [encodeToCharArray]
         * @see [encodeBuffered]
         *
         * @throws [CancellationException]
         * @throws [EncodingException] If the [encoder] is configured to reject something,
         *   such as `UTF-8` byte to text transformations rejecting invalid byte sequences.
         * @throws [EncodingSizeException] If [EncoderDecoder.Config.encodeOutMaxSize]
         *   threw its exception (i.e. output would exceed [Int.MAX_VALUE]) and
         *   [throwOnOverflow] is `true`.
         * @throws [IllegalArgumentException] If [buf] size is less than or equal to
         *   [EncoderDecoder.Config.maxEncodeEmitWithLineBreak].
         * */
        @JvmStatic
        public suspend fun ByteArray.encodeBufferedAsync(
            encoder: Encoder<*>,
            throwOnOverflow: Boolean,
            buf: CharArray,
            action: suspend (buf: CharArray, offset: Int, len: Int) -> Unit,
        ): Long = encoder.encodeBufferedUnsafe(
            data = this,
            offset = 0,
            len = size,
            buf = buf,
            maxBufSize = buf.size,
            throwOnOverflow = throwOnOverflow,
            _action = { _buf, _offset, _len -> action(_buf, _offset, _len) },
        )

        /**
         * Encode [len] number of bytes from the array, starting at index [offset], using
         * the provided pre-allocated, reusable, [buf] array.
         *
         * The encoding operation will stream encoded characters to the provided [buf],
         * flushing to [action] when needed. If the pre-calculated size returned by
         * [EncoderDecoder.Config.encodeOutMaxSize] is less than or equal to the [buf]
         * size, then [action] is only invoked once (single-shot encoding). In the event
         * that [EncoderDecoder.Config.encodeOutMaxSize] throws its [EncodingSizeException]
         * (i.e. encoding would exceed [Int.MAX_VALUE]) while [throwOnOverflow] is `false`,
         * or its return value is greater than [buf] size, then this function will always
         * stream encode to a buffer while flushing to [action] until the encoding
         * operation has completed.
         *
         * **NOTE:** Documented exceptions thrown by this function do not include those
         * for which [action] may throw.
         *
         * **NOTE:** If [EncoderDecoder.Config.backFillBuffers] is `true`, provided [buf]
         * array will be back-filled with null character `\u0000` upon encoding completion.
         *
         * **NOTE:** The [Encoder] implementation must be compatible with version `2.6.0+`
         * APIs and define a [EncoderDecoder.Config.maxEncodeEmit]. If the value is `-1` (i.e.
         * it has not updated to the new API yet), then this function will fail with an
         * [EncodingException]. All implementations provided by this library have been updated
         * to meet the API requirement; only [EncoderDecoder] implementations external to this
         * library that have not updated yet may fail when using them with [encodeBuffered]
         * and [encodeBufferedAsync] APIs.
         *
         * @param [encoder] The [Encoder] to use.
         * @param [throwOnOverflow] If `true` and [EncoderDecoder.Config.encodeOutMaxSize]
         *   throws an [EncodingSizeException], it will be re-thrown. If `false`, the exception
         *   is ignored and stream encoding to the buffer will continue.
         * @param [offset] The index in the array to start at.
         * @param [len] The number of bytes, starting at index [offset].
         * @param [buf] The pre-allocated array to use as the buffer. Its size must
         *   be greater than [EncoderDecoder.Config.maxEncodeEmitWithLineBreak].
         * @param [action] The suspend function to flush the buffer to; a destination to
         *   "write" encoded data to whereby `len` is the number of characters within `buf`,
         *   starting at index `offset`, to "write".
         *
         * @return The number of encoded characters.
         *
         * @see [encodeToString]
         * @see [encodeToCharArray]
         * @see [encodeBuffered]
         *
         * @throws [CancellationException]
         * @throws [EncodingException] If the [encoder] is configured to reject something,
         *   such as `UTF-8` byte to text transformations rejecting invalid byte sequences.
         * @throws [EncodingSizeException] If [EncoderDecoder.Config.encodeOutMaxSize]
         *   threw its exception (i.e. output would exceed [Int.MAX_VALUE]) and
         *   [throwOnOverflow] is `true`.
         * @throws [IllegalArgumentException] If [buf] size is less than or equal to
         *   [EncoderDecoder.Config.maxEncodeEmitWithLineBreak].
         * @throws [IndexOutOfBoundsException] If [offset] or [len] are inappropriate.
         * */
        @JvmStatic
        public suspend fun ByteArray.encodeBufferedAsync(
            encoder: Encoder<*>,
            throwOnOverflow: Boolean,
            offset: Int,
            len: Int,
            buf: CharArray,
            action: suspend (buf: CharArray, offset: Int, len: Int) -> Unit,
        ): Long {
            checkBounds(offset, len)
            return encoder.encodeBufferedUnsafe(
                data = this,
                offset = offset,
                len = len,
                buf = buf,
                maxBufSize = buf.size,
                throwOnOverflow = throwOnOverflow,
                _action = { _buf, _offset, _len -> action(_buf, _offset, _len) },
            )
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
            val maxSize = encoder.config.encodeOutMaxSize(size)
            var i = 0
            val a = ByteArray(maxSize)
            encoder.encodeUnsafe(this, 0, size, _outFeed = { OutFeed { char -> a[i++] = char.code.toByte() } })
            if (i == maxSize) return a
            val copy = a.copyOf(i)
            if (encoder.config.backFillBuffers) {
                a.fill(0, 0, i)
            }
            return copy
        }
    }
}

// Does not check offset/len, thus the *Unsafe suffix
private inline fun ByteArray.encodeToStringUnsafe(encoder: Encoder<*>, offset: Int, len: Int): String {
    val maxSize = encoder.config.encodeOutMaxSize(len)
    val sb = StringBuilder(maxSize)
    encoder.encodeUnsafe(this, offset, len, _outFeed = { OutFeed(sb::append) })
    val result = sb.toString()
    if (encoder.config.backFillBuffers) {
        sb.wipe()
    }
    return result
}

// Does not check offset/len, thus the *Unsafe suffix
private inline fun ByteArray.encodeToCharArrayUnsafe(encoder: Encoder<*>, offset: Int, len: Int): CharArray {
    val maxSize = encoder.config.encodeOutMaxSize(len)
    var i = 0
    val a = CharArray(maxSize)
    encoder.encodeUnsafe(this, offset, len, _outFeed = { OutFeed { c -> a[i++] = c } })
    if (i == maxSize) return a
    val copy = a.copyOf(i)
    if (encoder.config.backFillBuffers) {
        a.fill('\u0000', 0, i)
    }
    return copy
}
