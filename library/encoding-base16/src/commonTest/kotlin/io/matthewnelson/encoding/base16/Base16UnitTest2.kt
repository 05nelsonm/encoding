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
package io.matthewnelson.encoding.base16

import kotlin.test.Test
import kotlin.test.assertEquals

class Base16UnitTest2 {

    @Test
    fun givenBase16_whenLowercaseAndUppercaseChars_thenMatch() {
        assertEquals(Base16.CHARS_UPPER, Base16.CHARS_LOWER.uppercase())
        assertEquals(Base16.CHARS_LOWER, Base16.CHARS_UPPER.lowercase())
    }
}
