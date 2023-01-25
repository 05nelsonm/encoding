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
import io.matthewnelson.encoding.core.helpers.TestEncoderDecoder
import kotlin.test.*

@OptIn(ExperimentalEncodingApi::class)
class EncoderDecoderFeedUnitTest {

    @Test
    fun givenFeed_whenDoFinalIsCalled_thenFeedCloses() {
        val feed = TestEncoderDecoder(TestConfig(isLenient = true)).newDecoderFeed { decoded ->
            assertEquals(Byte.MIN_VALUE, decoded)
        }

        assertFalse(feed.isClosed())
        feed.doFinal()
        assertTrue(feed.isClosed())
    }

    @Test
    fun givenDecoderFeed_whenClosed_thenConsumeAndDoFinalThrowException() {
        val feed = TestEncoderDecoder(TestConfig()).newDecoderFeed {}

        feed.close()

        try {
            feed.consume('d')
            fail()
        } catch (_: EncodingException) {
            // pass
        }

        try {
            feed.doFinal()
            fail()
        } catch (_: EncodingException) {
            // pass
        }
    }

    @Test
    fun givenEncoderFeed_whenClosed_thenConsumeAndDoFinalThrowException() {
        val feed = TestEncoderDecoder(TestConfig()).newEncoderFeed {}

        feed.close()

        try {
            feed.consume(5)
            fail()
        } catch (_: EncodingException) {
            // pass
        }

        try {
            feed.doFinal()
            fail()
        } catch (_: EncodingException) {
            // pass
        }
    }

    @Test
    fun givenDecoderFeed_whenConsumeThrowsException_thenFeedClosesAutomaticallyBeforeThrowing() {
        val feed = TestEncoderDecoder(TestConfig()).newDecoderFeed {
            throw IllegalStateException("")
        }

        try {
            feed.consume('d')
            fail()
        } catch (_: IllegalStateException) {
            assertTrue(feed.isClosed())
        }
    }

    @Test
    fun givenEncoderFeed_whenConsumeThrowsException_thenFeedClosesAutomaticallyBeforeThrowing() {
        val feed = TestEncoderDecoder(TestConfig()).newEncoderFeed {
            throw IllegalStateException("")
        }

        try {
            feed.consume(5)
            fail()
        } catch (_: IllegalStateException) {
            assertTrue(feed.isClosed())
        }
    }

    @Test
    fun givenDecoderFeed_whenIsLenientTrue_thenSpacesAndNewLinesAreNotSubmitted() {
        var out: Byte = 0
        val feed = TestEncoderDecoder(TestConfig(isLenient = true)).newDecoderFeed { decoded ->
            out = decoded
        }

        listOf(
            ' ',
            '\n',
            '\r',
            '\t'
        ).forEach { char ->
            feed.consume(char)
            assertEquals(0, out)
        }

        feed.consume('g')
        assertEquals(Byte.MAX_VALUE, out)
    }

    @Test
    fun givenDecoderFeed_whenIsLenientFalse_thenSpaceOrNewLinesThrowExceptionAndCloseFeed() {
        listOf(
            ' ',
            '\n',
            '\r',
            '\t'
        ).forEach { char ->
            val feed = TestEncoderDecoder(TestConfig(isLenient = false)).newDecoderFeed {}

            try {
                feed.consume(char)
                fail()
            } catch (_: EncodingException) {
                assertTrue(feed.isClosed())
            }
        }
    }

    @Test
    fun givenDecoderFeed_whenIsLenientNull_thenSpaceOrNewLinesArePassed() {
        var out: Byte = 0
        val feed = TestEncoderDecoder(TestConfig(isLenient = null)).newDecoderFeed { decoded ->
            out = decoded
        }

        listOf(
            ' ',
            '\n',
            '\r',
            '\t'
        ).forEach { char ->
            assertEquals(0, out)
            feed.consume(char)
            assertEquals(Byte.MAX_VALUE, out)

            // Reset for next
            out = 0
        }
    }

    @Test
    fun givenDecoderFeed_whenPaddingExpressedInConfig_thenPaddingIsNotSubmitted() {
        val feed = TestEncoderDecoder(TestConfig(paddingChar = '=')).newDecoderFeed {
            fail()
        }

        feed.consume('=')
    }

    @Test
    fun givenDecoderFeed_whenPaddingExpressedInConfig_thenThrowsExceptionAndClosesIfConsumingNonPaddingAfterSeeingPadding() {
        val feed = TestEncoderDecoder(TestConfig(isLenient = true, paddingChar = '=')).newDecoderFeed {
            fail()
        }

        feed.consume('=')
        feed.consume('=')

        // Whitespace after padding should simply be
        // ignored if isLenient is true, as per usual
        feed.consume(' ')

        try {
            feed.consume('g')
            fail()
        } catch (_: EncodingException) {
            assertTrue(feed.isClosed())
        }
    }

    @Test
    fun givenEncoderFeed_whenLineBreakExpressedInConfig_then() {
        val encoder = TestEncoderDecoder(
            config = TestConfig(
                isLenient = true,
                lineBreakInterval = 2,
                encodeReturn = { it }
            )
        )
        
        var count = 0
        val feed = encoder.newEncoderFeed { char ->
            count++
        }
        
        feed.consume(0)
        feed.consume(0)
        assertEquals(2, count)
        feed.consume(0)
        assertEquals(4, count)
        feed.consume(0)
        assertEquals(5, count)
        feed.consume(0)
        assertEquals(7, count)
        feed.consume(0)
        assertEquals(8, count)
        feed.consume(0)
        assertEquals(10, count)
        feed.consume(0)
        assertEquals(11, count)

        feed.doFinal()
    }
}
