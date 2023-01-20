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

import io.matthewnelson.encoding.core.util.char
import kotlin.jvm.JvmStatic

/**
 * Encode things.
 *
 * @see [EncoderDecoder]
 * @see [encodeToString]
 * @see [encodeToCharArray]
 * @see [encodeToByteArray]
 * @see [Feed]
 * @see [newEncoderFeed]
 * */
public sealed class Encoder(config: EncoderDecoder.Config): Decoder(config) {

    /**
     * Creates a new [Encoder.Feed] for the [Encoder], outputting
     * encoded bytes to the provided [OutFeed].
     *
     * @see [Encoder.Feed]
     * @sample [io.matthewnelson.encoding.base16.Base16.newEncoderFeed]
     * */
    @ExperimentalEncodingApi
    public abstract fun newEncoderFeed(out: OutFeed): Encoder.Feed

    /**
     * Data goes into [update], and upon the [Encoder]
     * implementation's buffer filling, encoded data is fed
     * to [OutFeed] allowing for a "lazy" encode and streaming.
     *
     * Once all the data has been submitted via [update], call
     * [doFinal] to close the [Encoder.Feed] and perform
     * finalization for leftover data still in the [Encoder.Feed]
     * implementation's buffer.
     *
     * @see [newEncoderFeed]
     * @see [EncoderDecoder.Feed]
     * @see [use]
     * */
    public abstract inner class Feed
    @ExperimentalEncodingApi
    constructor(): EncoderDecoder.Feed() {
        protected abstract override fun updateProtected(input: Byte)
        protected abstract override fun doFinalProtected()
        final override fun toString(): String = "${this@Encoder}.Encoder.Feed@${hashCode()}"
    }

    public companion object {

        /**
         * Encodes a [ByteArray] for the provided [encoder] and
         * returns the encoded data in the form of a [String].
         *
         * @throws [EncodingSizeException] if the encoded output
         *   exceeds [Int.MAX_VALUE]. This is not applicable for
         *   most encoding specifications as the majority compress
         *   data, but is something that can occur with Base16
         *   which produces 2 characters for every 1 byte of input.
         * */
        @JvmStatic
        @Throws(EncodingSizeException::class)
        public fun ByteArray.encodeToString(encoder: Encoder): String {
            val sb = StringBuilder(encoder.config.encodeOutSize(size))
            encoder.encode(this) { byte ->
                sb.append(byte.char)
            }
            return sb.toString()
        }

        /**
         * Encodes a [ByteArray] for the provided [encoder] and
         * returns the encoded data in the form of a [CharArray].
         *
         * @throws [EncodingSizeException] if the encoded output
         *   exceeds [Int.MAX_VALUE]. This is not applicable for
         *   most encoding specifications as the majority compress
         *   data, but is something that can occur with Base16
         *   which produces 2 characters for every 1 byte of input.
         * */
        @JvmStatic
        @Throws(EncodingSizeException::class)
        public fun ByteArray.encodeToCharArray(encoder: Encoder): CharArray {
            val ca = CharArray(encoder.config.encodeOutSize(size))
            var i = 0
            encoder.encode(this) { byte ->
                ca[i++] = byte.char
            }
            return ca
        }

        /**
         * Encodes a [ByteArray] for the provided [encoder] and
         * returns the encoded data in the form of a [ByteArray].
         *
         * @throws [EncodingSizeException] if the encoded output
         *   exceeds [Int.MAX_VALUE]. This is not applicable for
         *   most encoding specifications as the majority compress
         *   data, but is something that can occur with Base16
         *   which produces 2 characters for every 1 byte of input.
         * */
        @JvmStatic
        @Throws(EncodingSizeException::class)
        public fun ByteArray.encodeToByteArray(encoder: Encoder): ByteArray {
            val ba = ByteArray(encoder.config.encodeOutSize(size))
            var i = 0
            encoder.encode(this) { byte ->
                ba[i++] = byte
            }
            return ba
        }

        @OptIn(ExperimentalEncodingApi::class)
        private fun Encoder.encode(bytes: ByteArray, out: OutFeed) {
            if (bytes.isEmpty()) return

            newEncoderFeed(out).use { feed ->
                for (byte in bytes) {
                    feed.update(byte)
                }
            }
        }
    }
}
