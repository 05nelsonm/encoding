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
 * Decode things.
 *
 * @see [decodeToArray]
 * @see [decodeToArrayOrNull]
 * @see [Feed]
 * @see [newDecoderFeed]
 * */
public sealed class Decoder {

    // TODO: These should be moved to the Configuration
    //  and instead accept a wrapper class which holds
    //  Any. Then based off of the configuration it can
    //  be determined whether or not the it is a valid
    //  encoding for the encoder so we can quit early.
    //  .
    //  Maybe the config generates Regex or something once
    //  it's instantiated, based off of the options that
    //  are set???
    @ExperimentalEncodingApi
    @Throws(EncodingException::class)
    public open fun quickAnalysisIsValid(encoded: String) {}
    @ExperimentalEncodingApi
    @Throws(EncodingException::class)
    public open fun quickAnalysisIsValid(encoded: CharArray) {}
    @ExperimentalEncodingApi
    @Throws(EncodingException::class)
    public open fun quickAnalysisIsValid(encoded: ByteArray) {}

    // TODO: This should be moved to the Configuration
    //  and instead accept a wrapper class which holds
    //  Any.
    public abstract fun decodedOutMaxSize(inSize: Int): Int

    /**
     * Creates a new [Decoder.Feed] for the [Decoder], outputting
     * decoded bytes to the provided [OutFeed].
     *
     * @see [Decoder.Feed]
     * */
    @ExperimentalEncodingApi
    public abstract fun newDecoderFeed(out: OutFeed): Decoder.Feed

    /**
     * Encoded data goes into [update], and upon the [Decoder]
     * implementation's buffer filling, decoded data is fed
     * to [OutFeed] allowing for a "lazy" decode and streaming.
     *
     * Once all the data has been submitted via [update], call
     * [doFinal] to close the [Feed] which is where the [Decoder]
     * will finalize the decoding and check for correctness.
     * */
    public abstract inner class Feed
    @ExperimentalEncodingApi
    constructor() {

        public var isClosed: Boolean = false
            private set

        @Throws(EncodingException::class)
        protected abstract fun updateProtected(c: Char)
        @Throws(EncodingException::class)
        protected abstract fun doFinalProtected()

        @ExperimentalEncodingApi
        @Throws(EncodingException::class)
        public fun update(c: Char) {
            if (isClosed) throw closedException()
            updateProtected(c)
        }

        @ExperimentalEncodingApi
        @Throws(EncodingException::class)
        public fun doFinal() {
            if (isClosed) throw closedException()
            isClosed = true
            doFinalProtected()
        }

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
            decoder.quickAnalysisIsValid(this)
            return decoder.decode(length) {
                forEach { char ->
                    update(char)
                }
            }
        }

        @JvmStatic
        public fun String.decodeToArrayOrNull(decoder: Decoder): ByteArray? {
            return try {
                decodeToArray(decoder)
            } catch (e: EncodingException) {
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
            decoder.quickAnalysisIsValid(this)
            return decoder.decode(size) {
                forEach { char ->
                    update(char)
                }
            }
        }

        @JvmStatic
        public fun CharArray.decodeToArrayOrNull(decoder: Decoder): ByteArray? {
            return try {
                decodeToArray(decoder)
            } catch (e: EncodingException) {
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
            decoder.quickAnalysisIsValid(this)
            return decoder.decode(size) {
                forEach { byte ->
                    update(byte.toInt().toChar())
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
        private fun Decoder.decode(length: Int, update: Decoder.Feed.() -> Unit): ByteArray {
            val size = decodedOutMaxSize(length)
            val ba = ByteArray(size)

            var i = 0
            val feed = newDecoderFeed { byte ->
                ba[i++] = byte
            }

            update.invoke(feed)
            feed.doFinal()

            return if (i == size) {
                ba
            } else {
                ba.copyOf(i)
            }
        }
    }
}
