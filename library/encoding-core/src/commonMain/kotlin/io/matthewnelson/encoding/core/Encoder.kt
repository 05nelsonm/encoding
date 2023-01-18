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

import io.matthewnelson.encoding.core.internal.closedException
import kotlin.jvm.JvmStatic

/**
 * Encode things.
 *
 * @see [encodeToString]
 * @see [encodeToCharArray]
 * @see [encodeToByteArray]
 * @see [Feed]
 * @see [newEncoderFeed]
 * */
public sealed class Encoder: Decoder() {

    // TODO: This should be moved to the Configuration
    //  and instead accept a wrapper class which holds
    //  Any.
    public abstract fun encodedOutSize(inSize: Int): Int

    /**
     * Creates a new [Encoder.Feed] for the [Encoder], outputting
     * encoded bytes to the provided [OutFeed].
     *
     * @see [Encoder.Feed]
     * */
    @ExperimentalEncodingApi
    public abstract fun newEncoderFeed(out: OutFeed): Encoder.Feed

    /**
     * Data goes into [update], and upon the [Encoder]
     * implementation's buffer filling, encoded data is fed
     * to [OutFeed] allowing for a "lazy" encode and streaming.
     *
     * Once all the data has been submitted via [update], call
     * [doFinal] to close the [Feed] which is where the [Encoder]
     * will finalize the encoding (such as applying padding, if
     * specified)
     * */
    public abstract inner class Feed
    @ExperimentalEncodingApi
    constructor() {

        public var isClosed: Boolean = false
            private set

        protected abstract fun updateProtected(b: Byte)
        protected abstract fun doFinalProtected()

        @ExperimentalEncodingApi
        @Throws(EncodingException::class)
        public fun update(b: Byte) {
            if (isClosed) throw closedException()
            updateProtected(b)
        }

        @ExperimentalEncodingApi
        @Throws(EncodingException::class)
        public fun doFinal() {
            if (isClosed) throw closedException()
            isClosed = true
            doFinalProtected()
        }

        final override fun toString(): String = "${this@Encoder}.Encoder.Feed@${hashCode()}"
    }

    public companion object {

        /**
         * Encodes a [ByteArray] for the provided [encoder] and
         * returns the encoded data in the form of a [String].
         * */
        @JvmStatic
        public fun ByteArray.encodeToString(encoder: Encoder): String {
            val sb = StringBuilder(encoder.encodedOutSize(size))
            encoder.encode(this) { byte ->
                sb.append(byte.toInt().toChar())
            }
            return sb.toString()
        }

        /**
         * Encodes a [ByteArray] for the provided [encoder] and
         * returns the encoded data in the form of a [CharArray].
         * */
        @JvmStatic
        public fun ByteArray.encodeToCharArray(encoder: Encoder): CharArray {
            val ca = CharArray(encoder.encodedOutSize(size))
            var i = 0
            encoder.encode(this) { byte ->
                ca[i++] = byte.toInt().toChar()
            }
            return ca
        }

        /**
         * Encodes a [ByteArray] for the provided [encoder] and
         * returns the encoded data in the form of a [ByteArray].
         * */
        @JvmStatic
        public fun ByteArray.encodeToByteArray(encoder: Encoder): ByteArray {
            val ba = ByteArray(encoder.encodedOutSize(size))
            var i = 0
            encoder.encode(this) { byte ->
                ba[i++] = byte
            }
            return ba
        }

        @OptIn(ExperimentalEncodingApi::class)
        private fun Encoder.encode(bytes: ByteArray, out: OutFeed) {
            val feed = newEncoderFeed(out)
            for (byte in bytes) {
                feed.update(byte)
            }
            feed.doFinal()
        }
    }
}
