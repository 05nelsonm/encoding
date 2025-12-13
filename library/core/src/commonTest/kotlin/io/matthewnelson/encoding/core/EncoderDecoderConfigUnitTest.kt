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
import kotlin.test.assertTrue
import kotlin.test.fail

class EncoderDecoderConfigUnitTest {

    @Test
    fun givenConfig_whenUsingDeprecatedConstructor_thenVersion260ParametersAreNegative1() {
        @Suppress("DEPRECATION")
        val config = object : EncoderDecoder.Config(isLenient = null, lineBreakInterval = 0, paddingChar = null) {
            override fun encodeOutSizeProtected(unEncodedSize: Long): Long = error("")
            override fun decodeOutMaxSizeProtected(encodedSize: Long): Long = error("")
            override fun decodeOutMaxSizeOrFailProtected(encodedSize: Int, input: DecoderInput): Int = error("")
            override fun toStringAddSettings(): Set<Setting> = error("")
        }
        assertEquals(-1, config.maxDecodeEmit)
        assertEquals(-1, config.maxEncodeEmit)
        assertEquals(-1, config.maxEncodeEmitWithLineBreak)
    }

    @Test
    fun givenConfig_whenNegativeValuesSent_thenThrowsEncodingSizeExceptionBeforePassingToProtected() {
        val config = TestConfig(
            encodeOutReturn = { error("Should not make it here") },
            decodeOutInputReturn = { error("Should not make it here") },
            decodeOutReturn = { error("Should not make it here") },
        )

        assertFailsWith<EncodingSizeException> { config.decodeOutMaxSize(-1L) }
        assertFailsWith<EncodingSizeException> { config.encodeOutMaxSize(-1L) }
        assertFailsWith<EncodingSizeException> { config.encodeOutMaxSize(-1) }
    }

    @Test
    fun givenConfig_whenNegativeValuesReturned_thenThrowsEncodingSizeException() {
        val config = TestConfig(
            encodeOutReturn = { -1L },
            decodeOutInputReturn = { -1 },
            decodeOutReturn = { -1L },
            maxDecodeEmit = 255,
            maxEncodeEmit = 255,
        )

        assertFailsWith<EncodingSizeException> { config.decodeOutMaxSize(5) }
        assertFailsWith<EncodingSizeException> { config.decodeOutMaxSizeOrFail(DecoderInput("a")) }
        assertFailsWith<EncodingSizeException> { config.encodeOutMaxSize(5L) }
        assertFailsWith<EncodingSizeException> { config.encodeOutMaxSize(5) }
    }

    @Test
    fun givenConfig_whenPositiveValuesSentAndReturned_thenDoseNotThrowException() {
        val config = TestConfig(
            encodeOutReturn = { 1L },
            decodeOutInputReturn = { 1 },
            decodeOutReturn = { 1L },
        )

        config.decodeOutMaxSizeOrFail(DecoderInput("a"))
        config.decodeOutMaxSize(1L)
        config.encodeOutMaxSize(1L)
        config.encodeOutMaxSize(1)
    }

    @Test
    fun givenConfig_whenZeroPassed_thenReturns0Immediately() {
        val config = TestConfig(
            encodeOutReturn = { fail("Should not make it here") },
            decodeOutInputReturn = { fail("Should not make it here") },
            decodeOutReturn = { fail("Should not make it here") },
        )

        assertEquals(0, config.decodeOutMaxSizeOrFail(DecoderInput("")))
        assertEquals(0L, config.decodeOutMaxSize(0L))
        assertEquals(0, config.encodeOutMaxSize(0))
        assertEquals(0L, config.encodeOutMaxSize(0L))
    }

    @Test
    fun givenConfig_whenZeroReturned_thenDoesNotThrowException() {
        val config = TestConfig(
            encodeOutReturn = { 0L },
            decodeOutInputReturn = { 0 },
            decodeOutReturn = { 0L },
        )

        assertEquals(0, config.decodeOutMaxSizeOrFail(DecoderInput("a")))
        assertEquals(0L, config.decodeOutMaxSize(1L))
        assertEquals(0, config.encodeOutMaxSize(1))
        assertEquals(0L, config.encodeOutMaxSize(1L))
    }

    @Test
    fun givenConfig_whenEncodeOutMaxSizeLongReturnsGreaterThanIntMax_thenEncodeOutMaxSizeIntThrowsEncodingSizeException() {
        val config = TestConfig(
            encodeOutReturn = { Int.MAX_VALUE.toLong() },
        )
        try {
            config.encodeOutMaxSize(1, lineBreakInterval = 64)
            fail()
        } catch (e: EncodingSizeException) {
            assertTrue(e.message.contains("maximum output Size[${Int.MAX_VALUE}]"))
        }
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
    fun givenConfig_whenLineBreakIntervalExpressed_thenCalculatesInflatedSize() {
        val config = TestConfig(isLenient = true, lineBreakInterval = 10, encodeOutReturn = { it })
        listOf(
            Pair(0L, 5L),
            Pair(0L, 10L),
            Pair(1L, 20L),
            Pair(2L, 30L),
            Pair(3L, 40L),
            Pair(399_999_999L, 4_000_000_000L),
            Pair(400_000_000L, 4_000_000_019L),
        ).forEach { (expectedInflation, actual) ->
            val expected = actual + expectedInflation
            assertEquals(expected, config.encodeOutMaxSize(actual))
        }
    }

    @Test
    fun givenLineBreakInterval_whenSizeIncreaseWouldExceedMaxValue_thenThrowsEncodingSizeException() {
        val config = TestConfig(isLenient = true, lineBreakInterval = 10, encodeOutReturn = { it })
        assertFailsWith<EncodingSizeException> { config.encodeOutMaxSize(Long.MAX_VALUE - 10L) }
    }

    @Test
    fun givenConfig_whenLineBreakIntervalZero_thenDoesNotInflateEncodeOutSize() {
        val config = TestConfig(encodeOutReturn = { it })
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

    @Test
    fun givenConfig_whenInvalidMaxEmitArguments_thenThrowsIllegalArgumentException() {
        assertFailsWith<IllegalArgumentException> { TestConfig(maxDecodeEmit = 0) }
        assertFailsWith<IllegalArgumentException> { TestConfig(maxDecodeEmit = -1) }
        assertFailsWith<IllegalArgumentException> { TestConfig(maxDecodeEmit = 256) }
        assertFailsWith<IllegalArgumentException> { TestConfig(maxEncodeEmit = 0) }
        assertFailsWith<IllegalArgumentException> { TestConfig(maxEncodeEmit = -1) }
        assertFailsWith<IllegalArgumentException> { TestConfig(maxEncodeEmit = 256) }
    }

    @Test
    fun givenCalculateMaxEncodeEmit_whenInvalidEmitSize_thenThrowsException() {
        assertFailsWith<IllegalArgumentException> { EncoderDecoder.Config.calculateMaxEncodeEmit(0, 2) }
        assertFailsWith<IllegalArgumentException> { EncoderDecoder.Config.calculateMaxEncodeEmit(-1, 2) }
        assertFailsWith<IllegalArgumentException> { EncoderDecoder.Config.calculateMaxEncodeEmit(256, 2) }
    }

    @Test
    fun givenCalculateMaxEncodeEmit_whenIntervalLessThan1_thenReturnsEmitSize() {
        val expected = 1
        val actual = EncoderDecoder.Config.calculateMaxEncodeEmit(expected, 0)
        assertEquals(expected, actual)
    }

    @Test
    fun givenCalculateMaxEncodeEmit_whenIntervalGreaterOrEqualToThanEmitSize_thenReturnsExpected() {
        val actual = EncoderDecoder.Config.calculateMaxEncodeEmit(1, 20)
        assertEquals(1 + 1, actual)
    }

    @Test
    fun givenCalculateMaxEncodeEmit_whenIntervalIn1ToEmitSize_thenReturnsExpected() {
        var i = 0
        arrayOf(
            Triple(1, 0, 1), // minimum possible value
            Triple(1, 1, 2),
            Triple(2, 1, 4),
            Triple(3, 2, 5),
            Triple(10, 1, 20),
            Triple(10, 2, 15),
            Triple(10, 3, 14),
            Triple(10, 4, 13),
            Triple(10, 5, 12),
            Triple(189, 5, 227),
            Triple(189, 64, 192),
            Triple(255, 1, 510), // maximum possible value
        ).forEach { (size, interval, expected) ->
            val actual = EncoderDecoder.Config.calculateMaxEncodeEmit(size, interval)
            assertEquals(expected, actual, "arguments at index[$i]")
            i++
        }
    }
}
