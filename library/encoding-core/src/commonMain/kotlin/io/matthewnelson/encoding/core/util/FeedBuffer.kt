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

package io.matthewnelson.encoding.core.util

import io.matthewnelson.encoding.core.Decoder
import io.matthewnelson.encoding.core.Encoder
import io.matthewnelson.encoding.core.EncodingException
import kotlin.jvm.JvmField
import kotlin.jvm.JvmName
import kotlin.jvm.JvmStatic

/**
 * Helper class for [Decoder.Feed] and [Encoder.Feed] to
 * buffer their input until ready to output data via their
 * supplied [Decoder.OutFeed]/[Encoder.OutFeed].
 *
 * @see [Flush]
 * @see [Finalize]
 * @see [truncatedInputEncodingException]
 * @throws [IllegalArgumentException] if [blockSize] is less
 *   than or equal to 0
 * @sample [io.matthewnelson.encoding.base16.Base16.DecodingBuffer]
 * */
public abstract class FeedBuffer
@Throws(IllegalArgumentException::class)
constructor(
    @JvmField
    public val blockSize: Int,
    private val flush: Flush,
    private val finalize: Finalize,
) {

    init {
        require(blockSize > 0) {
            "blockSize must be greater than 0"
        }
    }

    @get:JvmName("count")
    public var count: Int = 0
        private set

    private val buffer = IntArray(blockSize)

    /**
     * Update the [buffer] with new input.
     *
     * Will automatically invoke [flush] in order
     * to output data once the [buffer] fills.
     * */
    public fun update(input: Int) {
        buffer[count] = input

        if (++count % blockSize == 0) {
            flush.invoke(buffer)
            count = 0
        }
    }

    /**
     * Call whenever [Encoder.Feed.doFinalProtected] or
     * [Decoder.Feed.doFinalProtected] is invoked to
     * process the remaining input held in [buffer].
     * */
    public fun finalize() {
        buffer.fill(0, count)
        finalize.invoke(count % blockSize, buffer)
        buffer.fill(0, 0, count)
        count = 0
    }

    /**
     * [Flush.invoke] will be called once the [buffer]
     * fills up, and pass it along to perform bitwise
     * operations on it before outputting results to
     * the supplied [Decoder.OutFeed]/[Encoder.OutFeed].
     *
     * @see [update]
     * */
    public fun interface Flush {
        public fun invoke(buffer: IntArray)
    }

    /**
     * [Finalize.invoke] will be called whenever [finalize]
     * is called to process remaining input held in [buffer].
     *
     * @see [finalize]
     * */
    public fun interface Finalize {
        public fun invoke(modulus: Int, buffer: IntArray)
    }

    public companion object {

        /**
         * Helper for generating a standard [EncodingException] when
         * an illegal modulus is encountered while decoding.
         * */
        @JvmStatic
        public fun truncatedInputEncodingException(modulus: Int): EncodingException {
            return EncodingException("Truncated input. Illegal Modulus[$modulus]")
        }
    }
}
