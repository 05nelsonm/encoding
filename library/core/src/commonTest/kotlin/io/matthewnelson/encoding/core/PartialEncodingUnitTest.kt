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

import io.matthewnelson.encoding.core.Encoder.Companion.encodeBuffered
import io.matthewnelson.encoding.core.Encoder.Companion.encodeToCharArray
import io.matthewnelson.encoding.core.Encoder.Companion.encodeToString
import io.matthewnelson.encoding.core.helpers.TestConfig
import io.matthewnelson.encoding.core.helpers.TestEncoderDecoder
import kotlin.test.Test
import kotlin.test.assertContentEquals
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith
import kotlin.test.assertNotEquals

class PartialEncodingUnitTest {

    @Test
    fun givenEncode_whenPartialEncoding_thenChecksBounds() {
        val encoder = TestEncoderDecoder(TestConfig())
        assertFailsWith<IndexOutOfBoundsException> {
            ByteArray(1).encodeToString(encoder, -1, 1)
        }
        assertFailsWith<IndexOutOfBoundsException> {
            ByteArray(1).encodeToCharArray(encoder, -1, 1)
        }
    }

    @Test
    fun givenEncodeBuffered_whenPartialEncoding_thenChecksBounds() {
        val encoder = TestEncoderDecoder(TestConfig())
        assertFailsWith<IndexOutOfBoundsException> {
            ByteArray(1).encodeBuffered(
                encoder,
                true,
                offset = -1,
                len = 1,
                maxBufSize = 25,
                action = { _, _, _ -> error("Should not make it here") }
            )
        }
        assertFailsWith<IndexOutOfBoundsException> {
            ByteArray(1).encodeBuffered(
                encoder,
                true,
                offset = -1,
                len = 1,
                buf = CharArray(25),
                action = { _, _, _ -> error("Should not make it here") }
            )
        }
    }

    @Test
    fun givenEncode_whenPartialEncoding_thenConsumesCorrectIndices() {
        val expected = 4
        var invocationEncodeOut = 0
        var invocationEncoderConsume = 0
        val encoder = TestEncoderDecoder(
            config = TestConfig(
                encodeOutReturn = { size ->
                    invocationEncodeOut++
                    assertEquals(expected.toLong(), size)
                    size
                },
            ),
            encoderConsume = { b ->
                invocationEncoderConsume++
                assertEquals(expected.toByte(), b)
                output('b')
            },
            encoderDoFinal = {},
        )

        val data = byteArrayOf(0, expected.toByte(), expected.toByte(), expected.toByte(), expected.toByte(), 0)
        assertEquals("bbbb", data.encodeToString(encoder, 1, expected))
        assertEquals(1, invocationEncodeOut)
        assertEquals(expected, invocationEncoderConsume)

        assertContentEquals(charArrayOf('b', 'b', 'b', 'b'), data.encodeToCharArray(encoder, 1, expected))
        assertEquals(2, invocationEncodeOut)
        assertEquals(expected * 2, invocationEncoderConsume)

        var invocationAction = 0
        // single shot encode
        var result = data.encodeBuffered(
            encoder,
            throwOnOverflow = true,
            offset = 1,
            len = expected,
            action = { buf, offset, len ->
                invocationAction++
                assertEquals(expected, len)
                assertEquals(0, offset)
                repeat(len) { i -> assertEquals('b', buf[i]) }
            }
        )
        assertEquals(expected.toLong(), result)
        assertEquals(1, invocationAction)
        assertEquals(3, invocationEncodeOut)
        assertEquals(expected * 3, invocationEncoderConsume)
        invocationAction = 0 // reset

        // stream to buffer & flush
        result = data.encodeBuffered(
            encoder,
            throwOnOverflow = true,
            offset = 1,
            len = expected,
            maxBufSize = 3,
            action = { buf, offset, len ->
                invocationAction++
                assertEquals(0, offset)
                assertNotEquals(0, len)
                repeat(len) { i -> assertEquals('b', buf[i]) }
            }
        )
        assertEquals(expected.toLong(), result)
        assertEquals(2, invocationAction)
        assertEquals(4, invocationEncodeOut)
        assertEquals(expected * 4, invocationEncoderConsume)
    }
}
