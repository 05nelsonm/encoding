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
@file:Suppress("SpellCheckingInspection")

package io.matthewnelson.encoding.core

import io.matthewnelson.encoding.core.internal.decode
import io.matthewnelson.encoding.core.util.DecoderInput
import io.matthewnelson.encoding.core.util.buffer.DecodingBuffer
import io.matthewnelson.encoding.core.util.byte
import kotlin.jvm.JvmStatic

/**
 * Decode things.
 *
 * @see [EncoderDecoder]
 * @see [decodeToByteArray]
 * @see [decodeToByteArrayOrNull]
 * @see [Feed]
 * @see [newDecoderFeed]
 * */
public sealed class Decoder(public val config: EncoderDecoder.Config) {

    /**
     * Creates a new [Decoder.Feed] for the [Decoder], outputting
     * decoded bytes to the provided [OutFeed].
     *
     * @see [Decoder.Feed]
     * @sample [io.matthewnelson.encoding.base16.Base16.newDecoderFeed]
     * */
    @ExperimentalEncodingApi
    public abstract fun newDecoderFeed(out: OutFeed): Decoder.Feed

    /**
     * Encoded data goes into [update], and upon the [Decoder]
     * implementation's buffer filling, decoded data is fed
     * to [OutFeed] allowing for a "lazy" decode and streaming.
     *
     * Once all the data has been submitted via [update], call
     * [doFinal] to close the [Decoder.Feed] and perform
     * finalization for leftover data still in the [Decoder.Feed]
     * implementation's buffer. Alternatively, utilize the [use]
     * extension function.
     *
     * @see [newDecoderFeed]
     * @see [EncoderDecoder.Feed]
     * @see [use]
     * @see [DecodingBuffer]
     * */
    public abstract inner class Feed
    @ExperimentalEncodingApi
    constructor(): EncoderDecoder.Feed(config) {
        final override fun toString(): String = "${this@Decoder}.Decoder.Feed@${hashCode()}"
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
        public fun CharSequence.decodeToByteArray(decoder: Decoder): ByteArray {
            return decoder.decode(DecoderInput(this)) { feed ->
                forEach { c ->
                    feed.update(c.byte)
                }
            }
        }

        @JvmStatic
        public fun CharSequence.decodeToByteArrayOrNull(decoder: Decoder): ByteArray? {
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
        public fun CharArray.decodeToByteArray(decoder: Decoder): ByteArray {
            return decoder.decode(DecoderInput(this)) { feed ->
                forEach { c ->
                    feed.update(c.byte)
                }
            }
        }

        @JvmStatic
        public fun CharArray.decodeToByteArrayOrNull(decoder: Decoder): ByteArray? {
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
        public fun ByteArray.decodeToByteArray(decoder: Decoder): ByteArray {
            return decoder.decode(DecoderInput(this)) { feed ->
                forEach { b ->
                    feed.update(b)
                }
            }
        }

        @JvmStatic
        public fun ByteArray.decodeToByteArrayOrNull(decoder: Decoder): ByteArray? {
            return try {
                decodeToByteArray(decoder)
            } catch (_: EncodingException) {
                null
            }
        }
    }
}
