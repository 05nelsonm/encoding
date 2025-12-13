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
@file:Suppress("RedundantVisibilityModifier")

package io.matthewnelson.encoding.core.helpers

import io.matthewnelson.encoding.core.EncoderDecoder
import io.matthewnelson.encoding.core.util.DecoderInput

class TestConfig public constructor(
    isLenient: Boolean? = false,
    lineBreakInterval: Byte = 0,
    lineBreakResetOnFlush: Boolean = true,
    paddingChar: Char? = '=',
    maxDecodeEmit: Int = 1,
    maxEncodeEmit: Int = 1,
    private val encodeOutReturn: (unEncodedSize: Long) -> Long = { -1L },
    private val decodeOutInputReturn: (encodedSize: Int) -> Int = { -1 },
    private val decodeOutReturn: (encodedSize: Long) -> Long = { -1L },
): EncoderDecoder.Config(
    isLenient,
    lineBreakInterval,
    lineBreakResetOnFlush,
    paddingChar,
    maxDecodeEmit,
    maxEncodeEmit,
    backFillBuffers = true,
) {
    override fun decodeOutMaxSizeProtected(encodedSize: Long): Long {
        return decodeOutReturn.invoke(encodedSize)
    }
    override fun decodeOutMaxSizeOrFailProtected(encodedSize: Int, input: DecoderInput): Int {
        return decodeOutInputReturn.invoke(encodedSize)
    }
    override fun encodeOutSizeProtected(unEncodedSize: Long): Long {
        return encodeOutReturn.invoke(unEncodedSize)
    }
    override fun toStringAddSettings(): Set<Setting> = emptySet()
}
