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
import io.matthewnelson.encoding.core.internal.encodeOutSizeOrFail
import io.matthewnelson.encoding.core.util.LineBreakOutFeed
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
     * Creates a new [Encoder.Feed], outputting encoded data to
     * the supplied [Encoder.OutFeed].
     *
     * e.g. (Writing encoded data to a file)
     *
     *     file.outputStream().use { oStream ->
     *         myEncoder.newEncoderFeed { encodedChar ->
     *             oStream.write(encodedChar.code)
     *         }.use { feed ->
     *             "Hello World!".forEach { c ->
     *                 feed.consume(c.code.toByte())
     *             }
     *         }
     *     }
     *
     * @see [Encoder.Feed]
     * */
    public fun newEncoderFeed(out: Encoder.OutFeed): Encoder<C>.Feed {
        return if (config.lineBreakInterval > 0 && out !is LineBreakOutFeed) {
            newEncoderFeedProtected(LineBreakOutFeed(config.lineBreakInterval, out))
        } else {
            newEncoderFeedProtected(out)
        }
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
    public abstract inner class Feed: EncoderDecoder.Feed<C>(config) {
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
            } catch (t: Throwable) {
                close()
                throw t
            }
        }

        public final override fun close() { isClosed = true }
        public final override fun isClosed(): Boolean = isClosed
        public final override fun toString(): String = "${this@Encoder}.Encoder.Feed@${hashCode()}"

        protected abstract fun consumeProtected(input: Byte)
        protected abstract override fun doFinalProtected()
    }

    /**
     * A callback for returning encoded characters as they
     * are produced by [Encoder.Feed].
     *
     * @see [newEncoderFeed]
     * */
    public fun interface OutFeed {
        public fun output(encoded: Char)
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

                encoder.encode(this) { char ->
                    sb.append(char)
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
                encoder.encode(this) { char ->
                    ca[i++] = char
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
                encoder.encode(this) { char ->
                    ba[i++] = char.code.toByte()
                }

                ba
            }
        }
    }
}
