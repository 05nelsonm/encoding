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
package io.matthewnelson.encoding.core.util

import io.matthewnelson.encoding.core.EncoderDecoder
import io.matthewnelson.encoding.core.EncodingException
import io.matthewnelson.encoding.core.EncodingSizeException
import io.matthewnelson.encoding.core.internal.InternalEncodingApi
import kotlin.jvm.JvmName
import kotlin.jvm.JvmStatic

/**
 * Helper class for analyzing encoded data in order to
 * determine the maximum output size based off of the
 * options set for provided [EncoderDecoder.Config].
 *
 * Will "strip" padding (if the config specifies it) and
 * spaces/new lines from the end in order to provide
 * [EncoderDecoder.Config.decodeOutMaxSizeOrFail]
 * with the best guess of the last relevant character.
 *
 * @see [get]
 * @see [toDecoderInput]
 * @see [EncoderDecoder.Config.decodeOutMaxSizeOrFail]
 * */
@OptIn(InternalEncodingApi::class)
public class DecoderInput
@Throws(EncodingException::class)
private constructor(
    config: EncoderDecoder.Config,
    private val input: Any
) {

    private var _decodeOutMaxSize: Int = 0
    @get:JvmName("decodeOutMaxSize")
    public val decodeOutMaxSize: Int get() = _decodeOutMaxSize

    /**
     * Can be utilized by [EncoderDecoder.Config]
     * implementation to check [input] in order to fail
     * early.
     * */
    @Throws(EncodingException::class)
    public operator fun get(index: Int): Char {
        return try {
            when (input) {
                is String -> input[index]
                is CharArray -> input[index]
                else -> (input as ByteArray)[index].char
            }
        } catch (e: IndexOutOfBoundsException) {
            throw EncodingException("Index out of bounds", e)
        }
    }

    init {
        var limit = when (input) {
            is String -> input.length
            is CharArray -> input.size
            else -> (input as ByteArray).size
        }

        // Disregard any padding or spaces/new lines (if applicable)
        while (limit > 0) {
            val c = get(limit - 1)

            if (c.isSpaceOrNewLine()) {
                if (!config.isLenient) {
                    throw isLenientFalseEncodingException()
                } else {
                    limit--
                    continue
                }
            }

            if (c.byte == config.paddingByte) {
                limit--
                continue
            }

            break
        }

        val outSize = config.decodeOutMaxSizeOrFail(limit.toLong(), this)

        if (outSize > Int.MAX_VALUE.toLong()) {
            throw outSizeExceedsMaxEncodingSizeException(limit, Int.MAX_VALUE)
        }

        _decodeOutMaxSize = outSize.toInt()
    }

    public companion object {

        @JvmStatic
        @InternalEncodingApi // might move this somewhere else... don't use.
        public fun isLenientFalseEncodingException(): EncodingException {
            return EncodingException("Spaces and new lines are forbidden when isLenient[false]")
        }

        @JvmStatic
        @InternalEncodingApi // might move this somewhere else... don't use.
        public fun outSizeExceedsMaxEncodingSizeException(
            inSize: Number,
            maxSize: Number,
        ): EncodingSizeException {
            return EncodingSizeException("Size[$inSize] of data would exceed the maximum[$maxSize] for this operation")
        }

        @JvmStatic
        @Throws(EncodingException::class)
        public fun String.toDecoderInput(
            config: EncoderDecoder.Config
        ): DecoderInput = DecoderInput(config, this)

        @JvmStatic
        @Throws(EncodingException::class)
        public fun CharArray.toDecoderInput(
            config: EncoderDecoder.Config
        ): DecoderInput = DecoderInput(config, this)

        @JvmStatic
        @Throws(EncodingException::class)
        public fun ByteArray.toDecoderInput(
            config: EncoderDecoder.Config
        ): DecoderInput = DecoderInput(config, this)
    }
}
