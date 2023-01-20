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
@file:Suppress("KotlinRedundantDiagnosticSuppress")

package io.matthewnelson.encoding.core.internal

import io.matthewnelson.encoding.core.*
import io.matthewnelson.encoding.core.EncoderDecoder.Config
import io.matthewnelson.encoding.core.util.DecoderInput

@Suppress("NOTHING_TO_INLINE")
@Throws(EncodingException::class)
@OptIn(ExperimentalEncodingApi::class)
internal inline fun Decoder.decode(
    input: DecoderInput,
    update: (feed: Decoder.Feed) -> Unit
): ByteArray {
    val ba = ByteArray(input.decodeOutMaxSize)

    var i = 0
    newDecoderFeed { byte ->
        try {
            ba[i++] = byte
        } catch (e: IndexOutOfBoundsException) {
            // Something is wrong with the encoder's pre-calculation
            throw EncodingSizeException("Encoder's pre-calculation of Size[${input.decodeOutMaxSize}] was incorrect", e)
        }
    }.use { feed ->
        update.invoke(feed)
    }

    return if (i == input.decodeOutMaxSize) {
        ba
    } else {
        ba.copyOf(i)
    }
}

/**
 * Fails if the returned [Long] for [Config.encodeOutSize]
 * exceeds [Int.MAX_VALUE]
 * */
@Throws(EncodingSizeException::class)
internal inline fun <T: Any> Encoder.encodeOutSizeOrFail(
    size: Int, block: (outSize: Int) -> T
): T {
    val outSize = config.encodeOutSize(size.toLong())
    if (outSize > Int.MAX_VALUE.toLong()) {
        @OptIn(InternalEncodingApi::class)
        throw DecoderInput.outSizeExceedsMaxEncodingSizeException(outSize, Int.MAX_VALUE)
    }

    return block.invoke(outSize.toInt())
}

@Suppress("NOTHING_TO_INLINE")
@OptIn(ExperimentalEncodingApi::class)
internal inline fun Encoder.encode(
    bytes: ByteArray,
    out: OutFeed,
) {
    if (bytes.isEmpty()) return

    newEncoderFeed(out).use { feed ->
        for (byte in bytes) {
            feed.update(byte)
        }
    }
}
