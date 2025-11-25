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
import io.matthewnelson.encoding.core.Encoder.Companion.encodeToString
import io.matthewnelson.encoding.test.IS_JS
import io.matthewnelson.encoding.utf8.UTF8.CharPreProcessor.Companion.sizeUTF8
import kotlin.random.Random
import kotlin.test.Test
import kotlin.test.assertContentEquals
import kotlin.test.assertEquals

abstract class UTF8BaseUnitTest(protected val utf8: UTF8) {

    private fun assertUtf8(hex: String) {
        val bytes = hex.decodeToByteArray(Base16)
        val utf8Kotlin = bytes.decodeToString()

        val expected = utf8Kotlin.encodeToByteArray()
        assertEquals(expected.size.toLong(), utf8Kotlin.sizeUTF8(utf8), "size")

        val actual = utf8Kotlin.decodeToByteArray(utf8)

        assertContentEquals(expected, actual, "bytes - HEX[$hex]")

        if (utf8.config.replacementStrategy == UTF8.ReplacementStrategy.KOTLIN) {
            val utf8Encoding = bytes.encodeToString(utf8)
            assertEquals(utf8Kotlin, utf8Encoding, "utf8 - HEX[$hex]")
        }
    }

    @Test
    fun givenZeroBytes_whenEncodedDecoded_thenIsAsExpected() {
        assertUtf8(hex = "")
    }

    @Test
    fun givenOneByte_whenEncodedDecoded_thenIsAsExpected() {
        assertUtf8(hex = "00")       // Success
        assertUtf8(hex = "20")       // Success
        assertUtf8(hex = "7e")       // Success
        assertUtf8(hex = "7f")       // Success
        assertUtf8(hex = "ff")       // Replace
    }

    @Test
    fun givenTwoBytes_whenEncodedDecoded_thenIsAsExpected() {
        assertUtf8(hex = "c280")     // Success
        assertUtf8(hex = "c480")     // Success
        assertUtf8(hex = "dfbf")     // Success
        assertUtf8(hex = "c2bf")     // Success

        assertUtf8(hex = "85af")     // Replace >> needed = 0 >> Replace
        assertUtf8(hex = "7a43")     // Success >> needed = 0 >> Success

        assertUtf8(hex = "c180")     // Null >> needed = 0 >> Success
        assertUtf8(hex = "c1aa")     // Null >> needed = 0 >> Replace

        assertUtf8(hex = "c2e2")     // Non-continuation >> needed = 3 >> Truncated
        assertUtf8(hex = "c2f2")     // Non-continuation >> needed = 3 >> Truncated

        assertUtf8(hex = "c21e")     // Non-continuation >> needed = 0 >> Success
        assertUtf8(hex = "cf7a")     // Non-continuation >> needed = 0 >> Success
    }

    @Test
    fun givenThreeBytes_whenEncodedDecoded_thenIsAsExpected() {
        assertUtf8(hex = "e0a080")   // Success
        assertUtf8(hex = "e0bfbf")   // Success
        assertUtf8(hex = "e18080")   // Success
        assertUtf8(hex = "e1bfbf")   // Success
        assertUtf8(hex = "ed8080")   // Success
        assertUtf8(hex = "ed9fbf")   // Success
        assertUtf8(hex = "ee8080")   // Success
        assertUtf8(hex = "eebfbf")   // Success
        assertUtf8(hex = "ef8080")   // Success
        assertUtf8(hex = "efbfbf")   // Success

        assertUtf8(hex = "e09faf")   // Non-shortest form >> needed = 0 >> Replace >> needed = 0 >> Replace
        assertUtf8(hex = "e0c2af")   // Non-shortest form >> needed = 2 >> Success
        assertUtf8(hex = "e0c2c2")   // Non-shortest form >> needed = 2 >> Replace >> needed = 2 >> Truncated
        assertUtf8(hex = "e0e0c2")   // Non-shortest form >> needed = 3 >> Replace >> needed = 3 >> Truncated
        assertUtf8(hex = "e0f0c2")   // Non-shortest form >> needed = 4 >> Replace >> needed = 2 >> Truncated

        assertUtf8(hex = "efaf7a")   // Non-continuation >> needed = 0 >> Success

        assertUtf8(hex = "edaf41")   // Partial surrogate >> needed = 0 >> Success
        assertUtf8(hex = "edafaf")   // Partial surrogate >> needed = 0 >> Replace
        assertUtf8(hex = "eded1e")   // Partial surrogate >> needed = 0 >> Replace
    }

    @Test
    fun givenFourBytes_whenEncodedDecoded_thenIsAsExpected() {
        assertUtf8(hex = "f0908080") // Success
        assertUtf8(hex = "f48fbfbf") // Success

        assertUtf8(hex = "f00fbfbf") // Non-shortest form >> needed = 0 >> Success >> needed = 0 >> Replace
        assertUtf8(hex = "f00fc2bf") // Non-shortest form >> needed = 0 >> Success >> needed = 2 >> Success
        assertUtf8(hex = "f00fc21e") // Non-shortest form >> needed = 0 >> Success >> needed = 2 >> Non-continuation >> needed = 0 >> Replace
        assertUtf8(hex = "f50fed1e") // Exceeds Unicode max >> needed = 0 >> Success >> needed = 3 >> Truncated
        assertUtf8(hex = "f5c2bf00") // Exceeds Unicode max >> needed = 2 >> Success >> needed = 0 >> Success
        assertUtf8(hex = "f5c21e00") // Exceeds Unicode max >> needed = 2 >> Non-continuation >> needed = 0 >> Success >> needed = 0 >> Success
        assertUtf8(hex = "f5efbfbf") // Exceeds Unicode max >> needed = 3 >> Success
        assertUtf8(hex = "f5e09faf") // Exceeds Unicode max >> needed = 3 >> Non-shortest form >> needed = 0 >> Replace >> needed = 0 >> Replace
        assertUtf8(hex = "f5e0c2af") // Exceeds Unicode max >> needed = 3 >> Non-shortest form >> needed = 2 >> Success
        assertUtf8(hex = "f5e0c2c2") // Exceeds Unicode max >> needed = 3 >> Non-shortest form >> needed = 2 >> Replace >> needed = 2 >> Truncated
        assertUtf8(hex = "f5e0e0c2") // Exceeds Unicode max >> needed = 3 >> Non-shortest form >> needed = 3 >> Replace >> needed = 3 >> Truncated
        assertUtf8(hex = "f5e0f0c2") // Exceeds Unicode max >> needed = 3 >> Non-shortest form >> needed = 4 >> Replace >> needed = 2 >> Truncated
        assertUtf8(hex = "f5e0f5ed") // Exceeds Unicode max >> needed = 3 >> Non-shortest form >> needed = 4 >> Replace >> needed = 4 >> Truncated
        assertUtf8(hex = "f5efaf7a") // Exceeds Unicode max >> needed = 3 >> Non-continuation >> needed = 0 >> Success
        assertUtf8(hex = "f5edaf41") // Exceeds Unicode max >> needed = 3 >> Partial surrogate >> needed = 0 >> Success
        assertUtf8(hex = "f5edafaf") // Exceeds Unicode max >> needed = 3 >> Partial surrogate >> needed = 0 >> Replace
        assertUtf8(hex = "f5eded1e") // Exceeds Unicode max >> needed = 3 >> Partial surrogate >> needed = 0 >> Replace
        assertUtf8(hex = "f5f48fbf") // Exceeds Unicode max >> needed = 4 >> Truncated

        assertUtf8(hex = "f0c28080") // Non-continuation(b1) >> needed = 2 >> Success >> needed = 0 >> Success
        assertUtf8(hex = "f0c2c280") // Non-continuation(b1) >> needed = 2 >> Non-continuation >> needed = 2 >> Success
        assertUtf8(hex = "f0911e80") // Non-continuation(b2) >> needed = 0 >> Replace >> needed = 0 >> Replace
        assertUtf8(hex = "f090c280") // Non-continuation(b2) >> needed = 2 >> Success
        assertUtf8(hex = "f090c2c2") // Non-continuation(b2) >> needed = 2 >> Non-continuation >> Truncated
        assertUtf8(hex = "f090edc2") // Non-continuation(b2) >> needed = 3 >> Truncated
        assertUtf8(hex = "f090f5c2") // Non-continuation(b2) >> needed = 4 >> Truncated
        assertUtf8(hex = "f0908000") // Non-continuation(b3) >> needed = 0 >> Success
    }

    @Test
    fun givenTruncatedInputSequences_whenEncodedDecoded_thenIsAsExpected() {
        // Exercises doFinalProtected logical branches

        // 1 buffered byte
        assertUtf8(hex = "c2")       // needed = 2 (Replace)
        assertUtf8(hex = "e0")       // needed = 3 (Replace)
        assertUtf8(hex = "f0")       // needed = 4 (Replace)

        // 2 buffered bytes
        assertUtf8(hex = "e0af")     // needed = 3 (Index exhaustion)
        assertUtf8(hex = "e080")     // needed = 3 (Non-shortest form >> needed = 0 >> Replace)
        assertUtf8(hex = "edaf")     // needed = 3 (Partial surrogate)
        assertUtf8(hex = "ed1e")     // needed = 3 (Partial surrogate >> needed = 0 >> Success)

        assertUtf8(hex = "f093")     // needed = 4 (Index exhaustion)
        assertUtf8(hex = "f080")     // needed = 4 (Non-shortest form >> needed = 0 >> Replace)
        assertUtf8(hex = "f4f5")     // needed = 4 (Exceeds Unicode max >> needed = 4 >> Replace)
        assertUtf8(hex = "f5f5")     // needed = 4 (Exceeds Unicode max >> needed = 4 >> Replace)

        // 3 buffered bytes
        assertUtf8(hex = "f09388")   // needed = 4 (Index exhaustion)
        assertUtf8(hex = "f0c288")   // needed = 4 (Non-continuation >> needed = 2 >> Success)
        assertUtf8(hex = "f0c2e0")   // needed = 4 (Non-continuation >> needed = 2 >> Non-continuation)
        assertUtf8(hex = "f0e088")   // needed = 4 (Non-continuation >> needed = 3 >> Non-shortest form)
        assertUtf8(hex = "f0e0af")   // needed = 4 (Non-continuation >> needed = 3 >> Index exhaustion)
        assertUtf8(hex = "f0edaf")   // needed = 4 (Non-continuation >> needed = 3 >> Partial surrogate)
        assertUtf8(hex = "f0ed1d")   // needed = 4 (Non-continuation >> needed = 3 >> Partial surrogate >> needed = 0 >> Success)

        assertUtf8(hex = "f0f080")   // needed = 4 (Non-continuation >> needed = 4 >> Non-shortest form)
        assertUtf8(hex = "f0f4f5")   // needed = 4 (Non-continuation >> needed = 4 >> Exceeds Unicode max)
        assertUtf8(hex = "f0f0f0")   // needed = 4 (Non-continuation >> needed = 4 >> Non-continuation)
    }

    @Test
    fun givenRandomSequences_whenEncoded_thenMatchesKotlinEncodeToByteArray() {
        if (utf8.config.replacementStrategy != UTF8.ReplacementStrategy.KOTLIN) {
            println("Skipping...")
            return
        }

        val (times, maxSize) = when {
            // Js/WasmJs
            IS_JS -> 250 to 42
            // Jvm
            utf8.config.replacementStrategy == UTF8.ReplacementStrategy.U_003F -> 5_000 to 106
            // WasmWasi/Native
            else -> 2_500 to 88
        }

        repeat(times) { i ->
            val size = Random.nextInt(5, maxSize)
            val text = buildString(size) {
                repeat(size) {
                    val c = Random.nextInt().toChar()
                    append(c)
                }
            }

            val expected = text.encodeToByteArray()
            assertEquals(expected.size.toLong(), text.sizeUTF8(utf8), "sizes >> run[$i]")

            val actual = text.decodeToByteArray(utf8)
            assertEquals(
                expected.size,
                actual.size,
                """

                    Sizes do not match for run[$i]
                    Expected${expected.toList()}
                      Actual${actual.toList()}
                        TEXT[$text]

                """.trimIndent()
            )
            for (j in expected.indices) {
                assertEquals(
                    expected[j],
                    actual[j],
                    """

                        Content mismatched at index[$j] for run[$i]
                        Expected${expected.toList()}
                          Actual${actual.toList()}
                            TEXT[$text]

                    """.trimIndent()
                )
            }

            val utf8Kotlin = expected.decodeToString()
            val utf8Encoding = expected.encodeToString(utf8)
            assertEquals(
                utf8Kotlin,
                utf8Encoding,
                """

                    Content mismatched for run[$i]
                    Expected[$utf8Kotlin]
                      Actual[$utf8Encoding]
                        TEXT[$text]
                """.trimIndent()
            )
        }
    }
}
