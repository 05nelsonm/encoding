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

import io.matthewnelson.encoding.core.EncodingException
import io.matthewnelson.encoding.core.EncodingSizeException
import io.matthewnelson.encoding.core.helpers.TestConfig
import io.matthewnelson.encoding.core.util.DecoderInput.Companion.toDecoderInput
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.fail

class DecoderInputUnitTest {

    @Test
    fun givenDecoderInput_whenPaddingPresent_thenFindsTheLastRelevantCharacter() {
        val pad = '='
        val encoded = "1234${pad}${pad}${pad}${pad}"
        val expectedEncodedSize = 4L // just the stuff we care about
        val expectedOutSize = 2L

        val config = TestConfig(paddingByte = pad.byte) { encodedSize ->
            assertEquals(expectedEncodedSize, encodedSize)
            expectedOutSize // return
        }

        assertEquals(expectedOutSize.toInt(), encoded.toDecoderInput(config).decodeOutMaxSize)
    }

    @Test
    fun givenDecoderInput_whenConfigIsLenientFalse_thenThrowsEncodingExceptionWhenSpaceOrNewLine() {
        val valid = "VALID INPUT"

        val config = TestConfig(isLenient = false) { validSize ->
            assertEquals(valid.length.toLong(), validSize)
            1 // return some positive value
        }

        // DecoderInput works from the end, so invalid
        // characters must be at the end somewhere.
        listOf(
            "invalid\n",
            "invalid\t",
            "invalid\r",
            "invalid ",
            valid,
        ).forEach { item ->
            try {
                item.toDecoderInput(config)
                if (item != valid) {
                    fail()
                }
            } catch (t: EncodingSizeException) {
                fail("", t)
            } catch (_: EncodingException) {
                // pass
            }
        }
    }

    @Test
    fun givenDecoderInput_whenConfigIsLenientTrue_thenPassesOverSpaceOrNewLine() {
        val expected = 1L
        val config = TestConfig(isLenient = true) { expected }

        listOf(
            "invalid\n",
            "invalid\t",
            "invalid\r",
            "invalid ",
        ).forEach { item ->
            assertEquals(expected, item.toDecoderInput(config).decodeOutMaxSize.toLong())
        }
    }

    @Test
    fun givenDecoderInput_whenReturnedValueGreaterThanIntMax_thenThrowsEncodingSizeException() {
        val config = TestConfig { Long.MAX_VALUE }

        try {
            "VALID".toDecoderInput(config)
            fail()
        } catch (_: EncodingSizeException) {
            // pass
        }
    }

    @Test
    fun givenDecoderInput_whenInputIsCharSequence_thenSuccess() {
        // Include spaces and set isLenient = true (so they are
        // skipped) in order to exercise DecoderInput.get
        val validInput = "D    " as CharSequence
        val config = TestConfig(isLenient = true) { inputSize ->
            assertEquals(1L, inputSize)
            inputSize// pass
        }

        validInput.toDecoderInput(config)
    }

    @Test
    fun givenDecoderInput_whenInputIsCharArray_thenSuccess() {
        // Include spaces and set isLenient = true (so they are
        // skipped) in order to exercise DecoderInput.get
        val validInput = CharArray(5) { ' ' }.apply { set(0, 'D') }
        val config = TestConfig(isLenient = true) { inputSize ->
            assertEquals(1L, inputSize)
            inputSize
        }

        validInput.toDecoderInput(config)
    }

    @Test
    fun givenDecoderInput_whenInputIsByteArray_thenSuccess() {
        // Include spaces and set isLenient = true (so they are
        // skipped) in order to exercise DecoderInput.get
        val validInput = ByteArray(5) { ' '.byte }.apply { set(0, 'D'.byte) }
        val config = TestConfig(isLenient = true) { inputSize ->
            assertEquals(1L, inputSize)
            inputSize
        }

        validInput.toDecoderInput(config)
    }


}
