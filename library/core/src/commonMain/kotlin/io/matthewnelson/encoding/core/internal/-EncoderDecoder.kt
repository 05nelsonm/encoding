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
@file:Suppress("NOTHING_TO_INLINE")

package io.matthewnelson.encoding.core.internal

import io.matthewnelson.encoding.core.*
import io.matthewnelson.encoding.core.EncoderDecoder.Config
import io.matthewnelson.encoding.core.util.DecoderInput
import kotlin.contracts.ExperimentalContracts
import kotlin.contracts.InvocationKind
import kotlin.contracts.contract
import kotlin.math.min

private const val MAX_ENCODE_OUT_SIZE: Long = Int.MAX_VALUE.toLong()

@Throws(EncodingException::class)
@OptIn(ExperimentalContracts::class)
internal inline fun <C: Config> Decoder<C>.decode(
    input: DecoderInput,
    action: (feed: Decoder<*>.Feed) -> Unit
): ByteArray {
    contract {
        callsInPlace(action, InvocationKind.UNKNOWN)
    }

    val size = config.decodeOutMaxSizeOrFail(input)
    val a = ByteArray(size)

    var i = 0
    try {
        newDecoderFeed(out = { b -> a[i++] = b }).use { feed -> action.invoke(feed) }
    } catch (t: Throwable) {
        a.fill(0, toIndex = min(a.size, i))
        if (t is IndexOutOfBoundsException && i >= size) {
            // Something is wrong with the encoder's pre-calculation
            throw EncodingSizeException("Encoder's pre-calculation of Size[$size] was incorrect", t)
        }
        throw t
    }

    if (i == size) return a

    val copy = a.copyOf(i)
    a.fill(0, i)
    return copy
}

/**
 * Fails if the returned [Long] for [Config.encodeOutSize]
 * exceeds [Int.MAX_VALUE]
 * */
@OptIn(ExperimentalContracts::class)
@Throws(EncodingSizeException::class)
internal inline fun <T: Any> Encoder<*>.encodeOutSizeOrFail(
    size: Int,
    block: (outSize: Int) -> T
): T {
    contract {
        callsInPlace(block, InvocationKind.AT_MOST_ONCE)
    }

    val outSize = config.encodeOutSize(size.toLong())
    if (outSize > MAX_ENCODE_OUT_SIZE) {
        throw Config.outSizeExceedsMaxEncodingSizeException(outSize, MAX_ENCODE_OUT_SIZE)
    }

    return block.invoke(outSize.toInt())
}

internal inline fun <C: Config> Encoder<C>.encode(
    data: ByteArray,
    out: Encoder.OutFeed,
) {
    if (data.isEmpty()) return
    newEncoderFeed(out).use { feed -> data.forEach { b -> feed.consume(b) } }
}

internal inline fun EncoderDecoder.Feed<*>.closedException(): EncodingException {
    return EncodingException("$this is closed")
}

@Suppress("UnusedReceiverParameter")
internal inline fun Config.calculatedOutputNegativeEncodingSizeException(outSize: Number): EncodingSizeException {
    return EncodingSizeException("Calculated output of Size[$outSize] was negative")
}
