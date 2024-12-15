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

import io.matthewnelson.encoding.core.Decoder.Companion.decodeToByteArray
import io.matthewnelson.encoding.core.Decoder.Companion.decodeToByteArrayOrNull
import io.matthewnelson.encoding.core.Encoder.Companion.encodeToString
import io.matthewnelson.encoding.test.BaseNEncodingTest
import kotlin.test.Test
import kotlin.test.assertEquals

class Base16UnitTest: BaseNEncodingTest() {

    private var useConstantTime = false
    private var useLowercase = false

    private fun base16(): Base16 = Base16 {
        isConstantTime = useConstantTime
        encodeToLowercase = useLowercase
    }

    override val decodeFailureDataSet: Set<Data<String, Any?>> = setOf(
        Data(raw = "A=", expected = null, message = "Typical padding character '=' should return null"),
        Data(raw = "666", expected = null, message = "Truncated value should return null"),
        Data(raw = "6", expected = null, message = "Truncated value should return null"),
        Data(raw = "AG", expected = null, message = "Character 'G' should return null"),
        Data(raw = "AH", expected = null, message = "Character 'H' should return null"),
        Data(raw = "AI", expected = null, message = "Character 'I' should return null"),
        Data(raw = "AJ", expected = null, message = "Character 'J' should return null"),
        Data(raw = "AK", expected = null, message = "Character 'K' should return null"),
        Data(raw = "AL", expected = null, message = "Character 'L' should return null"),
        Data(raw = "AM", expected = null, message = "Character 'M' should return null"),
        Data(raw = "AN", expected = null, message = "Character 'N' should return null"),
        Data(raw = "AO", expected = null, message = "Character 'O' should return null"),
        Data(raw = "AP", expected = null, message = "Character 'P' should return null"),
        Data(raw = "AQ", expected = null, message = "Character 'Q' should return null"),
        Data(raw = "AR", expected = null, message = "Character 'R' should return null"),
        Data(raw = "AS", expected = null, message = "Character 'S' should return null"),
        Data(raw = "AT", expected = null, message = "Character 'T' should return null"),
        Data(raw = "AU", expected = null, message = "Character 'U' should return null"),
        Data(raw = "AV", expected = null, message = "Character 'V' should return null"),
        Data(raw = "AW", expected = null, message = "Character 'W' should return null"),
        Data(raw = "AX", expected = null, message = "Character 'X' should return null"),
        Data(raw = "AY", expected = null, message = "Character 'Y' should return null"),
        Data(raw = "AZ", expected = null, message = "Character 'Z' should return null"),
    )

    override val decodeSuccessHelloWorld: Data<String, ByteArray> =
        Data(raw = "48656C6C6F20576F726C6421", expected = "Hello World!".encodeToByteArray())

    override val decodeSuccessDataSet: Set<Data<String, ByteArray>> = setOf(
        decodeSuccessHelloWorld,
        Data(raw = "48656C6C6 F20 576F726 C6 421", expected = "Hello World!".encodeToByteArray(), message = "Spaces ' ' should be ignored"),
        Data(raw = "66", expected = "f".encodeToByteArray()),
        Data(raw = "666F", expected = "fo".encodeToByteArray()),
        Data(raw = "666F6F", expected = "foo".encodeToByteArray()),
        Data(raw = "666F6F20", expected = "foo ".encodeToByteArray()),
        Data(raw = "666F6F2062", expected = "foo b".encodeToByteArray()),
        Data(raw = "666F6F206261", expected = "foo ba".encodeToByteArray()),
        Data(raw = "666F6F20626172", expected = "foo bar".encodeToByteArray()),
        Data(raw = "46", expected = "F".encodeToByteArray()),

        Data(raw = "464F", expected = "FO".encodeToByteArray()),
        Data(raw = "464F4F", expected = "FOO".encodeToByteArray()),
        Data(raw = "464F4F20", expected = "FOO ".encodeToByteArray()),
        Data(raw = "464F4F2042", expected = "FOO B".encodeToByteArray()),
        Data(raw = "464F4F204241", expected = "FOO BA".encodeToByteArray()),
        Data(raw = "464F4F20424152", expected = "FOO BAR".encodeToByteArray()),
        Data(raw = "72", expected = "r".encodeToByteArray()),
        Data(raw = "7261", expected = "ra".encodeToByteArray()),
        Data(raw = "726162", expected = "rab".encodeToByteArray()),
        Data(raw = "72616220", expected = "rab ".encodeToByteArray()),
        Data(raw = "726162206F", expected = "rab o".encodeToByteArray()),
        Data(raw = "726162206F6F", expected = "rab oo".encodeToByteArray()),
        Data(raw = "726162206F6F66", expected = "rab oof".encodeToByteArray()),
        Data(raw = "52", expected = "R".encodeToByteArray()),
        Data(raw = "5241", expected = "RA".encodeToByteArray()),
        Data(raw = "524142", expected = "RAB".encodeToByteArray()),
        Data(raw = "52414220", expected = "RAB ".encodeToByteArray()),
        Data(raw = "524142204F", expected = "RAB O".encodeToByteArray()),
        Data(raw = "524142204F4F", expected = "RAB OO".encodeToByteArray()),
        Data(raw = "524142204F4F46", expected = "RAB OOF".encodeToByteArray()),
        Data(raw = "524142204f4f46", expected = "RAB OOF".encodeToByteArray()),
    )

    override val encodeSuccessDataSet: Set<Data<String, String>> = setOf(
        Data(raw = "Hello World!", expected = "48656C6C6F20576F726C6421"),
        Data(raw = "f", expected = "66"),
        Data(raw = "fo", expected = "666F"),
        Data(raw = "foo", expected = "666F6F"),
        Data(raw = "foo ", expected = "666F6F20"),
        Data(raw = "foo b", expected = "666F6F2062"),
        Data(raw = "foo ba", expected = "666F6F206261"),
        Data(raw = "foo bar", expected = "666F6F20626172"),
        Data(raw = "F", expected = "46"),

        Data(raw = "FO", expected = "464F"),
        Data(raw = "FOO", expected = "464F4F"),
        Data(raw = "FOO ", expected = "464F4F20"),
        Data(raw = "FOO B", expected = "464F4F2042"),
        Data(raw = "FOO BA", expected = "464F4F204241"),
        Data(raw = "FOO BAR", expected = "464F4F20424152"),
        Data(raw = "r", expected = "72"),
        Data(raw = "ra", expected = "7261"),
        Data(raw = "rab", expected = "726162"),
        Data(raw = "rab ", expected = "72616220"),
        Data(raw = "rab o", expected = "726162206F"),
        Data(raw = "rab oo", expected = "726162206F6F"),
        Data(raw = "rab oof", expected = "726162206F6F66"),
        Data(raw = "R", expected = "52"),
        Data(raw = "RA", expected = "5241"),
        Data(raw = "RAB", expected = "524142"),
        Data(raw = "RAB ", expected = "52414220"),
        Data(raw = "RAB O", expected = "524142204F"),
        Data(raw = "RAB OO", expected = "524142204F4F"),
        Data(raw = "RAB OOF", expected = "524142204F4F46"),
    )

    override fun decode(data: String): ByteArray? {
        return data.decodeToByteArrayOrNull(base16())
    }

    override fun encode(data: ByteArray): String {
        return data.encodeToString(base16())
    }

    @Test
    fun givenString_whenEncoded_MatchesRfc4648Spec() {
        checkEncodeSuccessForDataSet(encodeSuccessDataSet)
        useConstantTime = true
        checkEncodeSuccessForDataSet(encodeSuccessDataSet)
    }

    @Test
    fun givenBadEncoding_whenDecoded_ReturnsNull() {
        checkDecodeFailureForDataSet(decodeFailureDataSet)
        useConstantTime = true
        checkDecodeFailureForDataSet(decodeFailureDataSet)
    }

    @Test
    fun givenEncodedData_whenDecoded_MatchesRfc4648Spec() {
        checkDecodeSuccessForDataSet(decodeSuccessDataSet)
        useConstantTime = true
        checkDecodeSuccessForDataSet(decodeSuccessDataSet)
    }

    @Test
    fun givenUniversalDecoderParameters_whenChecked_areSuccessful() {
        checkUniversalDecoderParameters()
        useConstantTime = true
        checkUniversalDecoderParameters()
    }

    @Test
    fun givenUniversalEncoderParameters_whenChecked_areSuccessful() {
        checkUniversalEncoderParameters()
        useConstantTime = true
        checkUniversalEncoderParameters()
    }

    @Test
    fun givenBase16_whenEncodeToLowercase_thenOutputIsLowercase() {
        useLowercase = true

        val lowercaseData = buildSet {
            encodeSuccessDataSet.forEach { data ->
                val lowercase = data.expected.lowercase()
                add(data.copy(expected = lowercase))
            }
        }

        checkEncodeSuccessForDataSet(lowercaseData)
        useConstantTime = true
        checkEncodeSuccessForDataSet(lowercaseData)
    }

    @Test
    fun givenBase16_whenLowercaseAndUppercaseChars_thenMatch() {
        assertEquals(Base16.CHARS_UPPER, Base16.CHARS_LOWER.uppercase())
        assertEquals(Base16.CHARS_LOWER, Base16.CHARS_UPPER.lowercase())
    }

    @Test
    fun givenBase16_whenDecodeEncode_thenReturnsSameValue() {
        val expected = "54686520717569636b2062726f776e20666f78206a756d7073206f76657220746865206c617a7920646f672e"
        useLowercase = true
        listOf(false, true).forEach { ct ->
            useConstantTime = ct
            val encoder = base16()
            val decoded = expected.decodeToByteArray(encoder)
            val actual = decoded.encodeToString(encoder)
            assertEquals(expected, actual)
        }
    }

    @Test
    fun givenBase16_whenEncodeDecodeRandomData_thenBytesMatch() {
        checkRandomData()
        useConstantTime = true
        checkRandomData()
    }

    @Test
    fun givenBase16Lowercase_whenEncodeDecodeRandomData_thenBytesMatch() {
        useLowercase = true
        checkRandomData()
        useConstantTime = true
        checkRandomData()
    }
}
