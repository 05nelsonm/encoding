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
import io.matthewnelson.encoding.core.Encoder.Companion.encodeBuffered
import io.matthewnelson.encoding.core.EncoderDecoder.Companion.DEFAULT_BUFFER_SIZE
import io.matthewnelson.encoding.core.helpers.TestConfig
import io.matthewnelson.encoding.core.helpers.TestEncoderDecoder
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith

class EncodeBufferedUnitTest {

    @Test
    fun givenBufferSize_whenLessThanOrEqualToConfigMaxEncodeEmitWithLineBreak_thenThrowsIllegalArgumentException() {
        val encoder = TestEncoderDecoder(TestConfig(
            maxEncodeEmit = 255,
            decodeOutInputReturn = { error("Should not make it here") },
        ))

        intArrayOf(254, 255).forEach { testSize ->
            assertFailsWith<IllegalArgumentException> {
                ByteArray(1).encodeBuffered(
                    encoder,
                    throwOnOverflow = true,
                    maxBufSize = testSize,
                    action = { _, _, _ -> error("Should not make it here") },
                )
            }

            assertFailsWith<IllegalArgumentException> {
                ByteArray(1).encodeBuffered(
                    encoder,
                    throwOnOverflow = true,
                    buf = CharArray(testSize),
                    action = { _, _, _ -> error("Should not make it here") },
                )
            }
        }
    }

    @Test
    fun givenEncodeOutMaxSizeException_whenThrowOnOverflowTrue_thenEncodingSizeExceptionIsRethrown() {
        val encoder = TestEncoderDecoder(TestConfig())
        assertFailsWith<EncodingSizeException> {
            ByteArray(1).encodeBuffered(
                encoder,
                throwOnOverflow = true,
                action = { _, _, _ -> error("Should not make it here") },
            )
        }
    }

    @Test
    fun givenEncodeOutMaxSizeException_whenThrowOnOverflowFalse_thenEncodingSizeExceptionIsIgnored() {
        var invocationEncodeOut = 0
        val encoder = TestEncoderDecoder(TestConfig(
            encodeOutReturn = { invocationEncodeOut++; -1 },
        ))

        var invocationAction = 0
        ByteArray(DEFAULT_BUFFER_SIZE + 50).encodeBuffered(
            encoder,
            throwOnOverflow = false,
            action = { buf, _, _ ->
                invocationAction++
                assertEquals(DEFAULT_BUFFER_SIZE, buf.size)
            },
        )
        assertEquals(1, invocationEncodeOut)
        // Confirms that it went into stream decoding b/c was flushed 2 times
        assertEquals(2, invocationAction)
    }

    @Test
    fun givenInputSize_whenLessThanBufferSize_thenOneShotEncodesWithSmallerSize() {
        val expectedSize = 2
        val expectedInputSize = DEFAULT_BUFFER_SIZE + 50
        var invocationConsume = 0
        val encoder = TestEncoderDecoder(
            config = TestConfig(
                encodeOutReturn = { expectedSize.toLong() },
            ),
            encoderConsume = { invocationConsume++ },
            encoderDoFinal = { (it as TestEncoderDecoder.EncoderFeed).getOut().output(Char.MAX_VALUE) },
        )

        var invocationAction = 0
        val result = ByteArray(expectedInputSize).encodeBuffered(
            encoder,
            throwOnOverflow = true,
            maxBufSize = expectedSize * 10,
            action = { buf, _, len ->
                invocationAction++
                assertEquals(expectedSize, buf.size)
                assertEquals(1, len)
            },
        )
        assertEquals(1, invocationAction)
        assertEquals(invocationConsume, expectedInputSize)
        assertEquals(1L, result)
    }

    @Test
    fun givenConfigMaxEncodeEmitWithLineBreak_whenEncodingBuffered_thenFlushesIfLastIndexWouldBeExceeded() {
        val encoder = TestEncoderDecoder(TestConfig(maxEncodeEmit = 50))

        var invocationAction = 0
        var invocationActionAssertion = 0
        var actualLen = 0
        val result = ByteArray(DEFAULT_BUFFER_SIZE).encodeBuffered(
            encoder,
            throwOnOverflow = false,
            maxBufSize = DEFAULT_BUFFER_SIZE,
            action = { buf, _, len ->
                invocationAction++
                actualLen += len
                assertEquals(DEFAULT_BUFFER_SIZE, buf.size)
                if (invocationAction == 1) {
                    invocationActionAssertion++
                    assertEquals(DEFAULT_BUFFER_SIZE - encoder.config.maxEncodeEmitWithLineBreak + 1, len, "invocationAction[$invocationAction]")
                }
                if (invocationAction == 2) {
                    invocationActionAssertion++
                    assertEquals(encoder.config.maxEncodeEmitWithLineBreak, len)
                }
            },
        )
        assertEquals(2, invocationAction)
        assertEquals(2, invocationActionAssertion)
        // TestEncoderDecoder.doFinal default value will output one 1 byte
        assertEquals((DEFAULT_BUFFER_SIZE + 1).toLong(), result)
        assertEquals(result, actualLen.toLong())
    }
}
