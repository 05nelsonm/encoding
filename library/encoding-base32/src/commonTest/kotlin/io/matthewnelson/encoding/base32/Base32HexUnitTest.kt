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

import io.matthewnelson.encoding.builders.Base32Hex
import io.matthewnelson.encoding.core.Decoder.Companion.decodeToByteArray
import io.matthewnelson.encoding.core.Decoder.Companion.decodeToByteArrayOrNull
import io.matthewnelson.encoding.core.Encoder.Companion.encodeToString
import io.matthewnelson.encoding.test.BaseNEncodingTest
import kotlin.test.Test
import kotlin.test.assertEquals

class Base32HexUnitTest: BaseNEncodingTest() {

    private var base32Hex = Base32Hex()

    override val decodeFailureDataSet: Set<Data<String, Any?>> = setOf(
        Data(raw = "AW", expected = null, message = "Character 'W' should return null"),
        Data(raw = "AX", expected = null, message = "Character 'X' should return null"),
        Data(raw = "AY", expected = null, message = "Character 'Y' should return null"),
        Data(raw = "AZ", expected = null, message = "Character 'Z' should return null"),
    )

    override val decodeSuccessHelloWorld: Data<String, ByteArray> =
        Data(raw = "91IMOR3F41BMUSJCCGGG====", expected = "Hello World!".encodeToByteArray())

    override val decodeSuccessDataSet: Set<Data<String, ByteArray>> = setOf(
        decodeSuccessHelloWorld,
        Data(
            raw = "ADGMOT35CHFLVU345QQRQGGISD537NVARRDPATARAV0MPLMAVPKT30ERQF4O7T4HMD62E7NEETB16N9OC1P0E7I79S0FT7UDM6HL6P8=",
            expected = ("53 61 6c 74 65 64 5f 5f f8 64 2e b5 bd 42 12 e3 4a 33 df ea de db 95 75 5b 57 c1 " +
                    "6c d6 ca fe 69 d1 81 db d3 c9 83 f4 91 b3 4c 27 1e ee 77 56 13 5d 38 60 72 07 1e 47 " +
                    "4f 00 fe 9f cd b1 a3 53 65").decodeHexToByteArray()
        ),
        Data(
            raw = "ADGMOT35CHFLVHNP2E9EJRGPRA5C2669DIFAARERLTR9FN8LQBD5OLRTEKE5SQDFKM76BG677QG8E===",
            expected = ("53 61 6c 74 65 64 5f 5f c6 f9 13 92 e9 ee 19 da 8a c1 18 c9 6c 9e a5 " +
                    "6d db af 76 97 dd 15 d2 da 5c 57 7d 75 1c 5e 69 af a5 8e 65 c0 c7 3e a0 " +
                    "87").decodeHexToByteArray()
        ),
        Data(
            raw = "ADGMOT35CHFLVSSKI1NFL78GU6OTVO0F80AG2EKNTGFKHEJMLMKG====",
            expected = ("53 61 6c 74 65 64 5f 5f f3 94 90 6e fa 9d 10 f1 b1 df e0 0f 40 15 01 " +
                    "3a 97 ec 1f 48 ba 76 ad a9").decodeHexToByteArray()
        ),
        Data(raw = "======", expected = ByteArray(0), message = "Decoding a String containing only padding '=' should return an empty ByteArray"),
        Data(raw = "CO======", expected = "f".encodeToByteArray()),
        Data(raw = "CO", expected = "f".encodeToByteArray(), message = "Stripped padding should decode"),
        Data(raw = "CPNG====", expected = "fo".encodeToByteArray()),
        Data(raw = "CPNMU===", expected = "foo".encodeToByteArray()),
        Data(raw = "CPNMU80=", expected = "foo ".encodeToByteArray()),
        Data(raw = "CPNMU832", expected = "foo b".encodeToByteArray()),
        Data(raw = "CPNMU832C4======", expected = "foo ba".encodeToByteArray()),
        Data(raw = "CPNMU832C5P0====", expected = "foo bar".encodeToByteArray()),
        Data(raw = "8O======", expected = "F".encodeToByteArray()),
        Data(raw = "8P7G====", expected = "FO".encodeToByteArray()),
        Data(raw = "8P7KU===", expected = "FOO".encodeToByteArray()),
        Data(raw = "8P7KU80=", expected = "FOO ".encodeToByteArray()),
        Data(raw = "8P7KU822", expected = "FOO B".encodeToByteArray()),
        Data(raw = "8P7KU82284======", expected = "FOO BA".encodeToByteArray()),
        Data(raw = "8P7KU8228590====", expected = "FOO BAR".encodeToByteArray()),
        Data(raw = "E8======", expected = "r".encodeToByteArray()),
        Data(raw = "E9GG====", expected = "ra".encodeToByteArray()),
        Data(raw = "E9GM4===", expected = "rab".encodeToByteArray()),
        Data(raw = "E9GM480=", expected = "rab ".encodeToByteArray()),
        Data(raw = "E9GM483F", expected = "rab o".encodeToByteArray()),
        Data(raw = "E9GM483FDS======", expected = "rab oo".encodeToByteArray()),
        Data(raw = "E9GM483FDTJ0====", expected = "rab oof".encodeToByteArray()),
        Data(raw = "A8======", expected = "R".encodeToByteArray()),
        Data(raw = "A90G====", expected = "RA".encodeToByteArray()),
        Data(raw = "A90K4===", expected = "RAB".encodeToByteArray()),
        Data(raw = "A90K480=", expected = "RAB ".encodeToByteArray()),
        Data(raw = "A90K482F", expected = "RAB O".encodeToByteArray()),
        Data(raw = "A90K482F9S======", expected = "RAB OO".encodeToByteArray()),
        Data(raw = "A90K482F9T30====", expected = "RAB OOF".encodeToByteArray()),
        Data(raw = "a90k482f9t30====", expected = "RAB OOF".encodeToByteArray()),
    )

    override val encodeSuccessDataSet: Set<Data<String, String>> = setOf(
        Data(raw = "Hello World!", expected = "91IMOR3F41BMUSJCCGGG===="),
        Data(raw = "f", expected = "CO======"),
        Data(raw = "fo", expected = "CPNG===="),
        Data(raw = "foo", expected = "CPNMU==="),
        Data(raw = "foo ", expected = "CPNMU80="),
        Data(raw = "foo b", expected = "CPNMU832"),
        Data(raw = "foo ba", expected = "CPNMU832C4======"),
        Data(raw = "foo bar", expected = "CPNMU832C5P0===="),
        Data(raw = "F", expected = "8O======"),
        Data(raw = "FO", expected = "8P7G===="),
        Data(raw = "FOO", expected = "8P7KU==="),
        Data(raw = "FOO ", expected = "8P7KU80="),
        Data(raw = "FOO B", expected = "8P7KU822"),
        Data(raw = "FOO BA", expected = "8P7KU82284======"),
        Data(raw = "FOO BAR", expected = "8P7KU8228590===="),
        Data(raw = "r", expected = "E8======"),
        Data(raw = "ra", expected = "E9GG===="),
        Data(raw = "rab", expected = "E9GM4==="),
        Data(raw = "rab ", expected = "E9GM480="),
        Data(raw = "rab o", expected = "E9GM483F"),
        Data(raw = "rab oo", expected = "E9GM483FDS======"),
        Data(raw = "rab oof", expected = "E9GM483FDTJ0===="),
        Data(raw = "R", expected = "A8======"),
        Data(raw = "RA", expected = "A90G===="),
        Data(raw = "RAB", expected = "A90K4==="),
        Data(raw = "RAB ", expected = "A90K480="),
        Data(raw = "RAB O", expected = "A90K482F"),
        Data(raw = "RAB OO", expected = "A90K482F9S======"),
        Data(raw = "RAB OOF", expected = "A90K482F9T30===="),
    )

    override fun decode(data: String): ByteArray? {
        return data.decodeToByteArrayOrNull(base32Hex)
    }
    override fun encode(data: ByteArray): String {
        return data.encodeToString(base32Hex)
    }

    @Test
    fun givenString_whenEncoded_MatchesRfc4648Spec() {
        checkEncodeSuccessForDataSet(encodeSuccessDataSet)
    }

    @Test
    fun givenBadEncoding_whenDecoded_ReturnsNull() {
        checkDecodeFailureForDataSet(decodeFailureDataSet)
    }

    @Test
    fun givenEncodedData_whenDecoded_MatchesRfc4648Spec() {
        checkDecodeSuccessForDataSet(decodeSuccessDataSet)
    }

    @Test
    fun givenUniversalDecoderParameters_whenChecked_areSuccessful() {
        checkUniversalDecoderParameters()
    }

    @Test
    fun givenUniversalEncoderParameters_whenChecked_areSuccessful() {
        checkUniversalEncoderParameters()
    }

    @Test
    fun givenBase32Hex_whenPadEncodedFalse_thenDoesNotPadOutput() {
        base32Hex = Base32Hex {
            padEncoded = false
        }

        val noPadData = buildSet {
            encodeSuccessDataSet.forEach { data ->
                val noPadding = data.expected.replace("=", "")
                add(data.copy(expected = noPadding))
            }
        }

        checkEncodeSuccessForDataSet(noPadData)
    }

    @Test
    fun givenBase32Hex_whenEncodeToLowercase_thenOutputIsLowercase() {
        base32Hex = Base32Hex {
            encodeToLowercase = true
        }

        val lowercaseData = buildSet {
            encodeSuccessDataSet.forEach { data ->
                val lowercase = data.expected.lowercase()
                add(data.copy(expected = lowercase))
            }
        }

        checkEncodeSuccessForDataSet(lowercaseData)
    }

    @Test
    fun givenBase32Hex_whenLowercaseAndUppercaseChars_thenMatch() {
        assertEquals(Base32.Hex.CHARS_UPPER, Base32.Hex.CHARS_LOWER.uppercase())
        assertEquals(Base32.Hex.CHARS_LOWER, Base32.Hex.CHARS_UPPER.lowercase())
    }

    @Test
    fun givenBase32Hex_whenDecodeEncode_thenReturnsSameValue() {
        val expected = "AHK6A83HELKM6QP0C9P6UTRE41J6UU10D9QMQS3J41NNCPBI41Q6GP90DHGNKU90CHNMEBG="
        val decoded = expected.decodeToByteArray(base32Hex)
        val rencoded = decoded.encodeToString(base32Hex)
        assertEquals(expected, rencoded)
    }
}
