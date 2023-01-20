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
@file:Suppress("MemberVisibilityCanBePrivate")

package io.matthewnelson.encoding.core

import io.matthewnelson.encoding.core.util.DecoderInput
import io.matthewnelson.encoding.core.util.DecoderInput.Companion.toInputAnalysis
import io.matthewnelson.encoding.core.util.byte
import kotlin.jvm.JvmStatic

/**
 * Decode things.
 *
 * @see [EncoderDecoder]
 * @see [decodeToArray]
 * @see [decodeToArrayOrNull]
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
     * implementation's buffer.
     *
     * @see [newDecoderFeed]
     * @see [EncoderDecoder.Feed]
     * @see [use]
     * */
    public abstract inner class Feed
    @ExperimentalEncodingApi
    constructor(): EncoderDecoder.Feed() {
        final override fun toString(): String = "${this@Decoder}.Decoder.Feed@${hashCode()}"
    }

    public companion object {

        /**
         * Decodes a [String] for the provided [decoder] and
         * returns the decoded bytes.
         *
         * @see [decodeToArrayOrNull]
         * @throws [EncodingException] if decoding failed.
         * */
        @JvmStatic
        @Throws(EncodingException::class)
        @OptIn(ExperimentalEncodingApi::class)
        public fun String.decodeToArray(decoder: Decoder): ByteArray {
            return decoder.decode(toInputAnalysis(decoder.config)) {
                forEach { char ->
                    update(char.byte)
                }
            }
        }

        @JvmStatic
        public fun String.decodeToArrayOrNull(decoder: Decoder): ByteArray? {
            return try {
                decodeToArray(decoder)
            } catch (_: EncodingException) {
                null
            }
        }

        /**
         * Decodes a [CharArray] for the provided [decoder] and
         * returns the decoded bytes.
         *
         * @see [decodeToArrayOrNull]
         * @throws [EncodingException] if decoding failed.
         * */
        @JvmStatic
        @Throws(EncodingException::class)
        @OptIn(ExperimentalEncodingApi::class)
        public fun CharArray.decodeToArray(decoder: Decoder): ByteArray {
            return decoder.decode(toInputAnalysis(decoder.config)) {
                forEach { char ->
                    update(char.byte)
                }
            }
        }

        @JvmStatic
        public fun CharArray.decodeToArrayOrNull(decoder: Decoder): ByteArray? {
            return try {
                decodeToArray(decoder)
            } catch (_: EncodingException) {
                null
            }
        }

        /**
         * Decodes a [ByteArray] for the provided [decoder] and
         * returns the decoded bytes.
         *
         * @see [decodeToArrayOrNull]
         * @throws [EncodingException] if decoding failed.
         * */
        @JvmStatic
        @Throws(EncodingException::class)
        @OptIn(ExperimentalEncodingApi::class)
        public fun ByteArray.decodeToArray(decoder: Decoder): ByteArray {
            return decoder.decode(toInputAnalysis(decoder.config)) {
                forEach { byte ->
                    update(byte)
                }
            }
        }

        @JvmStatic
        public fun ByteArray.decodeToArrayOrNull(decoder: Decoder): ByteArray? {
            return try {
                decodeToArray(decoder)
            } catch (_: EncodingException) {
                null
            }
        }

        @Throws(EncodingException::class)
        @OptIn(ExperimentalEncodingApi::class)
        private fun Decoder.decode(input: DecoderInput, update: Decoder.Feed.() -> Unit): ByteArray {
            val ba = ByteArray(input.decodeOutMaxSize)

            var i = 0
            newDecoderFeed { byte ->
                ba[i++] = byte
            }.use { feed ->
                update.invoke(feed)
            }

            return if (i == input.decodeOutMaxSize) {
                ba
            } else {
                ba.copyOf(i)
            }
        }
    }
}
