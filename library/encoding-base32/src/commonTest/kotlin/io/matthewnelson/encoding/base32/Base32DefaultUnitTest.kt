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

import io.matthewnelson.encoding.builders.Base32Default
import io.matthewnelson.encoding.core.Decoder.Companion.decodeToByteArrayOrNull
import io.matthewnelson.encoding.core.Encoder.Companion.encodeToString
import io.matthewnelson.encoding.test.BaseNEncodingTest
import kotlin.test.Test
import kotlin.test.assertEquals

class Base32DefaultUnitTest: BaseNEncodingTest() {

    override val decodeFailureDataSet: Set<Data<String, Any?>> = setOf(
        Data(raw = "A1", expected = null, message = "Character '1' should return null"),
        Data(raw = "A8", expected = null, message = "Character '8' should return null"),
        Data(raw = "A9", expected = null, message = "Character '9' should return null"),
    )

    override val decodeSuccessHelloWorld: Data<String, ByteArray> =
        Data(raw = "JBSWY3DPEBLW64TMMQQQ====", expected = "Hello World!".encodeToByteArray())

    override val decodeSuccessDataSet: Set<Data<String, ByteArray>> = setOf(
        decodeSuccessHelloWorld,
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
        return data.decodeToByteArrayOrNull(Base32Default())
    }
    override fun encode(data: ByteArray): String {
        return data.encodeToString(Base32Default())
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
    fun givenBase32Default_whenLowercaseAndUppercaseChars_thenMatch() {
        assertEquals(Base32.Default.CHARS_UPPER, Base32.Default.CHARS_LOWER.uppercase())
        assertEquals(Base32.Default.CHARS_LOWER, Base32.Default.CHARS_UPPER.lowercase())
    }
}
