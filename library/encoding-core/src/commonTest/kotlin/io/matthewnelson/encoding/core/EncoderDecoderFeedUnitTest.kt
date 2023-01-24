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
import io.matthewnelson.encoding.core.util.byte
import kotlin.test.*

@OptIn(ExperimentalEncodingApi::class)
class EncoderDecoderFeedUnitTest {

    @Test
    fun givenFeed_whenDoFinalIsCalled_thenFeedCloses() {
        val feed = TestEncoderDecoder(TestConfig(isLenient = true)).newDecoderFeed { byte ->
            assertEquals(Byte.MIN_VALUE, byte)
        }

        assertFalse(feed.isClosed)
        feed.doFinal()
        assertTrue(feed.isClosed)
    }

    @Test
    fun givenFeed_whenClosed_thenConsumeAndDoFinalThrowException() {
        val feed = TestEncoderDecoder(TestConfig()).newDecoderFeed {}

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
    fun givenFeed_whenConsumeThrowsException_thenFeedClosesAutomaticallyBeforeThrowing() {
        val feed = TestEncoderDecoder(TestConfig()).newDecoderFeed {
            throw IllegalStateException("")
        }

        try {
            feed.consume(5)
            fail()
        } catch (_: IllegalStateException) {
            assertTrue(feed.isClosed)
        }
    }

    @Test
    fun givenDecoderFeed_whenIsLenientTrue_thenSpacesAndNewLinesAreNotSubmitted() {
        var out: Byte = 0
        val feed = TestEncoderDecoder(TestConfig(isLenient = true)).newDecoderFeed { byte ->
            out = byte
        }

        listOf(
            ' ',
            '\n',
            '\r',
            '\t'
        ).forEach { char ->
            feed.consume(char.byte)
            assertEquals(0, out)
        }

        feed.consume('g'.byte)
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
                feed.consume(char.byte)
                fail()
            } catch (_: EncodingException) {
                assertTrue(feed.isClosed)
            }
        }
    }

    @Test
    fun givenDecoderFeed_whenIsLenientNull_thenSpaceOrNewLinesArePassed() {
        var out: Byte = 0
        val feed = TestEncoderDecoder(TestConfig(isLenient = null)).newDecoderFeed { byte ->
            out = byte
        }

        listOf(
            ' ',
            '\n',
            '\r',
            '\t'
        ).forEach { char ->
            assertEquals(0, out)
            feed.consume(char.byte)
            assertEquals(Byte.MAX_VALUE, out)

            // Reset for next
            out = 0
        }
    }

    @Test
    fun givenDecoderFeed_whenPaddingExpressedInConfig_thenPaddingIsNotSubmitted() {
        val feed = TestEncoderDecoder(TestConfig(paddingByte = '='.byte)).newDecoderFeed {
            fail()
        }

        feed.consume('='.byte)
    }

    @Test
    fun givenDecoderFeed_whenPaddingExpressedInConfig_thenThrowsExceptionAndClosesIfConsumingNonPaddingAfterSeeingPadding() {
        val feed = TestEncoderDecoder(TestConfig(isLenient = true, paddingByte = '='.byte)).newDecoderFeed {
            fail()
        }

        feed.consume('='.byte)
        feed.consume('='.byte)

        // Whitespace after padding should simply be
        // ignored if isLenient is true, as per usual
        feed.consume(' '.byte)

        try {
            feed.consume('g'.byte)
            fail()
        } catch (_: EncodingException) {
            assertTrue(feed.isClosed)
        }
    }
}
