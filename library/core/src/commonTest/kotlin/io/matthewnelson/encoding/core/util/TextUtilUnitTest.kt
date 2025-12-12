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

// See also nonJsTest TextUtilNonJsUnitTest
@Suppress("DEPRECATION")
class TextUtilUnitTest {

    @Test
    fun givenStringBuilder_whenCapacity0_thenWipeDoesNothing() {
        val sb = StringBuilder(0)
        assertEquals(0, sb.capacity())
        // If we didn't return early (capacity == 0),
        // finalLen of 1 would extend capacity.
        sb.commonWipe(sb.length, finalLen = 1)
        assertEquals(0, sb.capacity())
        assertEquals(0, sb.length)
    }

    @Test
    fun givenStringBuilder_whenWipe_thenSetsFinalLength0() {
        val sb = StringBuilder(1).append('a')
        assertEquals(1, sb.length)
        sb.wipe()
        assertEquals(0, sb.length)
    }
}
