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
@file:Suppress("LocalVariableName", "PropertyName", "RemoveRedundantQualifierName")

package io.matthewnelson.encoding.core

import io.matthewnelson.encoding.core.internal.closedException
import io.matthewnelson.encoding.core.internal.decode
import io.matthewnelson.encoding.core.internal.isSpaceOrNewLine
import io.matthewnelson.encoding.core.util.DecoderInput
import kotlin.jvm.JvmField
import kotlin.jvm.JvmName
import kotlin.jvm.JvmStatic
import kotlin.jvm.JvmSynthetic

/**
 * Decode things.
 *
 * @see [EncoderDecoder]
 * @see [Decoder.Feed]
 * */
public sealed class Decoder<C: EncoderDecoder.Config>(public val config: C) {

    /**
     * Creates a new [Decoder.Feed], outputting decoded data to
     * the supplied [Decoder.OutFeed].
     *
     * e.g.
     *
     *     myDecoder.newDecoderFeed { decodedByte ->
     *         println(decodedByte)
     *     }.use { feed ->
     *         "MYencoDEdTEXt".forEach { c -> feed.consume(c) }
     *     }
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
     * @see [newDecoderFeed]
     * @see [use]
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
         * @throws [EncodingException] if [isClosed] is true, or if
         *   there was an error decoding.
         * */
        @Throws(EncodingException::class)
        public fun consume(input: Char) {
            if (_isClosed) throw closedException()

            try {
                if (config.isLenient != null && input.isSpaceOrNewLine()) {
                    if (config.isLenient) {
                        return
                    } else {
                        throw EncodingException("Spaces and new lines are forbidden when isLenient[false]")
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
                    throw EncodingException(
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
         * Flushes the buffered input and performs any final decoding
         * operations without closing the [Feed].
         *
         * @see [EncoderDecoder.Feed.flush]
         * @throws [EncodingException] if [isClosed] is true, or if
         *   there was an error decoding.
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
         * DEPRECATED
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
         * Decodes a [String] for the provided [decoder] and
         * returns the decoded bytes.
         *
         * @see [decodeToByteArrayOrNull]
         * @throws [EncodingException] if decoding failed.
         * */
        @JvmStatic
        @Throws(EncodingException::class)
        public fun CharSequence.decodeToByteArray(decoder: Decoder<*>): ByteArray {
            return decoder.decode(DecoderInput(this)) { feed ->
                forEach { c -> feed.consume(c) }
            }
        }

        @JvmStatic
        public fun CharSequence.decodeToByteArrayOrNull(decoder: Decoder<*>): ByteArray? {
            return try {
                decodeToByteArray(decoder)
            } catch (_: EncodingException) {
                null
            }
        }

        /**
         * Decodes a [CharArray] for the provided [decoder] and
         * returns the decoded bytes.
         *
         * @see [decodeToByteArrayOrNull]
         * @throws [EncodingException] if decoding failed.
         * */
        @JvmStatic
        @Throws(EncodingException::class)
        public fun CharArray.decodeToByteArray(decoder: Decoder<*>): ByteArray {
            return decoder.decode(DecoderInput(this)) { feed ->
                forEach { c -> feed.consume(c) }
            }
        }

        @JvmStatic
        public fun CharArray.decodeToByteArrayOrNull(decoder: Decoder<*>): ByteArray? {
            return try {
                decodeToByteArray(decoder)
            } catch (_: EncodingException) {
                null
            }
        }

        /**
         * DEPRECATED
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
            return decoder.decode(DecoderInput(this)) { feed ->
                forEach { b -> feed.consume(b.toInt().toChar()) }
            }
        }

        /**
         * DEPRECATED
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
