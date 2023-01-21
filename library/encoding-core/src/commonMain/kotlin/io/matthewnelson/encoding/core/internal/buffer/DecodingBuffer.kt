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

package io.matthewnelson.encoding.core.internal.buffer

import io.matthewnelson.encoding.core.internal.Internal
import io.matthewnelson.encoding.core.internal.InternalEncodingApi

/**
 * A [Buffer] specifically for decoding things.
 *
 * @see [TypeInt]
 * @see [TypeLong]
 * @see [Buffer]
 * */
@InternalEncodingApi
public sealed class DecodingBuffer<T: Number>(
    blockSize: Int,
    update: Update<T>,
    flush: Flush<T>,
    finalize: Finalize<T, Unit>,
): Buffer<T, Unit>(blockSize, update, flush, finalize) {

    /**
     * Update all the things with new [bits].
     * */
    public fun update(bits: T) {
        super.updateBits(bits)
    }

    /**
     * A [DecodingBuffer] that uses [kotlin.Int].
     *
     * @see [Buffer]
     * @sample [io.matthewnelson.encoding.base16.Base16.Base16DecodingBuffer]
     * */
    public abstract class TypeInt(
        blockSize: Int,
        update: Update<Int>,
        flush: Flush<Int>,
        finalize: Finalize<Int, Unit>,
    ): DecodingBuffer<Int>(blockSize, update, flush, finalize) {
        private var bitBuffer: Int = 0
        protected final override fun bitBuffer(internal: Internal): Int = bitBuffer
        protected final override fun bitBuffer(internal: Internal, bits: Int) { bitBuffer = bits }
        protected final override fun reset(internal: Internal) { bitBuffer = 0 }
    }

    /**
     * A [DecodingBuffer] that uses [kotlin.Long].
     *
     * @see [Buffer]
     * @sample [io.matthewnelson.encoding.base32.Base32.Base32DecodingBuffer]
     * */
    public abstract class TypeLong(
        blockSize: Int,
        update: Update<Long>,
        flush: Flush<Long>,
        finalize: Finalize<Long, Unit>,
    ): DecodingBuffer<Long>(blockSize, update, flush, finalize) {
        private var bitBuffer: Long = 0L
        protected final override fun bitBuffer(internal: Internal): Long = bitBuffer
        protected final override fun bitBuffer(internal: Internal, bits: Long) { bitBuffer = bits }
        protected final override fun reset(internal: Internal) { bitBuffer = 0L }
    }

    protected final override fun byteBuffer(internal: Internal): Unit = Unit
    protected final override fun updateBits(bits: T) {}
}
