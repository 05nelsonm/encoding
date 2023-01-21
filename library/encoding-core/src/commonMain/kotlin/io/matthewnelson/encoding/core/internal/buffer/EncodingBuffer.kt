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

import io.matthewnelson.encoding.core.internal.Internal
import io.matthewnelson.encoding.core.internal.InternalEncodingApi

@InternalEncodingApi
public sealed class EncodingBuffer<T: Number>(
    blockSize: Int,
    update: Update<T>,
    flush: Flush<T>,
    finalize: Finalize<T, ByteArray>,
): Buffer<T, ByteArray>(blockSize, update, flush, finalize) {

    protected abstract fun toBits(byte: Byte): T

    private val byteBuffer = ByteArray(blockSize)

    protected final override fun byteBuffer(internal: Internal): ByteArray = byteBuffer
    protected final override fun updateBits(bits: T) {}

    public fun update(byte: Byte) {
        byteBuffer[count] = byte
        super.updateBits(toBits(byte))
    }

    public abstract class TypeInt(
        blockSize: Int,
        update: Update<Int>,
        flush: Flush<Int>,
        finalize: Finalize<Int, ByteArray>,
    ): EncodingBuffer<Int>(blockSize, update, flush, finalize) {
        private var bitBuffer: Int = 0
        protected final override fun bitBuffer(internal: Internal): Int = bitBuffer
        protected final override fun bitBuffer(internal: Internal, bits: Int) { bitBuffer = bits }
        protected final override fun reset(internal: Internal) { bitBuffer = 0 }
    }

    public abstract class TypeLong(
        blockSize: Int,
        update: Update<Long>,
        flush: Flush<Long>,
        finalize: Finalize<Long, ByteArray>,
    ): EncodingBuffer<Long>(blockSize, update, flush, finalize) {
        private var bitBuffer: Long = 0L
        protected final override fun bitBuffer(internal: Internal): Long = bitBuffer
        protected final override fun bitBuffer(internal: Internal, bits: Long) { bitBuffer = bits }
        protected final override fun reset(internal: Internal) { bitBuffer = 0L }
    }
}
