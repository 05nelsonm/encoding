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

import io.matthewnelson.encoding.core.internal.encode
import io.matthewnelson.encoding.core.internal.encodeOutSizeOrFail
import io.matthewnelson.encoding.core.util.buffer.EncodingBuffer
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
public sealed class Encoder<C: EncoderDecoder.Config>(config: C): Decoder<C>(config) {

    /**
     * Creates a new [Encoder.Feed] for the [Encoder], outputting
     * encoded bytes to the provided [OutFeed].
     *
     * e.g.
     *
     *     file.outputStream().use { oStream ->
     *         myEncoder.newEncoderFeed { encodedByte ->
     *             oStream.write(encodedByte.toInt())
     *         }.use { feed ->
     *             "Hello World!".forEach { c ->
     *                 feed.consume(c.code.toByte())
     *             }
     *         }
     *     }
     *
     * @see [Encoder.Feed]
     * @sample [io.matthewnelson.encoding.base16.Base16.newEncoderFeed]
     * */
    @ExperimentalEncodingApi
    public abstract fun newEncoderFeed(out: OutFeed): Encoder<C>.Feed

    /**
     * Data to encode is fed into [consume], and upon the [Encoder.Feed]'s
     * buffer filling, encoded data is pushed to the supplied [OutFeed].
     * This allows for a "lazy" encode, or streaming.
     *
     * Once all the data has been fed through [consume], call
     * [doFinal] to close the [Encoder.Feed] and perform decoding
     * finalization for leftover data still in the [Encoder.Feed]'s
     * buffer. Alternatively, utilize the [use] extension function
     * which will call [doFinal] for you, or [close] if there was an
     * encoding error.
     *
     * @see [newEncoderFeed]
     * @see [EncoderDecoder.Feed]
     * @see [use]
     * @see [EncodingBuffer]
     * */
    public abstract inner class Feed
    @ExperimentalEncodingApi
    constructor(): EncoderDecoder.Feed<C>(config) {
        protected abstract override fun consumeProtected(input: Byte)
        protected abstract override fun doFinalProtected()
        final override fun toString(): String = "${this@Encoder}.Encoder.Feed@${hashCode()}"
    }

    public companion object {

        /**
         * Encodes a [ByteArray] for the provided [encoder] and
         * returns the encoded data in the form of a [String].
         *
         * @throws [EncodingSizeException] if the encoded output
         *   exceeds [Int.MAX_VALUE]. This is **not applicable** for
         *   most encoding specifications as the majority compress
         *   data, but is something that can occur with Base16 (hex)
         *   as it produces 2 characters of output for every 1 byte
         *   of input.
         * */
        @JvmStatic
        @Throws(EncodingSizeException::class)
        public fun ByteArray.encodeToString(encoder: Encoder<*>): String {
            return encoder.encodeOutSizeOrFail(size) { outSize ->
                val sb = StringBuilder(outSize)

                encoder.encode(this) { encodedByte ->
                    sb.append(encodedByte.char)
                }

                sb.toString()
            }
        }

        /**
         * Encodes a [ByteArray] for the provided [encoder] and
         * returns the encoded data in the form of a [CharArray].
         *
         * @throws [EncodingSizeException] if the encoded output
         *   exceeds [Int.MAX_VALUE]. This is **not applicable** for
         *   most encoding specifications as the majority compress
         *   data, but is something that can occur with Base16 (hex)
         *   as it produces 2 characters of output for every 1 byte
         *   of input.
         * */
        @JvmStatic
        @Throws(EncodingSizeException::class)
        public fun ByteArray.encodeToCharArray(encoder: Encoder<*>): CharArray {
            return encoder.encodeOutSizeOrFail(size) { outSize ->
                val ca = CharArray(outSize)

                var i = 0
                encoder.encode(this) { encodedByte ->
                    ca[i++] = encodedByte.char
                }

                ca
            }
        }

        /**
         * Encodes a [ByteArray] for the provided [encoder] and
         * returns the encoded data in the form of a [ByteArray].
         *
         * @throws [EncodingSizeException] if the encoded output
         *   exceeds [Int.MAX_VALUE]. This is **not applicable** for
         *   most encoding specifications as the majority compress
         *   data, but is something that can occur with Base16 (hex)
         *   as it produces 2 characters of output for every 1 byte
         *   of input.
         * */
        @JvmStatic
        @Throws(EncodingSizeException::class)
        public fun ByteArray.encodeToByteArray(encoder: Encoder<*>): ByteArray {
            return encoder.encodeOutSizeOrFail(size) { outSize ->
                val ba = ByteArray(outSize)

                var i = 0
                encoder.encode(this) { encodedByte ->
                    ba[i++] = encodedByte
                }

                ba
            }
        }
    }
}
