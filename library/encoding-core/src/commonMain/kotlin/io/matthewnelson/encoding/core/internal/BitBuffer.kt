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
package io.matthewnelson.encoding.core.internal

@InternalEncodingApi
public abstract class BitBuffer<T: Number>(
    private val blockSize: Byte,
    private val onUpdate: (buffer: T, bits: T) -> T,
    private val onOutput: (buffer: T) -> Unit,
    private val doFinal: (count: Byte, buffer: T) -> Unit,
) {
    private var count: Byte = 0

    // JS doesn't like casting so these
    // are abstract because of the need
    // to declare a Number type parameter.
    // Initialize as 0
    protected abstract var bitBuffer: T

    // Should set bitBuffer to 0
    protected abstract fun resetBitBuffer()

    protected abstract fun definitelyDoNotUseThisClassItWillBeChanging()

    public fun update(bits: T) {
        bitBuffer = onUpdate.invoke(bitBuffer, bits)

        if (++count % blockSize == 0) {
            onOutput.invoke(bitBuffer)
            count = 0
            resetBitBuffer()
        }
    }

    public fun doFinal() {
        doFinal.invoke(count, bitBuffer)
    }
}
