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
@file:Suppress("UNUSED")

package io.matthewnelson.encoding.core.util

import io.matthewnelson.encoding.core.Decoder
import io.matthewnelson.encoding.core.EncoderDecoder
import io.matthewnelson.encoding.core.EncodingException
import kotlin.jvm.JvmSynthetic

/**
 * Helper class that ensures there is a common input type for
 * [EncoderDecoder.Config.decodeOutMaxSizeOrFail] such that changes
 * to the API (like adding support for a new type in [Decoder]
 * extension functions) will not affect inheritors of [EncoderDecoder].
 *
 * @see [get]
 * @see [EncoderDecoder.Config.decodeOutMaxSizeOrFail]
 * */
public class DecoderInput {

    @get:JvmSynthetic
    internal val size: Int
    private val _get: (Int) -> Char

    private constructor(size: Int, get: (Int) -> Char) { this.size = size; this._get = get }
    public constructor(input: CharSequence): this(input.length, get = input::get)
    public constructor(input: CharArray): this(input.size, get = input::get)

    @Throws(EncodingException::class)
    public operator fun get(index: Int): Char {
        try {
            return _get(index)
        } catch (e: IndexOutOfBoundsException) {
            throw EncodingException("Index out of bounds", e)
        }
    }

    /**
     * DEPRECATED since `2.3.0`
     * @suppress
     * */
    @Deprecated(
        message = "Should not utilize. Underlying Byte to Char conversion can produce incorrect results",
        level = DeprecationLevel.ERROR,
    )
    public constructor(input: ByteArray): this(input.size, get = { i -> input[i].toInt().toChar() })
}
