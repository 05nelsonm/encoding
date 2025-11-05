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
import io.matthewnelson.encoding.core.EncodingSizeException
import io.matthewnelson.encoding.utf8.UTF8.CharPreProcessor.Companion.sizeUTF8
import io.matthewnelson.encoding.utf8.internal.decodeOutMaxSize32
import kotlin.random.Random
import kotlin.test.Test
import kotlin.test.assertContentEquals
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith
import kotlin.test.fail

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
        assertUtf8(hex = "00")
        assertUtf8(hex = "20")
        assertUtf8(hex = "7e")
        assertUtf8(hex = "7f")
    }

    @Test
    fun givenTwoBytes_whenEncodedDecoded_thenIsAsExpected() {
        assertUtf8(hex = "c280") // needed = 2 (Success)
        assertUtf8(hex = "c180") // needed = 2 (Null >> needed = 0 >> Success)
        assertUtf8(hex = "c2e2") // needed = 2 (Non-continuation >> needed = 3 >> Truncated input)
        assertUtf8(hex = "c21e") // needed = 2 (Non-continuation >> needed = 0 >> Success)
        assertUtf8(hex = "c480") // needed = 2 (Success)
        assertUtf8(hex = "dfbf") // needed = 2 (Success)
        assertUtf8(hex = "85af") // needed = 0 (Replace, Replace)
        assertUtf8(hex = "7a43") // needed = 0 (Success, Success)
        assertUtf8(hex = "c2bf") // needed = 2 (Success)
        assertUtf8(hex = "cf7a") // needed = 2 (Non-continuation >> needed = 0 >> Success)
        assertUtf8(hex = "c1aa") // needed = 2 (Null >> needed = 0 >> Replace)
    }

    @Test
    fun givenThreeBytes_whenEncodedDecoded_thenIsAsExpected() {
        assertUtf8(hex = "e0a080") // needed = 3 (Success)
        assertUtf8(hex = "e0bfbf") // needed = 3 (Success)
        assertUtf8(hex = "e18080") // needed = 3 (Success)
        assertUtf8(hex = "e1bfbf") // needed = 3 (Success)
        assertUtf8(hex = "ed8080") // needed = 3 (Success)
        assertUtf8(hex = "ed9fbf") // needed = 3 (Success)
        assertUtf8(hex = "ee8080") // needed = 3 (Success)
        assertUtf8(hex = "eebfbf") // needed = 3 (Success)
        assertUtf8(hex = "ef8080") // needed = 3 (Success)
        assertUtf8(hex = "efbfbf") // needed = 3 (Success)
        assertUtf8(hex = "e09faf") // needed = 3 (Non-shortest form >> needed = 0 >> Replace >> needed = 0 >> Replace)
        assertUtf8(hex = "e0c2c2") // needed = 3 (Non-shortest form >> needed = 2 >> Replace >> needed = 2 >> Truncated)
        assertUtf8(hex = "e0e0c2") // needed = 3 (Non-shortest form >> needed = 3 >> Replace >> needed = 3 >> Truncated)
        assertUtf8(hex = "e0f0c2") // needed = 3 (Non-shortest form >> needed = 4 >> Replace >> needed = 2 >> Truncated)
        assertUtf8(hex = "e0c2af") // needed = 3 (Non-shortest form >> needed = 2 >> Success)
        assertUtf8(hex = "efaf7a") // needed = 3 (Non-continuation >> needed = 0 >> Success)
    }

    @Test
    fun givenFourBytes_whenEncodedDecoded_thenIsAsExpected() {
        assertUtf8(hex = "f0908080") // needed = 4 (Success)
        assertUtf8(hex = "f48fbfbf") // needed = 4 (Success)
        // TODO: Test more consumeProtected needed = 4 logical branches
    }

    @Test
    fun givenUnknownBytes_whenEncodedDecoded_thenIsAsExpected() {
        assertUtf8(hex = "f8")
        assertUtf8(hex = "f0f8")
        assertUtf8(hex = "ff")
        assertUtf8(hex = "f0ff")
        assertUtf8(hex = "80")
        assertUtf8(hex = "bf")
    }

    @Test
    fun givenLongSequence_whenEncodedDecoded_thenIsAsExpected() {
        assertUtf8(hex = "c080")
        assertUtf8(hex = "e08080")
        assertUtf8(hex = "f0808080")
        assertUtf8(hex = "c1bf")
        assertUtf8(hex = "e09fbf")
        assertUtf8(hex = "f08fbfbf")
    }

    @Test
    fun givenDanglingHighSurrogate_whenEncodedDecoded_thenIsAsExpected() {
        assertUtf8(hex = "3f")
        assertUtf8(hex = "eda080")
    }

    @Test
    fun givenLowSurrogateWithoutHighSurrogate_whenEncodedDecoded_thenIsAsExpected() {
        assertUtf8(hex = "edb080")
    }

    @Test
    fun givenHighSurrogateFollowedByNonSurrogate_whenEncodedDecoded_thenIsAsExpected() {
        assertUtf8(hex = "3fee8080")
        assertUtf8(hex = "f090ee8080")
        assertUtf8(hex = "3f61")
        assertUtf8(hex = "f09061")
        assertUtf8(hex = "eda18cedbeb4")
    }

    @Test
    fun givenHighSurrogate_whenEncodedDecoded_thenIsAsExpected() {
        assertUtf8(hex = "edafbf")
        assertUtf8(hex = "edb39a")
        assertUtf8(hex = "e696a4ed9f7a")
    }

    @Test
    fun givenDoubleLowSurrogate_whenEncodedDecoded_thenIsAsExpected() {
        assertUtf8(hex = "3f3f")
        assertUtf8(hex = "edb080edb080")
    }

    @Test
    fun givenDoubleHighSurrogate_whenEncodedDecoded_thenIsAsExpected() {
        assertUtf8(hex = "3f3f")
        assertUtf8(hex = "eda080eda080")
        assertUtf8(hex = "edafbfedb39a")
    }

    @Test
    fun givenPartialSurrogateFollowedByNonSurrogate_whenEncodedDecoded_thenIsAsExpected() {
        assertUtf8(hex = "eda080edbfbfedaf41")
        assertUtf8(hex = "e180e2f09192f1bf41")
        assertUtf8(hex = "c0afe080bff0818241")
        assertUtf8(hex = "f4919293ff4180bf42")
    }

    @Test
    fun givenTruncatedInputSequences_whenEncodedDecoded_thenIsAsExpected() {
        // Exercises doFinalProtected logical branches

        assertUtf8(hex = "c2")       // needed = 2, 1 buffered byte
        assertUtf8(hex = "e0")       // needed = 3, 1 buffered byte
        assertUtf8(hex = "f0")       // needed = 4, 1 buffered byte

        assertUtf8(hex = "e0af")     // needed = 3, 2 buffered bytes (Index exhaustion)
        assertUtf8(hex = "e080")     // needed = 3, 2 buffered bytes (Non-shortest form)

        assertUtf8(hex = "f093")     // needed = 4, 2 buffered bytes (Index exhaustion)
        assertUtf8(hex = "f080")     // needed = 4, 2 buffered bytes (Non-shortest form)
        assertUtf8(hex = "f4f5")     // needed = 4, 2 buffered bytes (Exceeds Unicode max)
        assertUtf8(hex = "f5f5")     // needed = 4, 2 buffered bytes (Exceeds Unicode max)

        assertUtf8(hex = "f09388")   // needed = 4, 3 buffered bytes (Index exhaustion)
        assertUtf8(hex = "f0c288")   // needed = 4, 3 buffered bytes (Non-continuation >> needed = 2 >> Success)
        assertUtf8(hex = "f0c2e0")   // needed = 4, 3 buffered bytes (Non-continuation >> needed = 2 >> Non-continuation)
        assertUtf8(hex = "f0e088")   // needed = 4, 3 buffered bytes (Non-continuation >> needed = 3 >> Non-shortest form)
        assertUtf8(hex = "f0e0af")   // needed = 4, 3 buffered bytes (Non-continuation >> needed = 3 >> Index exhaustion)

        assertUtf8(hex = "f0f080")   // needed = 4, 3 buffered bytes (Non-continuation >> needed = 4 >> Non-shortest form)
        assertUtf8(hex = "f0f4f5")   // needed = 4, 3 buffered bytes (Non-continuation >> needed = 4 >> Exceeds Unicode max)
        assertUtf8(hex = "f0f0f0")   // needed = 4, 3 buffered bytes (Non-continuation >> needed = 4 >> Non-continuation)
    }

    @Test
    fun givenDecodeOutSize_whenExceedsFastPathCalculation_thenUsesCharPreProcessorToCalculateExactSize() {
        val size = (Int.MAX_VALUE / 3) + 1
        var char = 'A'

        // Test size does in fact exceed the "fast path" calculation (size * 3)
        assertFailsWith<EncodingSizeException> {
            utf8.config.decodeOutMaxSize32(size, useCharPreProcessorIfNeeded = false) { char }
        }

        val actual = utf8.config.decodeOutMaxSize32(size, useCharPreProcessorIfNeeded = true) { char }

        // ASCII should be 1 byte per character, and thus encodable.
        assertEquals(size, actual)

        // Ensure that the function stops early when CharPreProcessor.currentSize exceeds Int.MAX_VALUE
        var index = 0
        // 2 bytes per character
        char = (Byte.MAX_VALUE.toInt() + 500).toChar()
        try {
            utf8.config.decodeOutMaxSize32(Int.MAX_VALUE, useCharPreProcessorIfNeeded = true) { i ->
                index = i
                char
            }
            fail("CharPreProcessor did not stop when currentSize exceeded Int.MAX_VALUE")
        } catch (_: EncodingSizeException) {
            assertEquals((Int.MAX_VALUE / 2) + 1, index)
        }
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
            utf8.config.replacementStrategy == UTF8.ReplacementStrategy.U_0034 -> 5_000 to 106
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
