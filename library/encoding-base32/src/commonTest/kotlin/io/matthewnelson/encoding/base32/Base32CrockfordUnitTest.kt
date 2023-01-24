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
@file:Suppress("SpellCheckingInspection")

package io.matthewnelson.encoding.base32

import kotlin.test.Test
import kotlin.test.assertEquals

class Base32CrockfordUnitTest {

    @Test
    fun givenBase32Crockford_whenLowercaseAndUppercaseChars_thenMatch() {
        assertEquals(Base32.Crockford.CHARS_UPPER, Base32.Crockford.CHARS_LOWER.uppercase())
        assertEquals(Base32.Crockford.CHARS_LOWER, Base32.Crockford.CHARS_UPPER.lowercase())
    }
}