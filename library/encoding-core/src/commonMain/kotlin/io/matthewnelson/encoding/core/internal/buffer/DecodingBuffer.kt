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
public sealed class DecodingBuffer<T: Number>(
    blockSize: Int,
    update: Update<T>,
    flush: Flush<T>,
    finalize: Finalize<T, Unit>,
): Buffer<T, Unit>(blockSize, update, flush, finalize) {

    protected final override fun byteBuffer(internal: Internal): Unit = Unit
    protected final override fun updateBits(bits: T) {}

    public fun update(bits: T) {
        super.updateBits(bits)
    }

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
}
