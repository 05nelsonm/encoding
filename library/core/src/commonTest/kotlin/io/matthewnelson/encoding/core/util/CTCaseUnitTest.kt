/*
 * Copyright (c) 2024 Matthew Nelson
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

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertNull

class CTCaseUnitTest {

    @Test
    fun givenDigits_whenInstantiated_areNotIncluded() {
        val table = "0abc"
        val case = CTCase(table = table)
        assertFalse(case.uppers.contains(table.first()))
        assertFalse(case.lowers.contains(table.first()))
    }

    @Test
    fun givenLetters_whenInstantiated_thenAreAsExpected() {
        val case = CTCase(table = "ABc")
        assertEquals("ABC".toSet(), case.uppers)
        assertEquals("abc".toSet(), case.lowers)
    }

    @Test
    fun givenUppercase_whenNotFound_thenReturnsNull() {
        val case = CTCase(table = "A")
        assertNull(case.uppercase('A'))
        assertNull(case.uppercase('b'))
    }

    @Test
    fun givenUppercase_whenFound_thenReturnsExpected() {
        val case = CTCase(table = "ABC")
        assertEquals('A', case.uppercase('a'))
        assertEquals('B', case.uppercase('b'))
        assertEquals('C', case.uppercase('c'))
    }

    @Test
    fun givenLowercase_whenNotFound_thenReturnsNull() {
        val case = CTCase(table = "A")
        assertNull(case.lowercase('a'))
        assertNull(case.lowercase('b'))
    }

    @Test
    fun givenLowercase_whenFound_thenReturnsExpected() {
        val case = CTCase(table = "ABC")
        assertEquals('a', case.lowercase('A'))
        assertEquals('b', case.lowercase('B'))
        assertEquals('c', case.lowercase('C'))
    }
}
