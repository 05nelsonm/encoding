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
package io.matthewnelson.encoding.core.util

import io.matthewnelson.encoding.core.EncodingException
import kotlin.jvm.JvmStatic

/**
 * An abstraction for performing bitwise operations during
 * the decoding process.
 *
 * Bit shifting does not change for the encoding, what does
 * is the bits which are dependent on the characters used and
 * what they translate to.
 *
 * For example, Base32 has many specs that use different
 * encoding characters, but the bitwise operations are still
 * the same; the same is true for Base64.
 *
 * This class is meant to commonize bit shifting operations
 * such that they are reusable and only need to be written
 * once.
 *
 * @see [Update]
 * @see [Flush]
 * @see [Finalize]
 * @see [truncatedInputEncodingException]
 * @sample [io.matthewnelson.encoding.base32.Base32.DecodingBuffer]
 * */
public abstract class BitBuffer<T: Number>(
    public val blockSize: Byte,
    private val update: Update<T>,
    private val flush: Flush<T>,
    private val finalize: Finalize<T>,
) {
    public var count: Byte = 0
        private set

    /**
     * The buffer of bits.
     *
     * JS hates unchecked casting, so implementors of
     * [BitBuffer] get to initialize this with a value
     * of 0 for whatever type [T] is.
     * */
    protected abstract var bitBuffer: T

    /**
     * Resets the [bitBuffer] back to 0 when called.
     * */
    protected abstract fun reset()

    /**
     * Call to [Update] the [bitBuffer] with new [bits].
     * */
    public fun update(bits: T) {
        bitBuffer = update.invoke(bitBuffer, bits)

        if (++count % blockSize == 0) {
            flush.invoke(bitBuffer)
            count = 0
            reset()
        }
    }

    /**
     * Call to [Finalize] the remaining bits in the [bitBuffer].
     * */
    public fun finalize() {
        finalize.invoke(count, blockSize, bitBuffer)
        count = 0
        reset()
    }

    /**
     * Perform a bitwise operation, and return the result to
     * update the [bitBuffer].
     *
     * Will be invoked whenever [update] is called.
     *
     * e.g.
     *
     *     update = { buffer, bits ->
     *         buffer shl 5 or bits
     *     }
     * */
    public fun interface Update<T: Number> {
        public fun invoke(buffer: T, bits: T): T
    }

    /**
     * When enough bits fill up the [bitBuffer] (i.e. the
     * [blockSize]), [Flush.invoke] will be called and
     * then the [bitBuffer] will immediately afterwards be
     * [reset].
     *
     * e.g.
     *
     *     out: OutFeed,
     *     flush = { buffer ->
     *         out.invoke((buffer shr 32).toByte())
     *         out.invoke((buffer shr 24).toByte())
     *         out.invoke((buffer shr 16).toByte())
     *         out.invoke((buffer shr  8).toByte())
     *         out.invoke((buffer       ).toByte())
     *     },
     * */
    public fun interface Flush<in T: Number> {
        public fun invoke(buffer: T)
    }

    /**
     * When the [BitBuffer] is done being used, [finalize] should
     * be called in order to process the remaining bits in the [bitBuffer].
     *
     * Will be invoked whenever [finalize] is called.
     *
     * e.g.
     *
     *     out: OutFeed,
     *     finalize = { count, blockSize, buf ->
     *         var buffer = buf
     *
     *         when (count % blockSize) {
     *             0 -> {} // perfect block. we done.
     *             1, 3, 6 -> {
     *                 // 5*1 =  5 bits. Truncated, fail.
     *                 // 5*3 = 15 bits. Truncated, fail.
     *                 // 5*6 = 30 bits. Truncated, fail.
     *                 throw truncatedInputEncodingException(count)
     *             }
     *             2 -> {
     *                 // 5*2 = 10 bits. Drop 2
     *                 buf =       buffer shr  2
     *
     *                 // 8/8 = 1 byte
     *                 out.invoke((buffer       ).toByte())
     *             },
     *             // ...
     *         }
     *     },
     * */
    public fun interface Finalize<in T: Number> {
        public fun invoke(count: Byte, blockSize: Byte, buffer: T)
    }

    public companion object {

        @JvmStatic
        public fun truncatedInputEncodingException(count: Byte): EncodingException {
            return EncodingException("Truncate input. Illegal block size of count[$count]")
        }
    }
}
