/*
 * Copyright (c) 2025 Matthew Nelson
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 **/
@file:Suppress("LocalVariableName", "NOTHING_TO_INLINE")

package io.matthewnelson.encoding.core.internal

import io.matthewnelson.encoding.core.Encoder
import io.matthewnelson.encoding.core.EncoderDecoder.Config
import io.matthewnelson.encoding.core.EncodingSizeException
import io.matthewnelson.encoding.core.use
import kotlin.contracts.ExperimentalContracts
import kotlin.contracts.InvocationKind
import kotlin.contracts.contract

private const val MAX_ENCODE_OUT_SIZE: Long = Int.MAX_VALUE.toLong()

/**
 * Fails if the returned [Long] for [Config.encodeOutMaxSize] exceeds [Int.MAX_VALUE].
 * */
@OptIn(ExperimentalContracts::class)
@Throws(EncodingSizeException::class)
internal inline fun <T: Any> Encoder<*>.encodeOutMaxSizeOrFail(
    size: Int,
    _block: (maxSize: Int) -> T,
): T {
    contract { callsInPlace(_block, InvocationKind.AT_MOST_ONCE) }

    val maxSize = config.encodeOutMaxSize(size.toLong())
    if (maxSize > MAX_ENCODE_OUT_SIZE) {
        throw Config.outSizeExceedsMaxEncodingSizeException(maxSize, MAX_ENCODE_OUT_SIZE)
    }

    return _block(maxSize.toInt())
}

internal inline fun <C: Config> Encoder<C>.encode(
    data: ByteArray,
    out: Encoder.OutFeed,
) {
    if (data.isEmpty()) return
    newEncoderFeed(out).use { feed -> data.forEach { b -> feed.consume(b) } }
}
