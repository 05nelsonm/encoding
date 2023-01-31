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
package io.matthewnelson.encoding.test

import kotlin.jvm.JvmStatic
import kotlin.random.Random
import kotlin.test.assertEquals
import kotlin.test.assertNull
import kotlin.test.assertTrue

abstract class BaseNEncodingTest {

    companion object {
        /**
         * Converts Bytes in hexidecimal format from https://cryptii.com to a ByteArray.
         * */
        @JvmStatic
        fun String.decodeHexToByteArray(): ByteArray {
            val newString = replace(" ", "")
            check(newString.length % 2 == 0) { "Hex must have an even length" }

            return newString.chunked(2)
                .map { it.toInt(16).toByte() }
                .toByteArray()
        }
    }

    protected data class Data<Data: Any, Expected: Any?>(
        val raw: Data,
        val expected: Expected,
        val message: String? = null,
    )

    protected abstract val decodeSuccessHelloWorld: Data<String, ByteArray>

    protected abstract val decodeFailureDataSet: Set<Data<String, Any?>>
    protected abstract val decodeSuccessDataSet: Set<Data<String, ByteArray>>

    protected abstract val encodeSuccessDataSet: Set<Data<String, String>>

    protected abstract fun decode(data: String): ByteArray?
    protected abstract fun encode(data: ByteArray): String

    /**
     * Decoding failure should always result in `null`
     * */
    protected fun checkDecodeFailureForDataSet(
        dataSet: Set<Data<String, Any?>> = decodeFailureDataSet
    ) {
        check(dataSet.isNotEmpty()) { "dataSet cannot be empty" }

        for (data in dataSet) {
            val decoded = decode(data.raw)
            assertNull(decoded, data.message)
        }
    }

    protected fun checkDecodeSuccessForDataSet(
        dataSet: Set<Data<String, ByteArray>> = decodeSuccessDataSet
    ) {
        check(dataSet.isNotEmpty()) { "dataSet cannot be empty" }

        for (data in dataSet) {
            val decoded = decode(data.raw)

            assertEquals(
                expected = data.expected.size,
                actual = decoded?.size,
                message = "ByteArray size did not match after decoding $data"
            )

            for ((i, expectedByte) in data.expected.withIndex()) {
                assertEquals(
                    expected = expectedByte,
                    actual = decoded?.get(i),
                    message = "${data.message}. Byte[$i] did not match for $data"
                )
            }
        }
    }

    protected fun checkEncodeSuccessForDataSet(
        dataSet: Set<Data<String, String>> = encodeSuccessDataSet
    ) {
        check(dataSet.isNotEmpty()) { "dataSet cannot be empty" }

        for (data in dataSet) {
            val encoded = encode(data.raw.encodeToByteArray())
            assertEquals(data.expected, encoded, message = data.message)
        }
    }

    protected fun checkUniversalDecoderParameters() {
        val emptyDecode = decode("")
        assertTrue(
            actual = emptyDecode?.isEmpty() == true,
            message = "Decoding an empty String should return an empty ByteArray"
        )
        val emptyDecode2 = decode("      ")
        assertTrue(
            actual = emptyDecode2?.isEmpty() == true,
            message = "Decoding a String with all spaces should return an empty ByteArray"
        )

//        // For Base32.Crockford, the standard '=' padding is only accepted when
//        // appended as a check symbol, so checking here for all other decoders is ok
//        assertNull(
//            actual = decode("=="),
//            message = "Decoding a String containing only padding '=' should return null"
//        )

        val newHelloWorldEncodedString = decodeSuccessHelloWorld.raw.let { string ->
            val sb = StringBuilder()

            for ((i, c) in string.withIndex()) {
                sb.append(c)
                if (i % 2 == 0) {
                    sb.append("\n  ")
                } else if (i % 3 == 0) {
                    sb.append("\t \r  ")
                }
            }

            sb.append(" \n  ")
            sb.toString()
        }

        val decoded = decode(newHelloWorldEncodedString)

        assertEquals(
            expected = decodeSuccessHelloWorld.expected.size,
            actual = decoded?.size,
            message = "Decoded ByteArray sizes did not match when checking for chars '\\n', '\\t', '\\r', ' '"
        )

        for ((i, expectedByte) in decodeSuccessHelloWorld.expected.withIndex()) {
            assertEquals(
                expected = expectedByte,
                actual = decoded?.get(i),
                message = "Byte[$i] did not match for $decodeSuccessHelloWorld when checking for chars '\\n', '\\t', '\\r', ' '"
            )
        }
    }

    protected fun checkUniversalEncoderParameters() {
        assertEquals(
            expected = "",
            actual = encode(ByteArray(0)),
            message = "Encoding empty ByteArray should return an empty String"
        )
    }

    protected fun checkRandomData() {
        val bytes = Random.nextBytes(1_000_000)
        val encoded = encode(bytes)
        val decoded = decode(encoded)!!

        assertEquals(bytes.size, decoded.size)
        bytes.forEachIndexed { index, byte ->
            assertEquals(byte, decoded[index])
        }
    }
}
