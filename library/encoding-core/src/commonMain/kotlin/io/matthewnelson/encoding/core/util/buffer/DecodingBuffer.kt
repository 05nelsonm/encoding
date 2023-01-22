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

package io.matthewnelson.encoding.core.util.buffer

import io.matthewnelson.encoding.core.Decoder
import io.matthewnelson.encoding.core.internal.Internal
import io.matthewnelson.encoding.core.internal.InternalEncodingApi

/**
 * Helper for buffering [Decoder.Feed] input.
 *
 * @see [FeedBuffer]
 * @see [FeedBuffer.Flush]
 * @see [FeedBuffer.Finalize]
 * @throws [IllegalArgumentException] if [blockSize] is less
 *   than or equal to 0
 * */
public abstract class DecodingBuffer
@Throws(IllegalArgumentException::class)
constructor(
    blockSize: Int,
    flush: Flush<Int>,
    finalize: Finalize<Int>
): FeedBuffer<Int>(blockSize, flush, finalize) {
    @Suppress("RemoveExplicitTypeArguments")
    private val buffer = Array<Int>(blockSize) { 0 }
    @OptIn(InternalEncodingApi::class)
    protected final override fun buffer(internal: Internal): Array<Int> = buffer
    protected final override fun zero(): Int = 0
}
