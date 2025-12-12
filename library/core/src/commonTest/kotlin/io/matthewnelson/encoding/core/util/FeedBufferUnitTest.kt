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

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith
import kotlin.test.fail

class FeedBufferUnitTest {

    private class TestBuffer(
        blockSize: Int,
        flush: Flush = Flush { _ -> },
        finalize: Finalize = Finalize { _, _ -> },
    ): FeedBuffer(
        blockSize, flush, finalize
    )

    @Test
    fun givenFeedBuffer_whenBlockSizeLessThanOrEqualToZero_thenThrowsException() {
        assertFailsWith<IllegalArgumentException> { TestBuffer(0) }
    }

    @Test
    fun givenBlockSize_whenBufferFilled_thenFlushIsInvoked() {
        for (i in 1..10) {
            var invokedCount = 0
            val buffer = TestBuffer(i, flush = { buffer ->
                invokedCount++
                assertEquals(i, buffer.size)
                for (int in buffer) {
                    assertEquals(i, int)
                }
            })

            @Suppress("UNUSED")
            for (j in 0 until i) {
                buffer.update(i)
            }

            assertEquals(1, invokedCount)
        }
    }

    @Test
    fun givenFeedBuffer_whenFinalizeIsCalled_thenExcessBufferedAreClearedBeforeFinalize() {
        val buffer = TestBuffer(15, finalize = { modulus, buffer ->
            assertEquals(5, modulus)
            assertEquals(15, buffer.size)

            for ((i, value) in (5 downTo 1).withIndex()) {
                assertEquals(value, buffer[i])
            }

            for (i in 5..buffer.lastIndex) {
                assertEquals(0, buffer[i])
            }
        })

        // Fill the buffer entirely
        repeat(15) {
            buffer.update(20)
        }

        // Update with 5 more
        for (i in 5 downTo 1) {
            buffer.update(i)
        }

        buffer.finalize()
    }

    @Test
    fun givenFeedBuffer_whenFinalizeIsCalled_thenBufferIsClearedAfterInvocation() {
        var invokedCount = 0

        val buffer = TestBuffer(5, finalize = { modulus, buffer ->
            when (invokedCount++) {
                0 -> {
                    for (i in 0 until modulus) {
                        assertEquals(4, buffer[i])
                    }
                }
                1 -> {
                    for (byte in buffer) {
                        assertEquals(0, byte)
                    }
                }
                else -> fail()
            }
        })

        // Fill it up to just before flush would be invoked
        repeat(4) {
            buffer.update(4)
        }

        // After first invocation, buffered should be cleared
        buffer.finalize()

        // Calling again will test that what was buffered for
        // the first finalize call has been cleared post
        // finalize invocation.
        buffer.finalize()

        // Ensure it was invoked
        assertEquals(2, invokedCount)
    }
}
