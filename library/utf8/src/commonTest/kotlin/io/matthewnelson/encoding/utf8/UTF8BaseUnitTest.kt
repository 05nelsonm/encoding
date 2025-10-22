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
package io.matthewnelson.encoding.utf8

import io.matthewnelson.encoding.base16.Base16
import io.matthewnelson.encoding.core.Decoder.Companion.decodeToByteArray
import io.matthewnelson.encoding.utf8.UTF8.CharPreProcessor.Companion.sizeUTF8
import kotlin.random.Random
import kotlin.test.Test
import kotlin.test.assertContentEquals
import kotlin.test.assertEquals

abstract class UTF8BaseUnitTest(protected val utf8: UTF8) {

    @Test
    fun givenOneByte_whenEncoded_thenIsAsExpected() {
        assertUtf8(hex = "00")
        assertUtf8(hex = "20")
        assertUtf8(hex = "7e")
        assertUtf8(hex = "7f")
    }

    @Test
    fun givenTwoBytes_whenEncoded_thenIsAsExpected() {
        assertUtf8(hex = "c280")
        assertUtf8(hex = "c3bf")
        assertUtf8(hex = "c480")
        assertUtf8(hex = "dfbf")
    }

    @Test
    fun givenThreeBytes_whenEncoded_thenIsAsExpected() {
        assertUtf8(hex = "e0a080")
        assertUtf8(hex = "e0bfbf")
        assertUtf8(hex = "e18080")
        assertUtf8(hex = "e1bfbf")
        assertUtf8(hex = "ed8080")
        assertUtf8(hex = "ed9fbf")
        assertUtf8(hex = "ee8080")
        assertUtf8(hex = "eebfbf")
        assertUtf8(hex = "ef8080")
        assertUtf8(hex = "efbfbf")
    }

    @Test
    fun givenFourBytes_whenEncoded_thenIsAsExpected() {
        assertUtf8(hex = "f0908080")
        assertUtf8(hex = "f48fbfbf")
    }

    @Test
    fun givenUnknownBytes_whenEncoded_thenIsAsExpected() {
        assertUtf8(hex = "f8")
        assertUtf8(hex = "f0f8")
        assertUtf8(hex = "ff")
        assertUtf8(hex = "f0ff")
        assertUtf8(hex = "80")
        assertUtf8(hex = "bf")
    }

    @Test
    fun givenLongSequence_whenEncoded_thenIsAsExpected() {
        assertUtf8(hex = "c080")
        assertUtf8(hex = "e08080")
        assertUtf8(hex = "f0808080")
        assertUtf8(hex = "c1bf")
        assertUtf8(hex = "e09fbf")
        assertUtf8(hex = "f08fbfbf")
    }

    private fun assertUtf8(hex: String) {
        val decoded = hex.decodeToByteArray(Base16)
        val kotlinUtf8 = decoded.decodeToString()

        val expected = kotlinUtf8.encodeToByteArray()
        assertEquals(expected.size, kotlinUtf8.sizeUTF8(utf8).toInt())

        val actual = kotlinUtf8.decodeToByteArray(utf8)

        try {
            assertContentEquals(expected, actual)
        } catch (e: AssertionError) {
            throw AssertionError("HEX[$hex]", e)
        }
    }
}
