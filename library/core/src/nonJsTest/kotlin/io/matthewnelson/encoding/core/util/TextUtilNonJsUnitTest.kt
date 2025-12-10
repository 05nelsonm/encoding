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
package io.matthewnelson.encoding.core.util

import io.matthewnelson.encoding.core.internal.commonWipe
import kotlin.test.Test
import kotlin.test.assertEquals

class TextUtilNonJsUnitTest {

    @Test
    fun givenStringBuilder_whenWipeLenLessThan1_thenUsesCapacity() {
        val expected = 4
        val sb = StringBuilder(expected)
        repeat(expected) { sb.append('a') }
        assertEquals(expected, sb.capacity())
        assertEquals(expected, sb.length)

        // 0
        sb.commonWipe(len = 0, finalLen = 1)
        assertEquals(expected, sb.capacity())
        // Confirm via finalLen that it did not return early
        assertEquals(1, sb.length)

        repeat(expected - 1) { sb.append('a') }
        assertEquals(expected, sb.capacity())
        assertEquals(expected, sb.length)

        // Try negative
        sb.commonWipe(len = -1, finalLen = 1)
        assertEquals(expected, sb.capacity())
        // Confirm via finalLen that it did not return early
        assertEquals(1, sb.length)
    }

    @Test
    fun givenStringBuilder_whenWipeLenGreaterThanCapacity_thenUsesCapacity() {
        val expected = 4
        val sb = StringBuilder(expected)
        repeat(expected) { sb.append('a') }
        assertEquals(expected, sb.capacity())
        assertEquals(expected, sb.length)

        // 0
        sb.commonWipe(len = expected + 1, finalLen = 1)
        // backing array did not increase
        assertEquals(expected, sb.capacity())
        // Confirm via finalLen that it did not return early
        assertEquals(1, sb.length)
    }
}
