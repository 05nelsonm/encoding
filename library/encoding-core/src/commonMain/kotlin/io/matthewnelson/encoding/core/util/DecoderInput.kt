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
import io.matthewnelson.encoding.core.internal.ByteChar.Companion.toByteChar
import io.matthewnelson.encoding.core.internal.InternalEncodingApi
import kotlin.jvm.JvmStatic
import kotlin.jvm.JvmSynthetic

/**
 * Helper class for analyzing encoded data in order to
 * determine the maximum output size based off of the
 * options set for provided [EncoderDecoder.Configuration].
 *
 * Will "strip" padding (if the config specifies it) and
 * spaces/new lines from the end in order to provide
 * [EncoderDecoder.Configuration.decodeOutMaxSizeOrFail]
 * with the best guess at input size.
 *
 * @see [get]
 * @see [EncoderDecoder.Configuration.decodeOutMaxSizeOrFail]
 * */
@OptIn(InternalEncodingApi::class)
public class DecoderInput
@Throws(EncodingException::class)
private constructor(
    config: EncoderDecoder.Configuration,
    private val input: Any
) {

    @get:JvmSynthetic
    internal val decodeOutMaxSize: Int

    /**
     * Can be utilized by [EncoderDecoder.Configuration]
     * implementation to check [input] in order to fail
     * early.
     * */
    @Throws(EncodingException::class)
    public fun get(index: Int): Char {
        return try {
            when (input) {
                is String -> input[index]
                is CharArray -> input[index]
                else -> (input as ByteArray)[index].toByteChar().char
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
            when (get(limit - 1)) {
                '\n', '\r', ' ', '\t' -> {
                    if (!config.isLenient) {
                        throw isLenientFalseEncodingException()
                    } else {
                        limit--
                    }
                }
                config.paddingChar -> {
                    limit--
                }
                else -> {
                    break
                }
            }
        }

        val size = config.decodeOutMaxSizeOrFail(limit, this)

        decodeOutMaxSize = if (size < 0) 0 else size
    }

    public companion object {

        @JvmStatic
        @InternalEncodingApi
        public fun isLenientFalseEncodingException(): EncodingException {
            return EncodingException("Spaces and new lines are forbidden when isLenient[false]")
        }

        @JvmSynthetic
        @Throws(EncodingException::class)
        internal fun String.toInputAnalysis(
            config: EncoderDecoder.Configuration
        ): DecoderInput = DecoderInput(config, this)

        @JvmSynthetic
        @Throws(EncodingException::class)
        internal fun CharArray.toInputAnalysis(
            config: EncoderDecoder.Configuration
        ): DecoderInput = DecoderInput(config, this)

        @JvmSynthetic
        @Throws(EncodingException::class)
        internal fun ByteArray.toInputAnalysis(
            config: EncoderDecoder.Configuration
        ): DecoderInput = DecoderInput(config, this)
    }
}
