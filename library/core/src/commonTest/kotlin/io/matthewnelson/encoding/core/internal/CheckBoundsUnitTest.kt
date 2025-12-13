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
package io.matthewnelson.encoding.core.internal

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.fail

class CheckBoundsUnitTest {

    @Test
    fun givenSize_whenNegativeOffset_thenThrowsIndexOutOfBoundsException() {
        try {
            2.checkBounds(-1, 1) { "..." }
            fail()
        } catch (e: IndexOutOfBoundsException) {
            assertEquals(true, e.message?.contains("offset["))
            assertEquals(true, e.message?.contains("< 0"))
        }
    }

    @Test
    fun givenSize_whenNegativeLen_thenThrowsIndexOutOfBoundsException() {
        try {
            2.checkBounds(1, -1) { "..." }
            fail()
        } catch (e: IndexOutOfBoundsException) {
            assertEquals(true, e.message?.contains("len["))
            assertEquals(true, e.message?.contains("< 0"))
        }
    }

    @Test
    fun givenSize_whenOffsetExceedsBounds_thenThrowsIndexOutOfBoundsException() {
        try {
            2.checkBounds(3, 0) { "PARAMETER NAME" }
            fail()
        } catch (e: IndexOutOfBoundsException) {
            assertEquals(true, e.message?.contains("PARAMETER NAME"))
        }
    }

    @Test
    fun givenSize_whenLenExceedsBounds_thenThrowsIndexOutOfBoundsException() {
        try {
            2.checkBounds(0, 3) { "PARAMETER NAME" }
            fail()
        } catch (e: IndexOutOfBoundsException) {
            assertEquals(true, e.message?.contains("PARAMETER NAME"))
        }
    }
}
