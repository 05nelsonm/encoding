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
@file:Suppress("LocalVariableName", "NOTHING_TO_INLINE", "PropertyName", "RemoveRedundantQualifierName")

package io.matthewnelson.encoding.core

import io.matthewnelson.encoding.core.EncoderDecoder.Companion.DEFAULT_BUFFER_SIZE
import io.matthewnelson.encoding.core.internal.closedException
import io.matthewnelson.encoding.core.internal.decode
import io.matthewnelson.encoding.core.internal.decodeBuffered
import io.matthewnelson.encoding.core.internal.isSpaceOrNewLine
import io.matthewnelson.encoding.core.util.DecoderInput
import kotlin.coroutines.cancellation.CancellationException
import kotlin.jvm.JvmField
import kotlin.jvm.JvmName
import kotlin.jvm.JvmStatic
import kotlin.jvm.JvmSynthetic

/**
 * Decode things.
 *
 * @see [EncoderDecoder]
 * @see [decodeToByteArray]
 * @see [decodeToByteArrayOrNull]
 * @see [decodeBuffered]
 * @see [decodeBufferedAsync]
 * @see [Decoder.Feed]
 * */
public sealed class Decoder<C: EncoderDecoder.Config>(public val config: C) {

    /**
     * Creates a new [Decoder.Feed], outputting decoded data to the supplied [Decoder.OutFeed].
     *
     * e.g.
     *
     *     val sb = StringBuilder()
     *
     *     // Alternatively use newDecoderFeed(sb::append)
     *     myDecoder.newDecoderFeed { decodedByte ->
     *         sb.append(decodedByte)
     *     }.use { feed ->
     *         "MYencoDEdTEXt".forEach(feed::consume)
     *     }
     *     println(sb.toString())
     *
     * @see [Decoder.Feed]
     * */
    public fun newDecoderFeed(out: Decoder.OutFeed): Decoder<C>.Feed {
        // Reserved for future Decoder.OutFeed interception
        return newDecoderFeedProtected(out)
    }

    protected abstract fun newDecoderFeedProtected(out: Decoder.OutFeed): Decoder<C>.Feed

    /**
     * Encoded data is fed into [consume] and, as the [Decoder.Feed]'s
     * buffer fills, decoded data is output to the supplied
     * [Decoder.OutFeed]. This allows for a "lazy" decode, or streaming
     * of decoded data.
     *
     * Once all data has been fed through [consume], call [doFinal] to
     * process remaining data in the [Decoder.Feed] buffer. Alternatively,
     * utilize the [use] extension function (highly recommended)
     * which will call [doFinal] (or [close] if there was an error with
     * decoding) for you.
     *
     * @see [use]
     * @see [newDecoderFeed]
     * @see [EncoderDecoder.Feed]
     * @see [EncoderDecoder.Feed.doFinal]
     * */
    public abstract inner class Feed: EncoderDecoder.Feed<C> {

        private var _isClosed = false
        private var _isPaddingSet = false

        /**
         * For implementations to pass in as a constructor argument and reference
         * while performing decoding operations. Upon [close] being called, this
         * is set to the [Decoder.OutFeed.NoOp] instance which ensures any local
         * object references that the initial [OutFeed] has are not leaked and can
         * be promptly GCd.
         * */
        @get:JvmName("_out")
        protected var _out: Decoder.OutFeed
            private set

        // pass Decoder.OutFeed.NoOp if not wanting to utilize _out at all.
        public constructor(_out: Decoder.OutFeed): super(this@Decoder.config) { this._out = _out }

        public final override fun isClosed(): Boolean = _isClosed

        /**
         * Updates the [Decoder.Feed] with a new character to decode.
         *
         * @throws [EncodingException] If [isClosed] is `true`, or if there was an
         *   error decoding.
         * */
        @Throws(EncodingException::class)
        public fun consume(input: Char) {
            if (_isClosed) throw closedException()

            try {
                if (config.isLenient != null && input.isSpaceOrNewLine()) {
                    if (config.isLenient) {
                        return
                    } else {
                        throw MalformedEncodingException("Spaces and new lines are forbidden when isLenient[false]")
                    }
                }

                // if paddingChar is null, it will never equal
                // input, thus never set isPaddingSet
                if (config.paddingChar == input) {
                    _isPaddingSet = true
                    return
                }

                if (_isPaddingSet) {
                    // Trying to decode something else that is not
                    // a space, new line, or padding. Fail.
                    throw MalformedEncodingException(
                        "Padding[${config.paddingChar}] was previously passed, " +
                        "but decoding operations are still being attempted."
                    )
                }

                consumeProtected(input)
            } catch (t: Throwable) {
                close()
                throw t
            }
        }

        /**
         * Flushes the buffered input and performs final decoding operations without
         * closing the [Feed].
         *
         * @see [EncoderDecoder.Feed.flush]
         *
         * @throws [EncodingException] If [isClosed] is `true`, or if there was an
         *   error decoding.
         * */
        @Throws(EncodingException::class)
        public final override fun flush() {
            if (_isClosed) throw closedException()

            try {
                doFinalProtected()
                _isPaddingSet = false
            } catch (t: Throwable) {
                close()
                throw t
            }
        }

        public final override fun close() {
            _isClosed = true
            _out = OutFeed.NoOp
        }

        @Throws(EncodingException::class)
        protected abstract fun consumeProtected(input: Char)

        @JvmSynthetic
        internal fun markAsClosed() { _isClosed = true }

        /** @suppress */
        public final override fun toString(): String = "${this@Decoder}.Decoder.Feed@${hashCode()}"

        /**
         * DEPRECATED since `2.6.0`
         * @suppress
         * */
        @Deprecated(
            message = "Parameter _out: Decoder.OutFeed was added. Use the new constructor.",
            level = DeprecationLevel.WARNING,
        )
        public constructor(): this(_out = Decoder.OutFeed.NoOp)
    }

    /**
     * A callback for returning decoded bytes as they
     * are produced by [Decoder.Feed].
     *
     * @see [newDecoderFeed]
     * @see [NoOp]
     * */
    public fun interface OutFeed {
        public fun output(decoded: Byte)

        public companion object {

            /**
             * A static, non-operational instance of [OutFeed]
             * */
            @JvmField
            public val NoOp: OutFeed = OutFeed {}
        }
    }

    public companion object {

        /**
         * Decode a [CharSequence].
         *
         * @param [decoder] The [Decoder] to use.
         *
         * @return The array of decoded data.
         *
         * @see [CharSequence.decodeToByteArrayOrNull]
         * @see [CharSequence.decodeBuffered]
         * @see [CharSequence.decodeBufferedAsync]
         *
         * @throws [EncodingException] If decoding failed, such as the [decoder] rejecting
         *   an invalid character or sequence.
         * @throws [EncodingSizeException] If the decoded output would exceed [Int.MAX_VALUE].
         * */
        @JvmStatic
        @Throws(EncodingException::class)
        public fun CharSequence.decodeToByteArray(decoder: Decoder<*>): ByteArray {
            return decoder.decode(DecoderInput(this), ::get)
        }

        /**
         * Decode a [CharSequence].
         *
         * @param [decoder] The [Decoder] to use.
         *
         * @return The array of decoded data, or `null` if there was a decoding error.
         *
         * @see [CharSequence.decodeToByteArray]
         * @see [CharSequence.decodeBuffered]
         * @see [CharSequence.decodeBufferedAsync]
         * */
        @JvmStatic
        public fun CharSequence.decodeToByteArrayOrNull(decoder: Decoder<*>): ByteArray? {
            return try {
                decodeToByteArray(decoder)
            } catch (_: EncodingException) {
                null
            }
        }

        /**
         * Decode a [CharArray].
         *
         * @param [decoder] The [Decoder] to use.
         *
         * @return The array of decoded data.
         *
         * @see [CharArray.decodeToByteArrayOrNull]
         * @see [CharArray.decodeBuffered]
         * @see [CharArray.decodeBufferedAsync]
         *
         * @throws [EncodingException] If decoding failed, such as the [decoder] rejecting
         *   an invalid character or sequence.
         * @throws [EncodingSizeException] If the decoded output would exceed [Int.MAX_VALUE].
         * */
        @JvmStatic
        @Throws(EncodingException::class)
        public fun CharArray.decodeToByteArray(decoder: Decoder<*>): ByteArray {
            return decoder.decode(DecoderInput(this), ::get)
        }

        /**
         * Decode a [CharArray].
         *
         * @param [decoder] The [Decoder] to use.
         *
         * @return The array of decoded data, or `null` if there was a decoding error.
         *
         * @see [CharArray.decodeToByteArray]
         * @see [CharArray.decodeBuffered]
         * @see [CharArray.decodeBufferedAsync]
         * */
        @JvmStatic
        public fun CharArray.decodeToByteArrayOrNull(decoder: Decoder<*>): ByteArray? {
            return try {
                decodeToByteArray(decoder)
            } catch (_: EncodingException) {
                null
            }
        }

        /**
         * Decode a [CharSequence] using a buffer of maximum size [DEFAULT_BUFFER_SIZE].
         *
         * The decoding operation will allocate a single buffer, streaming decoded bytes
         * to it and flushing to [action] when needed. If the pre-calculated size
         * returned by [EncoderDecoder.Config.decodeOutMaxSizeOrFail] is less than or
         * equal to the [DEFAULT_BUFFER_SIZE], then a buffer of that size will be allocated
         * and [action] is only invoked once (single-shot decoding). In the event that
         * [EncoderDecoder.Config.decodeOutMaxSizeOrFail] throws its [EncodingSizeException]
         * due to an overflow (i.e. decoding would exceed [Int.MAX_VALUE]) while [throwOnOverflow]
         * is `false`, or its return value is greater than [DEFAULT_BUFFER_SIZE], then this
         * function will always stream decode to a buffer while flushing to [action] until
         * the decoding operation has completed.
         *
         * **NOTE:** Documented exceptions thrown by this function do not include those
         * for which [action] may throw.
         *
         * e.g. (Using `io.matthewnelson.kmp-file:file` & module `:utf8`)
         *
         *     "/path/to/file.txt".toFile().openWrite(excl = null).use { stream ->
         *         val n = "Some long string"
         *             .decodeBuffered(UTF8, false, stream::write)
         *         println("Wrote $n UTF-8 bytes to file.txt")
         *     }
         *
         * e.g. (Using `org.kotlincrypto.hash:sha2` & module `:base64`)
         *
         *     val d = SHA256()
         *     "SGVsbG8gV29ybGQh"
         *         .decodeBuffered(Base64.Default, false, d::update)
         *     // ...
         *
         * **NOTE:** The [Decoder] implementation must be compatible with version `2.6.0+`
         * APIs and define a [EncoderDecoder.Config.maxDecodeEmit]. If the value is `-1` (i.e.
         * it has not updated to the new API yet), then this function will fail with an
         * [EncodingException]. All implementations provided by this library have been updated
         * to meet the API requirement; only [EncoderDecoder] implementations external to this
         * library that have not updated yet may fail when using them with [decodeBuffered]
         * and [decodeBufferedAsync] APIs.
         *
         * @param [decoder] The [Decoder] to use.
         * @param [throwOnOverflow] If `true` and [EncoderDecoder.Config.decodeOutMaxSizeOrFail]
         *   throws an [EncodingSizeException], it will be re-thrown. If `false`, the exception
         *   will be ignored and stream decoding to the buffer will continue.
         * @param [action] The function to flush the buffer to; a destination to "write"
         *   decoded data to whereby `len` is the number of bytes within `buf`, starting
         *   at index `offset`, to "write".
         *
         * @return The number of decoded bytes.
         *
         * @see [CharSequence.decodeToByteArray]
         * @see [CharSequence.decodeToByteArrayOrNull]
         * @see [CharSequence.decodeBufferedAsync]
         *
         * @throws [EncodingException] If decoding failed, such as the [decoder] rejecting
         *   an invalid character or sequence.
         * @throws [EncodingSizeException] If [EncoderDecoder.Config.decodeOutMaxSizeOrFail]
         *   threw its exception and [throwOnOverflow] is `true`.
         * */
        @JvmStatic
        @Throws(EncodingException::class)
        public inline fun CharSequence.decodeBuffered(
            decoder: Decoder<*>,
            throwOnOverflow: Boolean,
            noinline action: (buf: ByteArray, offset: Int, len: Int) -> Unit,
        ): Long = decodeBuffered(decoder, throwOnOverflow, DEFAULT_BUFFER_SIZE, action)

        /**
         * Decode a [CharSequence] using a buffer of maximum size [maxBufSize].
         *
         * The decoding operation will allocate a single buffer, streaming decoded bytes
         * to it and flushing to [action] when needed. If the pre-calculated size
         * returned by [EncoderDecoder.Config.decodeOutMaxSizeOrFail] is less than or
         * equal to the [maxBufSize], then a buffer of that size will be allocated
         * and [action] is only invoked once (single-shot decoding). In the event that
         * [EncoderDecoder.Config.decodeOutMaxSizeOrFail] throws its [EncodingSizeException]
         * due to an overflow (i.e. decoding would exceed [Int.MAX_VALUE]) while [throwOnOverflow]
         * is `false`, or its return value is greater than [maxBufSize], then this
         * function will always stream decode to a buffer while flushing to [action] until
         * the decoding operation has completed.
         *
         * **NOTE:** Documented exceptions thrown by this function do not include those
         * for which [action] may throw.
         *
         * e.g. (Using `io.matthewnelson.kmp-file:file` & module `:utf8`)
         *
         *     "/path/to/file.txt".toFile().openWrite(excl = null).use { stream ->
         *         val n = "Some string"
         *             .decodeBuffered(UTF8, false, 1024, stream::write)
         *         println("Wrote $n UTF-8 bytes to file.txt")
         *     }
         *
         * e.g. (Using `org.kotlincrypto.hash:sha2` & module `:base64`)
         *
         *     val d = SHA256()
         *     "SGVsbG8gV29ybGQh"
         *         .decodeBuffered(Base64.Default, false, 1024, d::update)
         *     // ...
         *
         * **NOTE:** The [Decoder] implementation must be compatible with version `2.6.0+`
         * APIs and define a [EncoderDecoder.Config.maxDecodeEmit]. If the value is `-1` (i.e.
         * it has not updated to the new API yet), then this function will fail with an
         * [EncodingException]. All implementations provided by this library have been updated
         * to meet the API requirement; only [EncoderDecoder] implementations external to this
         * library that have not updated yet may fail when using them with [decodeBuffered]
         * and [decodeBufferedAsync] APIs.
         *
         * @param [decoder] The [Decoder] to use.
         * @param [throwOnOverflow] If `true` and [EncoderDecoder.Config.decodeOutMaxSizeOrFail]
         *   throws an [EncodingSizeException], it will be re-thrown. If `false`, the exception
         *   will be ignored and stream decoding to the buffer will continue.
         * @param [maxBufSize] The maximum size buffer this function will allocate. Must
         *   be greater than [EncoderDecoder.Config.maxDecodeEmit].
         * @param [action] The function to flush the buffer to; a destination to "write"
         *   decoded data to whereby `len` is the number of bytes within `buf`, starting
         *   at index `offset`, to "write".
         *
         * @return The number of decoded bytes.
         *
         * @see [CharSequence.decodeToByteArray]
         * @see [CharSequence.decodeToByteArrayOrNull]
         * @see [CharSequence.decodeBufferedAsync]
         *
         * @throws [EncodingException] If decoding failed, such as the [decoder] rejecting
         *   an invalid character or sequence.
         * @throws [EncodingSizeException] If [EncoderDecoder.Config.decodeOutMaxSizeOrFail]
         *   threw its exception and [throwOnOverflow] is `true`.
         * @throws [IllegalArgumentException] If [maxBufSize] is less than or equal to
         *   [EncoderDecoder.Config.maxDecodeEmit].
         * */
        @JvmStatic
        @Throws(EncodingException::class)
        public fun CharSequence.decodeBuffered(
            decoder: Decoder<*>,
            throwOnOverflow: Boolean,
            maxBufSize: Int,
            action: (buf: ByteArray, offset: Int, len: Int) -> Unit,
        ): Long = decoder.decodeBuffered(
            buf = null,
            maxBufSize = maxBufSize,
            throwOnOverflow = throwOnOverflow,
            _get = ::get,
            _input = { DecoderInput(this) },
            _action = action,
        )

        /**
         * Decode a [CharSequence] using the provided pre-allocated, reusable, [buf] array.
         *
         * The decoding operation will stream decoded bytes to the provided [buf], flushing
         * to [action] when needed. If the pre-calculated size returned by
         * [EncoderDecoder.Config.decodeOutMaxSizeOrFail] is less than or equal to the [buf]
         * size, then [action] is only invoked once (single-shot decoding). In the event that
         * [EncoderDecoder.Config.decodeOutMaxSizeOrFail] throws its [EncodingSizeException]
         * due to an overflow (i.e. decoding would exceed [Int.MAX_VALUE]) while [throwOnOverflow]
         * is `false`, or its return value is greater than [buf] size, then this
         * function will always stream decode to a buffer while flushing to [action] until
         * the decoding operation has completed.
         *
         * **NOTE:** Documented exceptions thrown by this function do not include those
         * for which [action] may throw.
         *
         * **NOTE:** If [EncoderDecoder.Config.backFillBuffers] is `true`, provided [buf]
         * array will be back-filled with `0` bytes upon decoding completion.
         *
         * e.g. (Using `io.matthewnelson.kmp-file:file` & module `:utf8`)
         *
         *     "/path/to/file.txt".toFile().openWrite(excl = null).use { stream ->
         *         val buf = ByteArray(DEFAULT_BUFFER_SIZE)
         *         var n = "Some string"
         *             .decodeBuffered(UTF8, false, buf, stream::write)
         *         n += "Some other string"
         *             .decodeBuffered(UTF8, false, buf, stream::write)
         *         println("Wrote $n UTF-8 bytes to file.txt")
         *     }
         *
         * e.g. (Using `org.kotlincrypto.hash:sha2` & module `:base64`)
         *
         *     val d = SHA256()
         *     val buf = ByteArray(DEFAULT_BUFFER_SIZE)
         *     "SGVsbG8gV29ybGQh"
         *         .decodeBuffered(Base64.Default, false, buf, d::update)
         *     "SGVsbG8gV29ybGQh"
         *         .decodeBuffered(Base64.Default, false, buf, d::update)
         *     // ...
         *
         * **NOTE:** The [Decoder] implementation must be compatible with version `2.6.0+`
         * APIs and define a [EncoderDecoder.Config.maxDecodeEmit]. If the value is `-1` (i.e.
         * it has not updated to the new API yet), then this function will fail with an
         * [EncodingException]. All implementations provided by this library have been updated
         * to meet the API requirement; only [EncoderDecoder] implementations external to this
         * library that have not updated yet may fail when using them with [decodeBuffered]
         * and [decodeBufferedAsync] APIs.
         *
         * @param [decoder] The [Decoder] to use.
         * @param [throwOnOverflow] If `true` and [EncoderDecoder.Config.decodeOutMaxSizeOrFail]
         *   throws an [EncodingSizeException], it will be re-thrown. If `false`, the exception
         *   will be ignored and stream decoding to the buffer will continue.
         * @param [buf] The pre-allocated array to use as the buffer. Its size must be
         *   greater than [EncoderDecoder.Config.maxDecodeEmit].
         * @param [action] The function to flush the buffer to; a destination to "write"
         *   decoded data to whereby `len` is the number of bytes within `buf`, starting
         *   at index `offset`, to "write".
         *
         * @return The number of decoded bytes.
         *
         * @see [CharSequence.decodeToByteArray]
         * @see [CharSequence.decodeToByteArrayOrNull]
         * @see [CharSequence.decodeBufferedAsync]
         *
         * @throws [EncodingException] If decoding failed, such as the [decoder] rejecting
         *   an invalid character or sequence.
         * @throws [EncodingSizeException] If [EncoderDecoder.Config.decodeOutMaxSizeOrFail]
         *   threw its exception and [throwOnOverflow] is `true`.
         * @throws [IllegalArgumentException] If [buf] size is less than or equal to
         *   [EncoderDecoder.Config.maxDecodeEmit].
         * */
        @JvmStatic
        @Throws(EncodingException::class)
        public fun CharSequence.decodeBuffered(
            decoder: Decoder<*>,
            throwOnOverflow: Boolean,
            buf: ByteArray,
            action: (buf: ByteArray, offset: Int, len: Int) -> Unit,
        ): Long = decoder.decodeBuffered(
            buf = buf,
            maxBufSize = buf.size,
            throwOnOverflow = throwOnOverflow,
            _get = ::get,
            _input = { DecoderInput(this) },
            _action = action,
        )

        /**
         * Decode a [CharSequence] using a buffer of maximum of [DEFAULT_BUFFER_SIZE].
         *
         * The decoding operation will allocate a single buffer, streaming decoded bytes
         * to it and flushing to [action] when needed. If the pre-calculated size
         * returned by [EncoderDecoder.Config.decodeOutMaxSizeOrFail] is less than or
         * equal to the [DEFAULT_BUFFER_SIZE], then a buffer of that size will be allocated
         * and [action] is only invoked once (single-shot decoding). In the event that
         * [EncoderDecoder.Config.decodeOutMaxSizeOrFail] throws its [EncodingSizeException]
         * due to an overflow (i.e. decoding would exceed [Int.MAX_VALUE]) while [throwOnOverflow]
         * is `false`, or its return value is greater than [DEFAULT_BUFFER_SIZE], then this
         * function will always stream decode to a buffer while flushing to [action] until
         * the decoding operation has completed.
         *
         * **NOTE:** Documented exceptions thrown by this function do not include those
         * for which [action] may throw.
         *
         * e.g. (Using `io.matthewnelson.kmp-file:async` & module `:utf8`)
         *
         *     AsyncFs.Default.with {
         *         "/path/to/file.txt".toFile()
         *             .openWriteAsync(excl = null)
         *             .useAsync { stream ->
         *                 val text = "Some long string"
         *                 val n = text.decodeBufferedAsync(
         *                     UTF8,
         *                     false,
         *                     stream::writeAsync,
         *                 )
         *                 println("Wrote $n UTF-8 bytes to file.txt")
         *             }
         *     }
         *
         * **NOTE:** The [Decoder] implementation must be compatible with version `2.6.0+`
         * APIs and define a [EncoderDecoder.Config.maxDecodeEmit]. If the value is `-1` (i.e.
         * it has not updated to the new API yet), then this function will fail with an
         * [EncodingException]. All implementations provided by this library have been updated
         * to meet the API requirement; only [EncoderDecoder] implementations external to this
         * library that have not updated yet may fail when using them with [decodeBuffered]
         * and [decodeBufferedAsync] APIs.
         *
         * @param [decoder] The [Decoder] to use.
         * @param [throwOnOverflow] If `true` and [EncoderDecoder.Config.decodeOutMaxSizeOrFail]
         *   throws an [EncodingSizeException], it will be re-thrown. If `false`, the exception
         *   will be ignored and stream decoding to the buffer will continue.
         * @param [action] The suspend function to flush the buffer to; a destination to
         *   "write" decoded data to whereby `len` is the number of bytes within `buf`,
         *   starting at index `offset`, to "write".
         *
         * @return The number of decoded bytes.
         *
         * @see [CharSequence.decodeToByteArray]
         * @see [CharSequence.decodeToByteArrayOrNull]
         * @see [CharSequence.decodeBuffered]
         *
         * @throws [CancellationException]
         * @throws [EncodingException] If decoding failed, such as the [decoder] rejecting
         *   an invalid character or sequence.
         * @throws [EncodingSizeException] If [EncoderDecoder.Config.decodeOutMaxSizeOrFail]
         *   threw its exception and [throwOnOverflow] is `true`.
         * */
        @JvmStatic
        @Throws(CancellationException::class, EncodingException::class)
        public suspend inline fun CharSequence.decodeBufferedAsync(
            decoder: Decoder<*>,
            throwOnOverflow: Boolean,
            noinline action: suspend (buf: ByteArray, offset: Int, len: Int) -> Unit,
        ): Long = decodeBufferedAsync(decoder, throwOnOverflow, DEFAULT_BUFFER_SIZE, action)

        /**
         * Decode a [CharSequence] using a buffer of maximum size [maxBufSize].
         *
         * The decoding operation will allocate a single buffer, streaming decoded bytes
         * to it and flushing to [action] when needed. If the pre-calculated size
         * returned by [EncoderDecoder.Config.decodeOutMaxSizeOrFail] is less than or
         * equal to the [maxBufSize], then a buffer of that size will be allocated
         * and [action] is only invoked once (single-shot decoding). In the event that
         * [EncoderDecoder.Config.decodeOutMaxSizeOrFail] throws its [EncodingSizeException]
         * due to an overflow (i.e. decoding would exceed [Int.MAX_VALUE]) while [throwOnOverflow]
         * is `false`, or its return value is greater than [maxBufSize], then this
         * function will always stream decode to a buffer while flushing to [action] until
         * the decoding operation has completed.
         *
         * **NOTE:** Documented exceptions thrown by this function do not include those
         * for which [action] may throw.
         *
         * e.g. (Using `io.matthewnelson.kmp-file:async` & module `:utf8`)
         *
         *     AsyncFs.Default.with {
         *         "/path/to/file.txt".toFile()
         *             .openWriteAsync(excl = null)
         *             .useAsync { stream ->
         *                 val text = "Some long string"
         *                 val n = text.decodeBufferedAsync(
         *                     UTF8,
         *                     false,
         *                     1024,
         *                     stream::writeAsync,
         *                 )
         *                 println("Wrote $n UTF-8 bytes to file.txt")
         *             }
         *     }
         *
         * **NOTE:** The [Decoder] implementation must be compatible with version `2.6.0+`
         * APIs and define a [EncoderDecoder.Config.maxDecodeEmit]. If the value is `-1` (i.e.
         * it has not updated to the new API yet), then this function will fail with an
         * [EncodingException]. All implementations provided by this library have been updated
         * to meet the API requirement; only [EncoderDecoder] implementations external to this
         * library that have not updated yet may fail when using them with [decodeBuffered]
         * and [decodeBufferedAsync] APIs.
         *
         * @param [decoder] The [Decoder] to use.
         * @param [throwOnOverflow] If `true` and [EncoderDecoder.Config.decodeOutMaxSizeOrFail]
         *   throws an [EncodingSizeException], it will be re-thrown. If `false`, the exception
         *   will be ignored and stream decoding to the buffer will continue.
         * @param [maxBufSize] The maximum size buffer this function will allocate. Must
         *   be greater than [EncoderDecoder.Config.maxDecodeEmit].
         * @param [action] The suspend function to flush the buffer to; a destination to
         *   "write" decoded data to whereby `len` is the number of bytes within `buf`,
         *   starting at index `offset`, to "write".
         *
         * @return The number of decoded bytes.
         *
         * @see [CharSequence.decodeToByteArray]
         * @see [CharSequence.decodeToByteArrayOrNull]
         * @see [CharSequence.decodeBuffered]
         *
         * @throws [CancellationException]
         * @throws [EncodingException] If decoding failed, such as the [decoder] rejecting
         *   an invalid character or sequence.
         * @throws [EncodingSizeException] If [EncoderDecoder.Config.decodeOutMaxSizeOrFail]
         *   threw its exception and [throwOnOverflow] is `true`.
         * @throws [IllegalArgumentException] If [maxBufSize] is less than or equal to
         *   [EncoderDecoder.Config.maxDecodeEmit].
         * */
        @JvmStatic
        @Throws(CancellationException::class, EncodingException::class)
        public suspend fun CharSequence.decodeBufferedAsync(
            decoder: Decoder<*>,
            throwOnOverflow: Boolean,
            maxBufSize: Int,
            action: suspend (buf: ByteArray, offset: Int, len: Int) -> Unit,
        ): Long = decoder.decodeBuffered(
            buf = null,
            maxBufSize = maxBufSize,
            throwOnOverflow = throwOnOverflow,
            _get = ::get,
            _input = { DecoderInput(this) },
            _action = { buf, offset, len -> action(buf, offset, len) },
        )

        /**
         * Decode a [CharSequence] using the provided pre-allocated, reusable, [buf] array.
         *
         * The decoding operation will stream decoded bytes to the provided [buf], flushing
         * to [action] when needed. If the pre-calculated size returned by
         * [EncoderDecoder.Config.decodeOutMaxSizeOrFail] is less than or equal to the [buf]
         * size, then [action] is only invoked once (single-shot decoding). In the event that
         * [EncoderDecoder.Config.decodeOutMaxSizeOrFail] throws its [EncodingSizeException]
         * due to an overflow (i.e. decoding would exceed [Int.MAX_VALUE]) while [throwOnOverflow]
         * is `false`, or its return value is greater than [buf] size, then this
         * function will always stream decode to a buffer while flushing to [action] until
         * the decoding operation has completed.
         *
         * **NOTE:** Documented exceptions thrown by this function do not include those
         * for which [action] may throw.
         *
         * **NOTE:** If [EncoderDecoder.Config.backFillBuffers] is `true`, provided [buf]
         * array will be back-filled with `0` bytes upon decoding completion.
         *
         * e.g. (Using `io.matthewnelson.kmp-file:async` & module `:utf8`)
         *
         *     AsyncFs.Default.with {
         *         "/path/to/file.txt".toFile()
         *             .openWriteAsync(excl = null)
         *             .useAsync { stream ->
         *                 val text = "Some long string"
         *                 val buf = ByteArray(DEFAULT_BUFFER_SIZE)
         *                 var n = text.decodeBufferedAsync(
         *                     UTF8,
         *                     false,
         *                     buf,
         *                     stream::writeAsync,
         *                 )
         *                 n += text.decodeBufferedAsync(
         *                     UTF8,
         *                     false,
         *                     buf,
         *                     stream::writeAsync,
         *                 )
         *                 println("Wrote $n UTF-8 bytes to file.txt")
         *             }
         *     }
         *
         * **NOTE:** The [Decoder] implementation must be compatible with version `2.6.0+`
         * APIs and define a [EncoderDecoder.Config.maxDecodeEmit]. If the value is `-1` (i.e.
         * it has not updated to the new API yet), then this function will fail with an
         * [EncodingException]. All implementations provided by this library have been updated
         * to meet the API requirement; only [EncoderDecoder] implementations external to this
         * library that have not updated yet may fail when using them with [decodeBuffered]
         * and [decodeBufferedAsync] APIs.
         *
         * @param [decoder] The [Decoder] to use.
         * @param [throwOnOverflow] If `true` and [EncoderDecoder.Config.decodeOutMaxSizeOrFail]
         *   throws an [EncodingSizeException], it will be re-thrown. If `false`, the exception
         *   will be ignored and stream decoding to the buffer will continue.
         * @param [buf] The pre-allocated array to use as the buffer. Its size must be
         *   greater than [EncoderDecoder.Config.maxDecodeEmit].
         * @param [action] The suspend function to flush the buffer to; a destination to
         *   "write" decoded data to whereby `len` is the number of bytes within `buf`,
         *   starting at index `offset`, to "write".
         *
         * @return The number of decoded bytes.
         *
         * @see [CharSequence.decodeToByteArray]
         * @see [CharSequence.decodeToByteArrayOrNull]
         * @see [CharSequence.decodeBuffered]
         *
         * @throws [CancellationException]
         * @throws [EncodingException] If decoding failed, such as the [decoder] rejecting
         *   an invalid character or sequence.
         * @throws [EncodingSizeException] If [EncoderDecoder.Config.decodeOutMaxSizeOrFail]
         *   threw its exception and [throwOnOverflow] is `true`.
         * @throws [IllegalArgumentException] If [buf] size is less than or equal to
         *   [EncoderDecoder.Config.maxDecodeEmit].
         * */
        @JvmStatic
        @Throws(CancellationException::class, EncodingException::class)
        public suspend fun CharSequence.decodeBufferedAsync(
            decoder: Decoder<*>,
            throwOnOverflow: Boolean,
            buf: ByteArray,
            action: suspend (buf: ByteArray, offset: Int, len: Int) -> Unit,
        ): Long = decoder.decodeBuffered(
            buf = buf,
            maxBufSize = buf.size,
            throwOnOverflow = throwOnOverflow,
            _get = ::get,
            _input = { DecoderInput(this) },
            _action = { _buf, offset, len -> action(_buf, offset, len) },
        )

        /**
         * Decode a [CharArray] using a buffer of maximum size [DEFAULT_BUFFER_SIZE].
         *
         * The decoding operation will allocate a single buffer, streaming decoded bytes
         * to it and flushing to [action] when needed. If the pre-calculated size
         * returned by [EncoderDecoder.Config.decodeOutMaxSizeOrFail] is less than or
         * equal to the [DEFAULT_BUFFER_SIZE], then a buffer of that size will be allocated
         * and [action] is only invoked once (single-shot decoding). In the event that
         * [EncoderDecoder.Config.decodeOutMaxSizeOrFail] throws its [EncodingSizeException]
         * due to an overflow (i.e. decoding would exceed [Int.MAX_VALUE]) while [throwOnOverflow]
         * is `false`, or its return value is greater than [DEFAULT_BUFFER_SIZE], then this
         * function will always stream decode to a buffer while flushing to [action] until
         * the decoding operation has completed.
         *
         * **NOTE:** Documented exceptions thrown by this function do not include those
         * for which [action] may throw.
         *
         * e.g. (Using `io.matthewnelson.kmp-file:file` & module `:utf8`)
         *
         *     "/path/to/file.txt".toFile().openWrite(excl = null).use { stream ->
         *         val n = "Some long string"
         *             .toCharArray()
         *             .decodeBuffered(UTF8, false, stream::write)
         *         println("Wrote $n UTF-8 bytes to file.txt")
         *     }
         *
         * e.g. (Using `org.kotlincrypto.hash:sha2` & module `:base64`)
         *
         *     val d = SHA256()
         *     "SGVsbG8gV29ybGQh"
         *         .toCharArray()
         *         .decodeBuffered(Base64.Default, false, d::update)
         *     // ...
         *
         * **NOTE:** The [Decoder] implementation must be compatible with version `2.6.0+`
         * APIs and define a [EncoderDecoder.Config.maxDecodeEmit]. If the value is `-1` (i.e.
         * it has not updated to the new API yet), then this function will fail with an
         * [EncodingException]. All implementations provided by this library have been updated
         * to meet the API requirement; only [EncoderDecoder] implementations external to this
         * library that have not updated yet may fail when using them with [decodeBuffered]
         * and [decodeBufferedAsync] APIs.
         *
         * @param [decoder] The [Decoder] to use.
         * @param [throwOnOverflow] If `true` and [EncoderDecoder.Config.decodeOutMaxSizeOrFail]
         *   throws an [EncodingSizeException], it will be re-thrown. If `false`, the exception
         *   will be ignored and stream decoding to the buffer will continue.
         * @param [action] The function to flush the buffer to; a destination to "write"
         *   decoded data to whereby `len` is the number of bytes within `buf`, starting
         *   at index `offset`, to "write".
         *
         * @return The number of decoded bytes.
         *
         * @see [CharArray.decodeToByteArray]
         * @see [CharArray.decodeToByteArrayOrNull]
         * @see [CharArray.decodeBufferedAsync]
         *
         * @throws [EncodingException] If decoding failed, such as the [decoder] rejecting
         *   an invalid character or sequence.
         * @throws [EncodingSizeException] If [EncoderDecoder.Config.decodeOutMaxSizeOrFail]
         *   threw its exception and [throwOnOverflow] is `true`.
         * */
        @JvmStatic
        @Throws(EncodingException::class)
        public inline fun CharArray.decodeBuffered(
            decoder: Decoder<*>,
            throwOnOverflow: Boolean,
            noinline action: (buf: ByteArray, offset: Int, len: Int) -> Unit,
        ): Long = decodeBuffered(decoder, throwOnOverflow, DEFAULT_BUFFER_SIZE, action)

        /**
         * Decode a [CharArray] using a buffer of maximum size [maxBufSize].
         *
         * The decoding operation will allocate a single buffer, streaming decoded bytes
         * to it and flushing to [action] when needed. If the pre-calculated size
         * returned by [EncoderDecoder.Config.decodeOutMaxSizeOrFail] is less than or
         * equal to the [maxBufSize], then a buffer of that size will be allocated
         * and [action] is only invoked once (single-shot decoding). In the event that
         * [EncoderDecoder.Config.decodeOutMaxSizeOrFail] throws its [EncodingSizeException]
         * due to an overflow (i.e. decoding would exceed [Int.MAX_VALUE]) while [throwOnOverflow]
         * is `false`, or its return value is greater than [maxBufSize], then this
         * function will always stream decode to a buffer while flushing to [action] until
         * the decoding operation has completed.
         *
         * **NOTE:** Documented exceptions thrown by this function do not include those
         * for which [action] may throw.
         *
         * e.g. (Using `io.matthewnelson.kmp-file:file` & module `:utf8`)
         *
         *     "/path/to/file.txt".toFile().openWrite(excl = null).use { stream ->
         *         val n = "Some string"
         *             .toCharArray()
         *             .decodeBuffered(UTF8, false, 1024, stream::write)
         *         println("Wrote $n UTF-8 bytes to file.txt")
         *     }
         *
         * e.g. (Using `org.kotlincrypto.hash:sha2` & module `:base64`)
         *
         *     val d = SHA256()
         *     "SGVsbG8gV29ybGQh"
         *         .toCharArray()
         *         .decodeBuffered(Base64.Default, false, 1024, d::update)
         *     // ...
         *
         * **NOTE:** The [Decoder] implementation must be compatible with version `2.6.0+`
         * APIs and define a [EncoderDecoder.Config.maxDecodeEmit]. If the value is `-1` (i.e.
         * it has not updated to the new API yet), then this function will fail with an
         * [EncodingException]. All implementations provided by this library have been updated
         * to meet the API requirement; only [EncoderDecoder] implementations external to this
         * library that have not updated yet may fail when using them with [decodeBuffered]
         * and [decodeBufferedAsync] APIs.
         *
         * @param [decoder] The [Decoder] to use.
         * @param [throwOnOverflow] If `true` and [EncoderDecoder.Config.decodeOutMaxSizeOrFail]
         *   throws an [EncodingSizeException], it will be re-thrown. If `false`, the exception
         *   will be ignored and stream decoding to the buffer will continue.
         * @param [maxBufSize] The maximum size buffer this function will allocate. Must
         *   be greater than [EncoderDecoder.Config.maxDecodeEmit].
         * @param [action] The function to flush the buffer to; a destination to "write"
         *   decoded data to whereby `len` is the number of bytes within `buf`, starting
         *   at index `offset`, to "write".
         *
         * @return The number of decoded bytes.
         *
         * @see [CharArray.decodeToByteArray]
         * @see [CharArray.decodeToByteArrayOrNull]
         * @see [CharArray.decodeBufferedAsync]
         *
         * @throws [EncodingException] If decoding failed, such as the [decoder] rejecting
         *   an invalid character or sequence.
         * @throws [EncodingSizeException] If [EncoderDecoder.Config.decodeOutMaxSizeOrFail]
         *   threw its exception and [throwOnOverflow] is `true`.
         * @throws [IllegalArgumentException] If [maxBufSize] is less than or equal to
         *   [EncoderDecoder.Config.maxDecodeEmit].
         * */
        @JvmStatic
        @Throws(EncodingException::class)
        public fun CharArray.decodeBuffered(
            decoder: Decoder<*>,
            throwOnOverflow: Boolean,
            maxBufSize: Int,
            action: (buf: ByteArray, offset: Int, len: Int) -> Unit,
        ): Long = decoder.decodeBuffered(
            buf = null,
            maxBufSize = maxBufSize,
            throwOnOverflow = throwOnOverflow,
            _get = ::get,
            _input = { DecoderInput(this) },
            _action = action,
        )

        /**
         * Decode a [CharArray] using the provided pre-allocated, reusable, [buf] array.
         *
         * The decoding operation will stream decoded bytes to the provided [buf], flushing
         * to [action] when needed. If the pre-calculated size returned by
         * [EncoderDecoder.Config.decodeOutMaxSizeOrFail] is less than or equal to the [buf]
         * size, then [action] is only invoked once (single-shot decoding). In the event that
         * [EncoderDecoder.Config.decodeOutMaxSizeOrFail] throws its [EncodingSizeException]
         * due to an overflow (i.e. decoding would exceed [Int.MAX_VALUE]) while [throwOnOverflow]
         * is `false`, or its return value is greater than [buf] size, then this
         * function will always stream decode to a buffer while flushing to [action] until
         * the decoding operation has completed.
         *
         * **NOTE:** Documented exceptions thrown by this function do not include those
         * for which [action] may throw.
         *
         * **NOTE:** If [EncoderDecoder.Config.backFillBuffers] is `true`, provided [buf]
         * array will be back-filled with `0` bytes upon decoding completion.
         *
         * e.g. (Using `io.matthewnelson.kmp-file:file` & module `:utf8`)
         *
         *     "/path/to/file.txt".toFile().openWrite(excl = null).use { stream ->
         *         val buf = ByteArray(DEFAULT_BUFFER_SIZE)
         *         var n = "Some string"
         *             .toCharArray()
         *             .decodeBuffered(UTF8, false, buf, stream::write)
         *         n += "Some other string"
         *             .toCharArray()
         *             .decodeBuffered(UTF8, false, buf, stream::write)
         *         println("Wrote $n UTF-8 bytes to file.txt")
         *     }
         *
         * e.g. (Using `org.kotlincrypto.hash:sha2` & module `:base64`)
         *
         *     val d = SHA256()
         *     val buf = ByteArray(DEFAULT_BUFFER_SIZE)
         *     "SGVsbG8gV29ybGQh"
         *         .toCharArray()
         *         .decodeBuffered(Base64.Default, false, buf, d::update)
         *     "SGVsbG8gV29ybGQh"
         *         .toCharArray()
         *         .decodeBuffered(Base64.Default, false, buf, d::update)
         *     // ...
         *
         * **NOTE:** The [Decoder] implementation must be compatible with version `2.6.0+`
         * APIs and define a [EncoderDecoder.Config.maxDecodeEmit]. If the value is `-1` (i.e.
         * it has not updated to the new API yet), then this function will fail with an
         * [EncodingException]. All implementations provided by this library have been updated
         * to meet the API requirement; only [EncoderDecoder] implementations external to this
         * library that have not updated yet may fail when using them with [decodeBuffered]
         * and [decodeBufferedAsync] APIs.
         *
         * @param [decoder] The [Decoder] to use.
         * @param [throwOnOverflow] If `true` and [EncoderDecoder.Config.decodeOutMaxSizeOrFail]
         *   throws an [EncodingSizeException], it will be re-thrown. If `false`, the exception
         *   will be ignored and stream decoding to the buffer will continue.
         * @param [buf] The pre-allocated array to use as the buffer. Its size must be
         *   greater than [EncoderDecoder.Config.maxDecodeEmit].
         * @param [action] The function to flush the buffer to; a destination to "write"
         *   decoded data to whereby `len` is the number of bytes within `buf`, starting
         *   at index `offset`, to "write".
         *
         * @return The number of decoded bytes.
         *
         * @see [CharArray.decodeToByteArray]
         * @see [CharArray.decodeToByteArrayOrNull]
         * @see [CharArray.decodeBufferedAsync]
         *
         * @throws [EncodingException] If decoding failed, such as the [decoder] rejecting
         *   an invalid character or sequence.
         * @throws [EncodingSizeException] If [EncoderDecoder.Config.decodeOutMaxSizeOrFail]
         *   threw its exception and [throwOnOverflow] is `true`.
         * @throws [IllegalArgumentException] If [buf] size is less than or equal to
         *   [EncoderDecoder.Config.maxDecodeEmit].
         * */
        @JvmStatic
        @Throws(EncodingException::class)
        public fun CharArray.decodeBuffered(
            decoder: Decoder<*>,
            throwOnOverflow: Boolean,
            buf: ByteArray,
            action: (buf: ByteArray, offset: Int, len: Int) -> Unit,
        ): Long = decoder.decodeBuffered(
            buf = buf,
            maxBufSize = buf.size,
            throwOnOverflow = throwOnOverflow,
            _get = ::get,
            _input = { DecoderInput(this) },
            _action = action,
        )

        /**
         * Decode a [CharArray] using a buffer of maximum size [DEFAULT_BUFFER_SIZE].
         *
         * The decoding operation will allocate a single buffer, streaming decoded bytes
         * to it and flushing to [action] when needed. If the pre-calculated size
         * returned by [EncoderDecoder.Config.decodeOutMaxSizeOrFail] is less than or
         * equal to the [DEFAULT_BUFFER_SIZE], then a buffer of that size will be allocated
         * and [action] is only invoked once (single-shot decoding). In the event that
         * [EncoderDecoder.Config.decodeOutMaxSizeOrFail] throws its [EncodingSizeException]
         * due to an overflow (i.e. decoding would exceed [Int.MAX_VALUE]) while [throwOnOverflow]
         * is `false`, or its return value is greater than [DEFAULT_BUFFER_SIZE], then this
         * function will always stream decode to a buffer while flushing to [action] until
         * the decoding operation has completed.
         *
         * **NOTE:** Documented exceptions thrown by this function do not include those
         * for which [action] may throw.
         *
         * e.g. (Using `io.matthewnelson.kmp-file:async` & module `:utf8`)
         *
         *     AsyncFs.Default.with {
         *         "/path/to/file.txt".toFile()
         *             .openWriteAsync(excl = null)
         *             .useAsync { stream ->
         *                 val chars = "Some long string"
         *                     .toCharArray()
         *                 val n = chars.decodeBufferedAsync(
         *                     UTF8,
         *                     false,
         *                     stream::writeAsync,
         *                 )
         *                 println("Wrote $n UTF-8 bytes to file.txt")
         *             }
         *     }
         *
         * **NOTE:** The [Decoder] implementation must be compatible with version `2.6.0+`
         * APIs and define a [EncoderDecoder.Config.maxDecodeEmit]. If the value is `-1` (i.e.
         * it has not updated to the new API yet), then this function will fail with an
         * [EncodingException]. All implementations provided by this library have been updated
         * to meet the API requirement; only [EncoderDecoder] implementations external to this
         * library that have not updated yet may fail when using them with [decodeBuffered]
         * and [decodeBufferedAsync] APIs.
         *
         * @param [decoder] The [Decoder] to use.
         * @param [throwOnOverflow] If `true` and [EncoderDecoder.Config.decodeOutMaxSizeOrFail]
         *   throws an [EncodingSizeException], it will be re-thrown. If `false`, the exception
         *   will be ignored and stream decoding to the buffer will continue.
         * @param [action] The suspend function to flush the buffer to; a destination to
         *   "write" decoded data to whereby `len` is the number of bytes within `buf`,
         *   starting at index `offset`, to "write".
         *
         * @return The number of decoded bytes.
         *
         * @see [CharArray.decodeToByteArray]
         * @see [CharArray.decodeToByteArrayOrNull]
         * @see [CharArray.decodeBuffered]
         *
         * @throws [CancellationException]
         * @throws [EncodingException] If decoding failed, such as the [decoder] rejecting
         *   an invalid character or sequence.
         * @throws [EncodingSizeException] If [EncoderDecoder.Config.decodeOutMaxSizeOrFail]
         *   threw its exception and [throwOnOverflow] is `true`.
         * */
        @JvmStatic
        @Throws(CancellationException::class, EncodingException::class)
        public suspend inline fun CharArray.decodeBufferedAsync(
            decoder: Decoder<*>,
            throwOnOverflow: Boolean,
            noinline action: suspend (buf: ByteArray, offset: Int, len: Int) -> Unit,
        ): Long = decodeBufferedAsync(decoder, throwOnOverflow, DEFAULT_BUFFER_SIZE, action)

        /**
         * Decode a [CharArray] using a buffer of maximum size [maxBufSize].
         *
         * The decoding operation will allocate a single buffer, streaming decoded bytes
         * to it and flushing to [action] when needed. If the pre-calculated size
         * returned by [EncoderDecoder.Config.decodeOutMaxSizeOrFail] is less than or
         * equal to the [maxBufSize], then a buffer of that size will be allocated
         * and [action] is only invoked once (single-shot decoding). In the event that
         * [EncoderDecoder.Config.decodeOutMaxSizeOrFail] throws its [EncodingSizeException]
         * due to an overflow (i.e. decoding would exceed [Int.MAX_VALUE]) while [throwOnOverflow]
         * is `false`, or its return value is greater than [maxBufSize], then this
         * function will always stream decode to a buffer while flushing to [action] until
         * the decoding operation has completed.
         *
         * **NOTE:** Documented exceptions thrown by this function do not include those
         * for which [action] may throw.
         *
         * e.g. (Using `io.matthewnelson.kmp-file:async` & module `:utf8`)
         *
         *     AsyncFs.Default.with {
         *         "/path/to/file.txt".toFile()
         *             .openWriteAsync(excl = null)
         *             .useAsync { stream ->
         *                 val chars = "Some long string"
         *                     .toCharArray()
         *                 val n = chars.decodeBufferedAsync(
         *                     UTF8,
         *                     false,
         *                     1024,
         *                     stream::writeAsync,
         *                 )
         *                 println("Wrote $n UTF-8 bytes to file.txt")
         *             }
         *     }
         *
         * **NOTE:** The [Decoder] implementation must be compatible with version `2.6.0+`
         * APIs and define a [EncoderDecoder.Config.maxDecodeEmit]. If the value is `-1` (i.e.
         * it has not updated to the new API yet), then this function will fail with an
         * [EncodingException]. All implementations provided by this library have been updated
         * to meet the API requirement; only [EncoderDecoder] implementations external to this
         * library that have not updated yet may fail when using them with [decodeBuffered]
         * and [decodeBufferedAsync] APIs.
         *
         * @param [decoder] The [Decoder] to use.
         * @param [throwOnOverflow] If `true` and [EncoderDecoder.Config.decodeOutMaxSizeOrFail]
         *   throws an [EncodingSizeException], it will be re-thrown. If `false`, the exception
         *   will be ignored and stream decoding to the buffer will continue.
         * @param [maxBufSize] The maximum size buffer this function will allocate. Must
         *   be greater than [EncoderDecoder.Config.maxDecodeEmit].
         * @param [action] The suspend function to flush the buffer to; a destination to
         *   "write" decoded data to whereby `len` is the number of bytes within `buf`,
         *   starting at index `offset`, to "write".
         *
         * @return The number of decoded bytes.
         *
         * @see [CharArray.decodeToByteArray]
         * @see [CharArray.decodeToByteArrayOrNull]
         * @see [CharArray.decodeBuffered]
         *
         * @throws [CancellationException]
         * @throws [EncodingException] If decoding failed, such as the [decoder] rejecting
         *   an invalid character or sequence.
         * @throws [EncodingSizeException] If [EncoderDecoder.Config.decodeOutMaxSizeOrFail]
         *   threw its exception and [throwOnOverflow] is `true`.
         * @throws [IllegalArgumentException] If [maxBufSize] is less than or equal to
         *   [EncoderDecoder.Config.maxDecodeEmit].
         * */
        @JvmStatic
        @Throws(CancellationException::class, EncodingException::class)
        public suspend fun CharArray.decodeBufferedAsync(
            decoder: Decoder<*>,
            throwOnOverflow: Boolean,
            maxBufSize: Int,
            action: suspend (buf: ByteArray, offset: Int, len: Int) -> Unit,
        ): Long = decoder.decodeBuffered(
            buf = null,
            maxBufSize = maxBufSize,
            throwOnOverflow = throwOnOverflow,
            _get = ::get,
            _input = { DecoderInput(this) },
            _action = { buf, offset, len -> action(buf, offset, len) },
        )

        /**
         * Decode a [CharArray] using the provided pre-allocated, reusable, [buf] array.
         *
         * The decoding operation will stream decoded bytes to the provided [buf], flushing
         * to [action] when needed. If the pre-calculated size returned by
         * [EncoderDecoder.Config.decodeOutMaxSizeOrFail] is less than or equal to the [buf]
         * size, then [action] is only invoked once (single-shot decoding). In the event that
         * [EncoderDecoder.Config.decodeOutMaxSizeOrFail] throws its [EncodingSizeException]
         * due to an overflow (i.e. decoding would exceed [Int.MAX_VALUE]) while [throwOnOverflow]
         * is `false`, or its return value is greater than [buf] size, then this
         * function will always stream decode to a buffer while flushing to [action] until
         * the decoding operation has completed.
         *
         * **NOTE:** Documented exceptions thrown by this function do not include those
         * for which [action] may throw.
         *
         * **NOTE:** If [EncoderDecoder.Config.backFillBuffers] is `true`, provided [buf]
         * array will be back-filled with `0` bytes upon decoding completion.
         *
         * e.g. (Using `io.matthewnelson.kmp-file:async` & module `:utf8`)
         *
         *     AsyncFs.Default.with {
         *         "/path/to/file.txt".toFile()
         *             .openWriteAsync(excl = null)
         *             .useAsync { stream ->
         *                 val chars = "Some long string"
         *                     .toCharArray()
         *                 val buf = ByteArray(DEFAULT_BUFFER_SIZE)
         *                 var n = chars.decodeBufferedAsync(
         *                     UTF8,
         *                     false,
         *                     buf,
         *                     stream::writeAsync,
         *                 )
         *                 n += chars.decodeBufferedAsync(
         *                     UTF8,
         *                     false,
         *                     buf,
         *                     stream::writeAsync,
         *                 )
         *                 println("Wrote $n UTF-8 bytes to file.txt")
         *             }
         *     }
         *
         * **NOTE:** The [Decoder] implementation must be compatible with version `2.6.0+`
         * APIs and define a [EncoderDecoder.Config.maxDecodeEmit]. If the value is `-1` (i.e.
         * it has not updated to the new API yet), then this function will fail with an
         * [EncodingException]. All implementations provided by this library have been updated
         * to meet the API requirement; only [EncoderDecoder] implementations external to this
         * library that have not updated yet may fail when using them with [decodeBuffered]
         * and [decodeBufferedAsync] APIs.
         *
         * @param [decoder] The [Decoder] to use.
         * @param [throwOnOverflow] If `true` and [EncoderDecoder.Config.decodeOutMaxSizeOrFail]
         *   throws an [EncodingSizeException], it will be re-thrown. If `false`, the exception
         *   will be ignored and stream decoding to the buffer will continue.
         * @param [buf] The pre-allocated array to use as the buffer. Its size must be
         *   greater than [EncoderDecoder.Config.maxDecodeEmit].
         * @param [action] The suspend function to flush the buffer to; a destination to
         *   "write" decoded data to whereby `len` is the number of bytes within `buf`,
         *   starting at index `offset`, to "write".
         *
         * @return The number of decoded bytes.
         *
         * @see [CharArray.decodeToByteArray]
         * @see [CharArray.decodeToByteArrayOrNull]
         * @see [CharArray.decodeBuffered]
         *
         * @throws [CancellationException]
         * @throws [EncodingException] If decoding failed, such as the [decoder] rejecting
         *   an invalid character or sequence.
         * @throws [EncodingSizeException] If [EncoderDecoder.Config.decodeOutMaxSizeOrFail]
         *   threw its exception and [throwOnOverflow] is `true`.
         * @throws [IllegalArgumentException] If [buf] size is less than or equal to
         *   [EncoderDecoder.Config.maxDecodeEmit].
         * */
        @JvmStatic
        @Throws(CancellationException::class, EncodingException::class)
        public suspend fun CharArray.decodeBufferedAsync(
            decoder: Decoder<*>,
            throwOnOverflow: Boolean,
            buf: ByteArray,
            action: suspend (buf: ByteArray, offset: Int, len: Int) -> Unit,
        ): Long = decoder.decodeBuffered(
            buf = buf,
            maxBufSize = buf.size,
            throwOnOverflow = throwOnOverflow,
            _get = ::get,
            _input = { DecoderInput(this) },
            _action = { _buf, offset, len -> action(_buf, offset, len) },
        )

        /**
         * TODO: Remove. See https://github.com/05nelsonm/encoding/issues/225
         * @suppress
         * */
        @JvmStatic
        @Throws(EncodingException::class)
        @Deprecated(
            message = "Will be removed upon 2.6.0 release",
            replaceWith = ReplaceWith("decodeBuffered(decoder, false, action)")
        )
        public inline fun CharSequence.decodeBuffered(
            decoder: Decoder<*>,
            noinline action: (buf: ByteArray, offset: Int, len: Int) -> Unit,
        ): Long = decodeBuffered(decoder, false, DEFAULT_BUFFER_SIZE, action)

        /**
         * TODO: Remove. See https://github.com/05nelsonm/encoding/issues/225
         * @suppress
         * */
        @JvmStatic
        @Throws(EncodingException::class)
        @Deprecated(
            message = "Will be removed upon 2.6.0 release",
            replaceWith = ReplaceWith("decodeBuffered(decoder, false, maxBufSize, action)")
        )
        public fun CharSequence.decodeBuffered(
            maxBufSize: Int,
            decoder: Decoder<*>,
            action: (buf: ByteArray, offset: Int, len: Int) -> Unit,
        ): Long = decodeBuffered(decoder, false, maxBufSize, action)

        /**
         * TODO: Remove. See https://github.com/05nelsonm/encoding/issues/225
         * @suppress
         * */
        @JvmStatic
        @Throws(CancellationException::class, EncodingException::class)
        @Deprecated(
            message = "Will be removed upon 2.6.0 release",
            replaceWith = ReplaceWith("decodeBufferedAsync(decoder, false, action)")
        )
        public suspend inline fun CharSequence.decodeBufferedAsync(
            decoder: Decoder<*>,
            noinline action: suspend (buf: ByteArray, offset: Int, len: Int) -> Unit,
        ): Long = decodeBufferedAsync(decoder, false, DEFAULT_BUFFER_SIZE, action)

        /**
         * TODO: Remove. See https://github.com/05nelsonm/encoding/issues/225
         * @suppress
         * */
        @JvmStatic
        @Throws(CancellationException::class, EncodingException::class)
        @Deprecated(
            message = "Will be removed upon 2.6.0 release",
            replaceWith = ReplaceWith("decodeBufferedAsync(decoder, false, maxBufSize, action)")
        )
        public suspend fun CharSequence.decodeBufferedAsync(
            maxBufSize: Int,
            decoder: Decoder<*>,
            action: suspend (buf: ByteArray, offset: Int, len: Int) -> Unit,
        ): Long = decodeBufferedAsync(decoder, false, maxBufSize, action)

        /**
         * DEPRECATED since `2.3.0`
         * @throws [EncodingException] If decoding failed, such as the [decoder] rejecting
         *   an invalid character or sequence.
         * @throws [EncodingSizeException] If the decoded output would exceed [Int.MAX_VALUE].
         * @suppress
         * */
        @JvmStatic
        @Throws(EncodingException::class)
        @Deprecated(
            message = "Should not utilize. Underlying Byte to Char conversion can produce incorrect results",
            level = DeprecationLevel.ERROR,
        )
        public fun ByteArray.decodeToByteArray(decoder: Decoder<*>): ByteArray {
            @Suppress("DEPRECATION_ERROR")
            return decoder.decode(DecoderInput(this), _get = { i -> this[i].toInt().toChar() })
        }

        /**
         * DEPRECATED since `2.3.0`
         * @suppress
         * */
        @JvmStatic
        @Deprecated(
            message = "Should not utilize. Underlying Byte to Char conversion can produce incorrect results",
            level = DeprecationLevel.ERROR,
        )
        public fun ByteArray.decodeToByteArrayOrNull(decoder: Decoder<*>): ByteArray? {
            return try {
                @Suppress("DEPRECATION_ERROR")
                decodeToByteArray(decoder)
            } catch (_: EncodingException) {
                null
            }
        }
    }
}
