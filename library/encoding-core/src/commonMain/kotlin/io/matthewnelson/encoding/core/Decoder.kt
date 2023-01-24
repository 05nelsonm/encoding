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
@file:Suppress("SpellCheckingInspection", "RemoveRedundantQualifierName")

package io.matthewnelson.encoding.core

import io.matthewnelson.encoding.core.internal.closedException
import io.matthewnelson.encoding.core.internal.decode
import io.matthewnelson.encoding.core.internal.isSpaceOrNewLine
import io.matthewnelson.encoding.core.util.DecoderInput
import io.matthewnelson.encoding.core.util.char
import kotlin.jvm.JvmStatic

/**
 * Decode things.
 *
 * @see [EncoderDecoder]
 * @see [decodeToByteArray]
 * @see [decodeToByteArrayOrNull]
 * @see [Decoder.Feed]
 * @see [newDecoderFeed]
 * */
public sealed class Decoder<C: EncoderDecoder.Config>(public val config: C) {

    /**
     * Creates a new [Decoder.Feed] for the [Decoder], outputting
     * decoded bytes to the provided [OutFeed].
     *
     * e.g.
     *
     *     val sb = StringBuilder()
     *     myDecoder.newDecoderFeed { decodedByte ->
     *         sb.append(decodedByte.toInt().toChar())
     *     }.use { feed ->
     *         "ENCODED TEXT".forEach { c ->
     *             feed.consume(c)
     *         }
     *     }
     *     println(sb.toString())
     *
     * @see [Decoder.Feed]
     * @sample [io.matthewnelson.encoding.base16.Base16.newDecoderFeed]
     * */
    @ExperimentalEncodingApi
    public abstract fun newDecoderFeed(out: Decoder.OutFeed): Decoder<C>.Feed

    /**
     * Encoded data is fed into [consume], and upon the [Decoder.Feed]'s
     * buffer filling, decoded data is pushed to the supplied [OutFeed].
     * This allows for a "lazy" decode, or streaming.
     *
     * Once all the data has been fed through [consume], call
     * [doFinal] to close the [Decoder.Feed] and perform encoding
     * finalization for leftover data still in the [Decoder.Feed]'s
     * buffer. Alternatively, utilize the [use] extension function
     * which will call [doFinal] for you, or [close] if there was a
     * decoding error.
     *
     * @see [newDecoderFeed]
     * @see [EncoderDecoder.Feed]
     * @see [EncoderDecoder.Feed.doFinal]
     * @see [use]
     * */
    public abstract inner class Feed
    @ExperimentalEncodingApi
    constructor(): EncoderDecoder.Feed<C>(config) {
        private var isClosed = false
        private var isPaddingSet = false

        /**
         * Updates the [Decoder.Feed] with a new character to decode.
         *
         * @throws [EncodingException] if [isClosed] is true, or if
         *   there was an error decoding.
         * */
        @ExperimentalEncodingApi
        @Throws(EncodingException::class)
        public fun consume(input: Char) {
            if (isClosed) throw closedException()

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
                    isPaddingSet = true
                    return
                }

                if (isPaddingSet) {
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

        @ExperimentalEncodingApi
        final override fun close() { isClosed = true }
        final override fun isClosed(): Boolean = isClosed
        final override fun toString(): String = "${this@Decoder}.Decoder.Feed@${hashCode()}"

        @Throws(EncodingException::class)
        protected abstract fun consumeProtected(input: Char)
    }

    /**
     * A callback for returning decoded bytes as they
     * are produced by [Decoder.Feed].
     *
     * @see [newDecoderFeed]
     * */
    public fun interface OutFeed {
        public fun output(decoded: Byte)
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
        @OptIn(ExperimentalEncodingApi::class)
        public fun CharSequence.decodeToByteArray(decoder: Decoder<*>): ByteArray {
            return decoder.decode(DecoderInput(this)) { feed ->
                forEach { c ->
                    feed.consume(c)
                }
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
        @OptIn(ExperimentalEncodingApi::class)
        public fun CharArray.decodeToByteArray(decoder: Decoder<*>): ByteArray {
            return decoder.decode(DecoderInput(this)) { feed ->
                forEach { c ->
                    feed.consume(c)
                }
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
         * Decodes a [ByteArray] for the provided [decoder] and
         * returns the decoded bytes.
         *
         * @see [decodeToByteArrayOrNull]
         * @throws [EncodingException] if decoding failed.
         * */
        @JvmStatic
        @Throws(EncodingException::class)
        @OptIn(ExperimentalEncodingApi::class)
        public fun ByteArray.decodeToByteArray(decoder: Decoder<*>): ByteArray {
            return decoder.decode(DecoderInput(this)) { feed ->
                forEach { b ->
                    feed.consume(b.char)
                }
            }
        }

        @JvmStatic
        public fun ByteArray.decodeToByteArrayOrNull(decoder: Decoder<*>): ByteArray? {
            return try {
                decodeToByteArray(decoder)
            } catch (_: EncodingException) {
                null
            }
        }
    }
}
