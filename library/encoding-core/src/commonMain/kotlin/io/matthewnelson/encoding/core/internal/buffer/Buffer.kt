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
package io.matthewnelson.encoding.core.internal.buffer

import io.matthewnelson.encoding.core.EncodingException
import io.matthewnelson.encoding.core.internal.Internal
import io.matthewnelson.encoding.core.internal.InternalEncodingApi
import kotlin.jvm.JvmStatic
import kotlin.jvm.JvmSynthetic

/**
 * An abstraction for performing bitwise operations during
 * the decoding/encoding process.
 *
 * Bit shifting does not change for the encoding, what does
 * is the bits.
 *
 * For example, Base32 has many specs that use different
 * encoding characters, but the bitwise operations are still
 * the same; the same is true for Base64.
 *
 * This class is meant to commonize bit shifting operations
 * such that they are reusable and only need to be written
 * once.
 *
 * @see [EncodingBuffer]
 * @see [DecodingBuffer]
 * @see [Update]
 * @see [Flush]
 * @see [Finalize]
 * @see [truncatedInputEncodingException]
 * */
@InternalEncodingApi
public sealed class Buffer<T: Number, V: Any>(
    public val blockSize: Int,
    private val update: Update<T>,
    private val flush: Flush<T>,
    private val finalize: Finalize<T, V>,
) {
    init {
        require(blockSize > 0) {
            "blockSize must be greater than 0"
        }
    }

    public var count: Int = 0
        private set

    /* get */
    protected open fun bitBuffer(internal: Internal): T {
        throw NotImplementedError()
    }

    /* set */
    protected open fun bitBuffer(internal: Internal, bits: T) {
        throw NotImplementedError()
    }

    /**
     * Resets the [bitBuffer] back to 0 when called.
     * */
    protected open fun reset(internal: Internal) {
        throw NotImplementedError()
    }

    protected open fun byteBuffer(internal: Internal): V {
        throw NotImplementedError()
    }

    protected open fun updateBits(bits: T) {
        val internal = Internal.get()
        bitBuffer(internal, update.invoke(bitBuffer(internal), bits))

        if (++count % blockSize == 0) {
            flush.invoke(bitBuffer(internal))
            count = 0
            reset(internal)
        }
    }

    /**
     * Call to [Finalize] the remaining bits in the [bitBuffer] and,
     * if using the [EncodingBuffer], remaining bytes in the [byteBuffer].
     * */
    public fun finalize() {
        val internal = Internal.get()
        val byteBuffer = byteBuffer(internal)

        (byteBuffer as? ByteArray)?.fill(0, count)
        finalize.invoke(count, blockSize, bitBuffer(internal), byteBuffer)
        (byteBuffer as? ByteArray)?.fill(0, 0, count)
        count = 0
        reset(internal)
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
     * When the [Buffer] is done being used, [finalize] should
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
    public fun interface Finalize<in T: Number, V: Any> {
        public fun invoke(count: Int, blockSize: Int, bitBuffer: T, byteBuffer: V)
    }

    public companion object {

        @JvmStatic
        public fun truncatedInputEncodingException(count: Int): EncodingException {
            return EncodingException("Truncate input. Illegal block size of count[$count]")
        }
    }
}
