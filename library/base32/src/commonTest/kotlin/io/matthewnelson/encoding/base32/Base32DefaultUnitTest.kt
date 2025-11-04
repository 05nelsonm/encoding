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
package io.matthewnelson.encoding.base32

import io.matthewnelson.encoding.core.Decoder.Companion.decodeToByteArray
import io.matthewnelson.encoding.core.Decoder.Companion.decodeToByteArrayOrNull
import io.matthewnelson.encoding.core.Encoder.Companion.encodeToCharArray
import io.matthewnelson.encoding.core.Encoder.Companion.encodeToString
import io.matthewnelson.encoding.test.BaseNEncodingTest
import kotlin.test.Test
import kotlin.test.assertEquals

class Base32DefaultUnitTest: BaseNEncodingTest() {

    private var useLowercase = false
    private var usePadding = true

    private fun base32(): Base32.Default = Base32.Default.Builder {
        encodeLowercase(useLowercase)
        padEncoded(usePadding)
    }

    override val decodeFailureDataSet: Set<Data<String, Any?>> = setOf(
        Data(raw = "A1", expected = null, message = "Character '1' should return null"),
        Data(raw = "A8", expected = null, message = "Character '8' should return null"),
        Data(raw = "A9", expected = null, message = "Character '9' should return null"),
    )

    override val decodeSuccessHelloWorld: Data<String, ByteArray> =
        Data(raw = "JBSWY3DPEBLW64TMMQQQ====", expected = "Hello World!".encodeToByteArray())

    override val decodeSuccessDataSet: Set<Data<String, ByteArray>> = setOf(
        decodeSuccessHelloWorld,
        Data(
            raw = "KNQWY5DFMRPV76DEF2232QQS4NFDHX7K33NZK5K3K7AWZVWK7ZU5DAO32PEYH5ERWNGCOHXOO5LBGXJYMBZAOHSHJ4AP5H6NWGRVGZI=",
            expected = ("53 61 6c 74 65 64 5f 5f f8 64 2e b5 bd 42 12 e3 4a 33 df ea de db 95 75 5b 57 c1 " +
                    "6c d6 ca fe 69 d1 81 db d3 c9 83 f4 91 b3 4c 27 1e ee 77 56 13 5d 38 60 72 07 1e 47 " +
                    "4f 00 fe 9f cd b1 a3 53 65").decodeHexToByteArray()
        ),
        Data(
            raw = "KNQWY5DFMRPV7RXZCOJOT3QZ3KFMCGGJNSPKK3O3V53JPXIV2LNFYV35OUOF42NPUWHGLQGHH2QIO===",
            expected = ("53 61 6c 74 65 64 5f 5f c6 f9 13 92 e9 ee 19 da 8a c1 18 c9 6c 9e a5 " +
                    "6d db af 76 97 dd 15 d2 da 5c 57 7d 75 1c 5e 69 af a5 8e 65 c0 c7 3e a0 " +
                    "87").decodeHexToByteArray()
        ),
        Data(
            raw = "KNQWY5DFMRPV744USBXPVHIQ6GY57YAPIAKQCOUX5QPUROTWVWUQ====",
            expected = ("53 61 6c 74 65 64 5f 5f f3 94 90 6e fa 9d 10 f1 b1 df e0 0f 40 15 01 " +
                    "3a 97 ec 1f 48 ba 76 ad a9").decodeHexToByteArray()
        ),
        Data(raw = "========", expected = ByteArray(0), message = "Decoding a String containing only padding '=' should return an empty ByteArray"),
        Data(raw = "MY======", expected = "f".encodeToByteArray()),
        Data(raw = "MY", expected = "f".encodeToByteArray(), message = "Stripped padding should decode"),
        Data(raw = "MZXQ====", expected = "fo".encodeToByteArray()),
        Data(raw = "MZXW6===", expected = "foo".encodeToByteArray()),
        Data(raw = "MZXW6IA=", expected = "foo ".encodeToByteArray()),
        Data(raw = "MZXW6IDC", expected = "foo b".encodeToByteArray()),
        Data(raw = "MZXW6IDCME======", expected = "foo ba".encodeToByteArray()),
        Data(raw = "MZXW6IDCMFZA====", expected = "foo bar".encodeToByteArray()),
        Data(raw = "IY======", expected = "F".encodeToByteArray()),
        Data(raw = "IZHQ====", expected = "FO".encodeToByteArray()),
        Data(raw = "IZHU6===", expected = "FOO".encodeToByteArray()),
        Data(raw = "IZHU6IA=", expected = "FOO ".encodeToByteArray()),
        Data(raw = "IZHU6ICC", expected = "FOO B".encodeToByteArray()),
        Data(raw = "IZHU6ICCIE======", expected = "FOO BA".encodeToByteArray()),
        Data(raw = "IZHU6ICCIFJA====", expected = "FOO BAR".encodeToByteArray()),
        Data(raw = "OI======", expected = "r".encodeToByteArray()),
        Data(raw = "OJQQ====", expected = "ra".encodeToByteArray()),
        Data(raw = "OJQWE===", expected = "rab".encodeToByteArray()),
        Data(raw = "OJQWEIA=", expected = "rab ".encodeToByteArray()),
        Data(raw = "OJQWEIDP", expected = "rab o".encodeToByteArray()),
        Data(raw = "OJQWEIDPN4======", expected = "rab oo".encodeToByteArray()),
        Data(raw = "OJQWEIDPN5TA====", expected = "rab oof".encodeToByteArray()),
        Data(raw = "KI======", expected = "R".encodeToByteArray()),
        Data(raw = "KJAQ====", expected = "RA".encodeToByteArray()),
        Data(raw = "KJAUE===", expected = "RAB".encodeToByteArray()),
        Data(raw = "KJAUEIA=", expected = "RAB ".encodeToByteArray()),
        Data(raw = "KJAUEICP", expected = "RAB O".encodeToByteArray()),
        Data(raw = "KJAUEICPJ4======", expected = "RAB OO".encodeToByteArray()),
        Data(raw = "KJAUEICPJ5DA====", expected = "RAB OOF".encodeToByteArray()),
        Data(raw = "kjaueicpj5da====", expected = "RAB OOF".encodeToByteArray()),
    )

    override val encodeSuccessDataSet: Set<Data<String, String>> = setOf(
        Data(raw = "Hello World!", expected = "JBSWY3DPEBLW64TMMQQQ===="),
        Data(raw = "f", expected = "MY======"),
        Data(raw = "fo", expected = "MZXQ===="),
        Data(raw = "foo", expected = "MZXW6==="),
        Data(raw = "foo ", expected = "MZXW6IA="),
        Data(raw = "foo b", expected = "MZXW6IDC"),
        Data(raw = "foo ba", expected = "MZXW6IDCME======"),
        Data(raw = "foo bar", expected = "MZXW6IDCMFZA===="),
        Data(raw = "F", expected = "IY======"),
        Data(raw = "FO", expected = "IZHQ===="),
        Data(raw = "FOO", expected = "IZHU6==="),
        Data(raw = "FOO ", expected = "IZHU6IA="),
        Data(raw = "FOO B", expected = "IZHU6ICC"),
        Data(raw = "FOO BA", expected = "IZHU6ICCIE======"),
        Data(raw = "FOO BAR", expected = "IZHU6ICCIFJA===="),
        Data(raw = "r", expected = "OI======"),
        Data(raw = "ra", expected = "OJQQ===="),
        Data(raw = "rab", expected = "OJQWE==="),
        Data(raw = "rab ", expected = "OJQWEIA="),
        Data(raw = "rab o", expected = "OJQWEIDP"),
        Data(raw = "rab oo", expected = "OJQWEIDPN4======"),
        Data(raw = "rab oof", expected = "OJQWEIDPN5TA===="),
        Data(raw = "R", expected = "KI======"),
        Data(raw = "RA", expected = "KJAQ===="),
        Data(raw = "RAB", expected = "KJAUE==="),
        Data(raw = "RAB ", expected = "KJAUEIA="),
        Data(raw = "RAB O", expected = "KJAUEICP"),
        Data(raw = "RAB OO", expected = "KJAUEICPJ4======"),
        Data(raw = "RAB OOF", expected = "KJAUEICPJ5DA===="),
    )

    override fun decode(data: String): ByteArray? {
        return data.decodeToByteArrayOrNull(base32())
    }
    override fun encode(data: ByteArray): String {
        // Use ToCharArray to ensure exact size calculations are correct
        return data.encodeToCharArray(base32()).joinToString("")
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
    fun givenBase32Default_whenPadEncodedFalse_thenDoesNotPadOutput() {
        usePadding = false

        val noPadData = buildSet {
            encodeSuccessDataSet.forEach { data ->
                val noPadding = data.expected.replace("=", "")
                add(data.copy(expected = noPadding))
            }
        }

        checkEncodeSuccessForDataSet(noPadData)
    }

    @Test
    fun givenBase32Default_whenEncodeToLowercase_thenOutputIsLowercase() {
        useLowercase = true

        val lowercaseData = buildSet {
            encodeSuccessDataSet.forEach { data ->
                val lowercase = data.expected.lowercase()
                add(data.copy(expected = lowercase))
            }
        }

        checkEncodeSuccessForDataSet(lowercaseData)
    }

    @Test
    fun givenBase32Default_whenLowercaseAndUppercaseChars_thenMatch() {
        assertEquals(Base32.Default.CHARS_UPPER, Base32.Default.CHARS_LOWER.uppercase())
        assertEquals(Base32.Default.CHARS_LOWER, Base32.Default.CHARS_UPPER.lowercase())
    }

    @Test
    fun givenBase32Default_whenDecodeEncode_thenReturnsSameValue() {
        val expected = "OBTDFCGTEKTGXPVR23DA7YFDEB5IZGLEHJH5GIIVBKGL5S2HNNRQ===="
        val encoder = base32()
        val decoded = expected.decodeToByteArray(encoder)
        val actual = decoded.encodeToString(encoder)
        assertEquals(expected, actual)
    }

    @Test
    fun givenBase32Default_whenEncodeDecodeRandomData_thenBytesMatch() {
        checkRandomData()
    }

    @Test
    fun givenBase32DefaultLowercase_whenEncodeDecodeRandomData_thenBytesMatch() {
        useLowercase = true
        checkRandomData()
    }
}
