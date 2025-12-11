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
package io.matthewnelson.encoding.core

import io.matthewnelson.encoding.core.helpers.TestConfig
import io.matthewnelson.encoding.core.util.DecoderInput
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith
import kotlin.test.fail

class EncoderDecoderConfigUnitTest {

    @Test
    fun givenConfig_whenNegativeValuesSent_thenThrowsEncodingSizeExceptionBeforePassingToProtected() {
        val config = TestConfig(
            encodeReturn = {
                fail("Should not make it here")
            },
            decodeInputReturn = {
                fail("Should not make it here")
            },
            decodeReturn = {
                fail("Should not make it here")
            }
        )

        try {
            config.decodeOutMaxSize(-1L)
            fail()
        } catch (_: EncodingSizeException) {
            // pass
        }

        try {
            config.encodeOutMaxSize(-1L)
            fail()
        } catch (_: EncodingSizeException) {
            // pass
        }
    }

    @Test
    fun givenConfig_whenNegativeValuesReturned_thenThrowsEncodingSizeException() {
        val config = TestConfig(
            encodeReturn = { -1L },
            decodeInputReturn = { -1 },
            decodeReturn = { -1L },
            maxDecodeEmit = 255,
            maxEncodeEmit = 255,
        )

        assertFailsWith<EncodingSizeException> { config.decodeOutMaxSize(5) }
        assertFailsWith<EncodingSizeException> { config.decodeOutMaxSizeOrFail(DecoderInput("a")) }
        assertFailsWith<EncodingSizeException> { config.encodeOutMaxSize(5) }
        assertFailsWith<IllegalArgumentException> { TestConfig(maxDecodeEmit = 0) }
        assertFailsWith<IllegalArgumentException> { TestConfig(maxDecodeEmit = -1) }
        assertFailsWith<IllegalArgumentException> { TestConfig(maxDecodeEmit = 256) }
        assertFailsWith<IllegalArgumentException> { TestConfig(maxEncodeEmit = 0) }
        assertFailsWith<IllegalArgumentException> { TestConfig(maxEncodeEmit = -1) }
        assertFailsWith<IllegalArgumentException> { TestConfig(maxEncodeEmit = 256) }
    }

    @Test
    fun givenConfig_whenPositiveValuesSentAndReturned_thenDoseNotThrowException() {
        val config = TestConfig(
            encodeReturn = { 1L },
            decodeInputReturn = { 1 },
            decodeReturn = { 1L },
        )

        config.decodeOutMaxSizeOrFail(DecoderInput("a"))
        config.decodeOutMaxSize(1L)
        config.encodeOutMaxSize(1L)
    }

    @Test
    fun givenConfig_whenZeroPassed_thenReturns0Immediately() {
        val config = TestConfig(
            encodeReturn = { fail("Should not make it here") },
            decodeInputReturn = { fail("Should not make it here") },
            decodeReturn = { fail("Should not make it here") },
        )

        config.decodeOutMaxSizeOrFail(DecoderInput(""))
        config.decodeOutMaxSize(0L)
        config.encodeOutMaxSize(0L)
    }

    @Test
    fun givenConfig_whenZeroReturned_thenDoesNotThrowException() {
        val config = TestConfig(
            encodeReturn = { 0L },
            decodeInputReturn = { 0 },
            decodeReturn = { 0L },
        )

        config.decodeOutMaxSizeOrFail(DecoderInput("a"))
        config.decodeOutMaxSize(1L)
        config.encodeOutMaxSize(1L)
    }

    @Test
    fun givenLineBreakInterval_whenIsLenientFalse_thenIsZero() {
        val config = TestConfig(isLenient = false, lineBreakInterval = 20)

        assertEquals(0, config.lineBreakInterval)
    }

    @Test
    fun givenLineBreakInterval_whenIsLenientTrue_thenIsExpected() {
        val expected: Byte = 20
        val config = TestConfig(isLenient = true, lineBreakInterval = expected)

        assertEquals(expected, config.lineBreakInterval)
    }

    @Test
    fun givenLineBreakInterval_whenIsLenientNull_thenIsExpected() {
        val expected: Byte = 20
        val config = TestConfig(isLenient = null, lineBreakInterval = expected)

        assertEquals(expected, config.lineBreakInterval)
    }

    @Test
    fun givenConfig_whenLineBreakIntervalNegative_thenIsZero() {
        val config = TestConfig(isLenient = true, lineBreakInterval = -5)

        assertEquals(0, config.lineBreakInterval)
    }

    @Test
    fun givenConfig_whenLineBreakIntervalExpressed_thenIncreasesEncodeOutSize() {
        val config = TestConfig(isLenient = true, lineBreakInterval = 10, encodeReturn = { it })
        listOf(
            Pair(5L, 5L),
            Pair(10L, 10L),
            Pair(21L, 20L),
            Pair(32L, 30L),
            Pair(43L, 40L),
        ).forEach { (expected, actual) ->
            assertEquals(expected, config.encodeOutMaxSize(actual))
        }
    }

    @Test
    fun givenLineBreakInterval_whenSizeIncreaseWouldExceedMaxValue_thenThrowsEncodingSizeException() {
        val config = TestConfig(isLenient = true, lineBreakInterval = 10, encodeReturn = { it })

        try {
            config.encodeOutMaxSize(Long.MAX_VALUE - 10L)
            fail()
        } catch (_: EncodingSizeException) {
            // pass
        }
    }

    @Test
    fun givenConfig_whenLineBreakIntervalZero_thenDoesNotAffectEncodeOutSize() {
        val config = TestConfig(encodeReturn = { it })
        assertEquals(0, config.lineBreakInterval)

        listOf(
            5L,
            10L,
            20L,
            30L,
            40L,
        ).forEach { size ->
            assertEquals(size, config.encodeOutMaxSize(size))
        }
    }

}
