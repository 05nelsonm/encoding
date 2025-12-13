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
import io.matthewnelson.encoding.core.Decoder.Companion.decodeBufferedAsync
import io.matthewnelson.encoding.core.Decoder.Companion.decodeToByteArray
import io.matthewnelson.encoding.core.Decoder.Companion.decodeToByteArrayOrNull
import io.matthewnelson.encoding.core.helpers.TestConfig
import io.matthewnelson.encoding.core.helpers.TestEncoderDecoder
import kotlinx.coroutines.test.runTest
import kotlin.test.Test
import kotlin.test.assertContentEquals
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith
import kotlin.test.assertNotEquals

class PartialDecodingUnitTest {

    @Test
    fun givenDecode_whenPartialDecoding_thenChecksBounds() {
        val decoder = TestEncoderDecoder(TestConfig())
        assertFailsWith<IndexOutOfBoundsException> {
            "a".decodeToByteArray(decoder, -1, 1)
        }
        assertFailsWith<IndexOutOfBoundsException> {
            charArrayOf('a').decodeToByteArray(decoder, -1, 1)
        }
        assertFailsWith<IndexOutOfBoundsException> {
            "a".decodeToByteArrayOrNull(decoder, -1, 1)
        }
        assertFailsWith<IndexOutOfBoundsException> {
            charArrayOf('a').decodeToByteArrayOrNull(decoder, -1, 1)
        }
    }

    @Test
    fun givenDecodeBuffered_whenPartialDecoding_thenChecksBounds() = runTest {
        val decoder = TestEncoderDecoder(TestConfig())
        assertFailsWith<IndexOutOfBoundsException> {
            "a".decodeBuffered(
                decoder,
                true,
                offset = -1,
                len = 1,
                action = { _, _, _ -> error("Should not make it here") }
            )
        }
        assertFailsWith<IndexOutOfBoundsException> {
            charArrayOf('a').decodeBuffered(
                decoder,
                true,
                offset = -1,
                len = 1,
                action = { _, _, _ -> error("Should not make it here") }
            )
        }
        assertFailsWith<IndexOutOfBoundsException> {
            "a".decodeBuffered(
                decoder,
                true,
                offset = -1,
                len = 1,
                maxBufSize = 25,
                action = { _, _, _ -> error("Should not make it here") }
            )
        }
        assertFailsWith<IndexOutOfBoundsException> {
            charArrayOf('a').decodeBuffered(
                decoder,
                true,
                offset = -1,
                len = 1,
                maxBufSize = 25,
                action = { _, _, _ -> error("Should not make it here") }
            )
        }
        assertFailsWith<IndexOutOfBoundsException> {
            "a".decodeBuffered(
                decoder,
                true,
                offset = -1,
                len = 1,
                buf = ByteArray(25),
                action = { _, _, _ -> error("Should not make it here") }
            )
        }
        assertFailsWith<IndexOutOfBoundsException> {
            charArrayOf('a').decodeBuffered(
                decoder,
                true,
                offset = -1,
                len = 1,
                buf = ByteArray(25),
                action = { _, _, _ -> error("Should not make it here") }
            )
        }
        assertFailsWith<IndexOutOfBoundsException> {
            "a".decodeBufferedAsync(
                decoder,
                true,
                offset = -1,
                len = 1,
                action = { _, _, _ -> error("Should not make it here") }
            )
        }
        assertFailsWith<IndexOutOfBoundsException> {
            charArrayOf('a').decodeBufferedAsync(
                decoder,
                true,
                offset = -1,
                len = 1,
                action = { _, _, _ -> error("Should not make it here") }
            )
        }
        assertFailsWith<IndexOutOfBoundsException> {
            "a".decodeBufferedAsync(
                decoder,
                true,
                offset = -1,
                len = 1,
                maxBufSize = 25,
                action = { _, _, _ -> error("Should not make it here") }
            )
        }
        assertFailsWith<IndexOutOfBoundsException> {
            charArrayOf('a').decodeBufferedAsync(
                decoder,
                true,
                offset = -1,
                len = 1,
                maxBufSize = 25,
                action = { _, _, _ -> error("Should not make it here") }
            )
        }
        assertFailsWith<IndexOutOfBoundsException> {
            "a".decodeBufferedAsync(
                decoder,
                true,
                offset = -1,
                len = 1,
                buf = ByteArray(25),
                action = { _, _, _ -> error("Should not make it here") }
            )
        }
        assertFailsWith<IndexOutOfBoundsException> {
            charArrayOf('a').decodeBufferedAsync(
                decoder,
                true,
                offset = -1,
                len = 1,
                buf = ByteArray(25),
                action = { _, _, _ -> error("Should not make it here") }
            )
        }
    }

    @Test
    fun givenDecode_whenPartialDecoding_thenConsumesCorrectIndices() {
        val expected = 4
        var invocationDecodeOut = 0
        var invocationDecoderConsume = 0
        val decoder = TestEncoderDecoder(
            config = TestConfig(
                decodeOutInputReturn = { size ->
                    invocationDecodeOut++
                    assertEquals(expected, size)
                    size
                },
            ),
            decoderConsume = { c ->
                invocationDecoderConsume++
                assertEquals(expected.toChar(), c)
                output(expected.toByte())
            },
            decoderDoFinal = {},
        )

        val data = charArrayOf('\u0000', expected.toChar(), expected.toChar(), expected.toChar(), expected.toChar(), '\u0000')

        // Exercises -Decoder.kt decode & decodeToUnsafe
        assertContentEquals(
            byteArrayOf(expected.toByte(), expected.toByte(), expected.toByte(), expected.toByte()),
            data.decodeToByteArray(decoder, 1, expected)
        )
        assertEquals(1, invocationDecodeOut)
        assertEquals(expected, invocationDecoderConsume)

        // Exercises -Decoder.kt decodeBuffered & decodeToUnsafe
        var invocationAction = 0
        // single shot decode
        var result = data.decodeBuffered(
            decoder,
            throwOnOverflow = true,
            offset = 1,
            len = expected,
            action = { buf, offset, len ->
                invocationAction++
                assertEquals(expected, len)
                assertEquals(0, offset)
                repeat(len) { i -> assertEquals(expected.toByte(), buf[i]) }
            }
        )
        assertEquals(expected.toLong(), result)
        assertEquals(1, invocationAction)
        assertEquals(2, invocationDecodeOut)
        assertEquals(expected * 2, invocationDecoderConsume)
        invocationAction = 0 // reset

        // stream to buffer & flush
        result = data.decodeBuffered(
            decoder,
            throwOnOverflow = true,
            offset = 1,
            len = expected,
            maxBufSize = 3,
            action = { buf, offset, len ->
                invocationAction++
                assertEquals(0, offset)
                assertNotEquals(0, len)
                repeat(len) { i -> assertEquals(expected.toByte(), buf[i]) }
            }
        )
        assertEquals(expected.toLong(), result)
        assertEquals(2, invocationAction)
        assertEquals(3, invocationDecodeOut)
        assertEquals(expected * 3, invocationDecoderConsume)
    }
}
