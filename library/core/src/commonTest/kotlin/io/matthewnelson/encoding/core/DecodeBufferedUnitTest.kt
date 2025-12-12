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
package io.matthewnelson.encoding.core

import io.matthewnelson.encoding.core.Decoder.Companion.decodeBuffered
import io.matthewnelson.encoding.core.EncoderDecoder.Companion.DEFAULT_BUFFER_SIZE
import io.matthewnelson.encoding.core.helpers.TestConfig
import io.matthewnelson.encoding.core.helpers.TestEncoderDecoder
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith
import kotlin.test.fail

class DecodeBufferedUnitTest {

    @Test
    fun givenBufferSize_whenLessThanOrEqualToConfigMaxDecodeEmit_thenThrowsIllegalArgumentException() {
        val decoder = TestEncoderDecoder(TestConfig(
            maxDecodeEmit = 255,
            decodeInputReturn = { error("Should not make it here") }
        ))

        intArrayOf(254, 255).forEach { testSize ->
            assertFailsWith<IllegalArgumentException> {
                "a".decodeBuffered(
                    decoder,
                    throwOnOverflow = true,
                    maxBufSize = testSize,
                    action = { _, _, _ -> error("Should not make it here") },
                )
            }

            assertFailsWith<IllegalArgumentException> {
                "a".decodeBuffered(
                    decoder,
                    throwOnOverflow = true,
                    buf = ByteArray(testSize),
                    action = { _, _, _ -> error("Should not make it here") },
                )
            }
        }
    }

    @Test
    fun givenDecodeInputNonSizeException_whenConfigThrows_thenIsNeverIgnored() {
        val expected = "Config implementation has some sort of checksum at end of encoding and it did not pass"
        val decoder = TestEncoderDecoder(TestConfig(
            decodeInputReturn = { throw EncodingException(expected) }
        ))
        try {
            "a".decodeBuffered(decoder, throwOnOverflow = true) { _, _, _ -> error("Should not make it here") }
            fail()
        } catch (e: EncodingException) {
            assertEquals(expected, e.message)
        }
        try {
            "a".decodeBuffered(decoder, throwOnOverflow = false) { _, _, _ -> error("Should not make it here") }
            fail()
        } catch (e: EncodingException) {
            assertEquals(expected, e.message)
        }
    }

    @Test
    fun givenDecodeInputSizeException_whenThrowOnOverflowTrue_thenEncodingSizeExceptionIsRethrown() {
        val decoder = TestEncoderDecoder(TestConfig())
        assertFailsWith<EncodingSizeException> {
            "a".decodeBuffered(
                decoder,
                throwOnOverflow = true,
                action = { _, _, _ -> error("Should not make it here") }
            )
        }
    }

    @Test
    fun givenDecodeInputSizeException_whenThrowOnOverflowFalse_thenEncodingSizeExceptionIsIgnored() {
        var invocationInput = 0
        val decoder = TestEncoderDecoder(TestConfig(
            decodeInputReturn = { invocationInput++; -1 },
        ))

        var invocationAction = 0
        object : CharSequence {
            override val length: Int = DEFAULT_BUFFER_SIZE + 50
            override fun get(index: Int): Char = 'a'
            override fun subSequence(startIndex: Int, endIndex: Int): CharSequence { error("unused") }
        }.decodeBuffered(
            decoder,
            throwOnOverflow = false,
            action = { buf, _, _ ->
                invocationAction++
                assertEquals(DEFAULT_BUFFER_SIZE, buf.size)
            }
        )
        assertEquals(1, invocationInput)
        // Confirms that it went into stream decoding b/c was flushed 2 times
        assertEquals(2, invocationAction)
    }

    @Test
    fun givenDecodeInputSize_whenLessThanBufferSize_thenOneShotDecodesWithSmallerSize() {
        val expectedSize = 2
        val expectedInputSize = DEFAULT_BUFFER_SIZE + 50
        var invocationConsume = 0
        val decoder = TestEncoderDecoder(
            config = TestConfig(
                decodeInputReturn = { expectedSize },
            ),
            decoderConsume = { invocationConsume++ },
            decoderDoFinal = { (it as TestEncoderDecoder.DecoderFeed).getOut().output(1) },
        )

        var invocationAction = 0
        val result = object : CharSequence {
            override val length: Int = expectedInputSize
            override fun get(index: Int): Char = 'a'
            override fun subSequence(startIndex: Int, endIndex: Int): CharSequence = error("unused")
        }.decodeBuffered(
            decoder,
            throwOnOverflow = true,
            maxBufSize = expectedSize * 10,
            action = { buf, _, len ->
                invocationAction++
                assertEquals(expectedSize, buf.size)
                assertEquals(1, len)
            }
        )
        assertEquals(1, invocationAction)
        assertEquals(invocationConsume, expectedInputSize)
        assertEquals(1L, result)
    }

    @Test
    fun givenConfigMaxDecodeEmit_whenDecodingBuffered_thenFlushesIfLastIndexWouldBeExceeded() {
        val decoder = TestEncoderDecoder(TestConfig(maxDecodeEmit = 50))

        var invocationAction = 0
        var invocationActionAssertion = 0
        var actualLen = 0
        val result = object : CharSequence {
            override val length: Int = DEFAULT_BUFFER_SIZE
            override fun get(index: Int): Char = 'a'
            override fun subSequence(startIndex: Int, endIndex: Int): CharSequence = error("unused")
        }.decodeBuffered(
            decoder,
            throwOnOverflow = false,
            maxBufSize = DEFAULT_BUFFER_SIZE,
            action = { buf, _, len ->
                invocationAction++
                actualLen += len
                assertEquals(DEFAULT_BUFFER_SIZE, buf.size)
                if (invocationAction == 1) {
                    invocationActionAssertion++
                    assertEquals(DEFAULT_BUFFER_SIZE - decoder.config.maxDecodeEmit + 1, len, "invocationAction[$invocationAction]")
                }
                if (invocationAction == 2) {
                    invocationActionAssertion++
                    assertEquals(decoder.config.maxDecodeEmit, len)
                }
            }
        )
        assertEquals(2, invocationAction)
        assertEquals(2, invocationActionAssertion)
        // TestEncoderDecoder.doFinal default value will output one 1 byte
        assertEquals((DEFAULT_BUFFER_SIZE + 1).toLong(), result)
        assertEquals(result, actualLen.toLong())
    }
}
