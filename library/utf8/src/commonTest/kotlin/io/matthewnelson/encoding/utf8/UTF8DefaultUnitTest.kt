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

import io.matthewnelson.encoding.core.Decoder.Companion.decodeToByteArray
import io.matthewnelson.encoding.utf8.UTF8.CharPreProcessor.Companion.sizeUTF8
import kotlin.random.Random
import kotlin.test.Test
import kotlin.test.assertEquals

class UTF8DefaultUnitTest: UTF8BaseUnitTest(UTF8.Default) {

    @Test
    fun givenRandomSequences_whenEncoded_thenMatchesKotlinEncodeToByteArray() {
        val times = when {
            // Js/WasmJs
            IS_JS -> 500
            // Jvm
            utf8.config.replacementStrategy == UTF8.ReplacementStrategy.U_0034 -> 5_000
            // WasmWasi/Native
            else -> 2_500
        }

        repeat(times) { i ->
            val size = Random.nextInt(5, 42)
            val text = buildString(size) {
                repeat(size) {
                    val c = Random.nextInt().toChar()
                    append(c)
                }
            }

            val expected = text.encodeToByteArray()
            assertEquals(expected.size, text.sizeUTF8(utf8).toInt(), "sizes >> run[$i]")

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
        }
    }
}
