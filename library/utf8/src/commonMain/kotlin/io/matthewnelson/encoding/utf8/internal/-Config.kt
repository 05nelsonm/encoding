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
@file:Suppress("LocalVariableName", "NOTHING_TO_INLINE", "RedundantCompanionReference")

package io.matthewnelson.encoding.utf8.internal

import io.matthewnelson.encoding.core.EncoderDecoder.Config.Companion.outSizeExceedsMaxEncodingSizeException
import io.matthewnelson.encoding.core.EncodingException
import io.matthewnelson.encoding.utf8.UTF8
import kotlin.contracts.ExperimentalContracts
import kotlin.contracts.InvocationKind
import kotlin.contracts.contract

internal inline fun ((UTF8.ReplacementStrategy, Boolean) -> UTF8.Config).build(
    b: UTF8.Builder,
    noinline utf8: (UTF8.Config) -> UTF8,
): UTF8 {
    if (
        b._backFillBuffers == UTF8.Default.config.backFillBuffers
    ) {
        if (b._replacementStrategy == UTF8.Default.config.replacementStrategy) return UTF8.Default
        if (b._replacementStrategy == UTF8.ThrowOnInvalid.config.replacementStrategy) return UTF8.ThrowOnInvalid
    }
    val config = this(
        b._replacementStrategy,
        b._backFillBuffers,
    )
    return utf8(config)
}

private const val MAX_ENCODED_SIZE_FAST_32: Int = Int.MAX_VALUE / 3
private const val MAX_ENCODED_SIZE_SLOW_32: Long = Int.MAX_VALUE.toLong()

@Throws(EncodingException::class)
@OptIn(ExperimentalContracts::class)
internal inline fun UTF8.Config.decodeOutMaxSize32(
    encodedSize: Int,
    useCharPreProcessorIfNeeded: Boolean,
    _get: (i: Int) -> Char,
): Int {
    contract { callsInPlace(_get, InvocationKind.UNKNOWN) }
    if (encodedSize > MAX_ENCODED_SIZE_FAST_32) {
        if (!useCharPreProcessorIfNeeded) throw outSizeExceedsMaxEncodingSizeException(encodedSize, Int.MAX_VALUE)
        // Instead of failing immediately, the entire input could be ASCII
        // characters or something. Try to calculate its exact size.
        return decodeOutMaxSizeSlow32(encodedSize, _get)
    }
    // Fast path
    return encodedSize * 3
}

@Throws(EncodingException::class)
@OptIn(ExperimentalContracts::class)
internal inline fun UTF8.Config.decodeOutMaxSizeSlow32(
    encodedSize: Int,
    _get: (i: Int) -> Char,
): Int {
    contract { callsInPlace(_get, InvocationKind.UNKNOWN) }
    var i = 0
    val cpp = UTF8.CharPreProcessor.of(replacementStrategy)
    while (i < encodedSize && cpp.currentSize <= MAX_ENCODED_SIZE_SLOW_32) {
        cpp + _get(i++)
    }
    if (cpp.currentSize <= MAX_ENCODED_SIZE_SLOW_32) {
        val size = cpp.doFinal()
        if (size <= MAX_ENCODED_SIZE_SLOW_32) return size.toInt()
    }

    throw outSizeExceedsMaxEncodingSizeException(encodedSize, Int.MAX_VALUE)
}
