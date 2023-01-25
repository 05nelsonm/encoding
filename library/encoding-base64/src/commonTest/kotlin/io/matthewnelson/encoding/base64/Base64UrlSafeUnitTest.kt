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

package io.matthewnelson.encoding.base64

import io.matthewnelson.encoding.test.BaseNEncodingTest
import io.matthewnelson.encoding.base64.Base64DefaultUnitTest.Companion.decodeHexToByteArray
import io.matthewnelson.encoding.builders.Base64
import io.matthewnelson.encoding.core.Decoder.Companion.decodeToByteArrayOrNull
import io.matthewnelson.encoding.core.Encoder.Companion.encodeToString
import kotlin.test.Test

class Base64UrlSafeUnitTest: BaseNEncodingTest() {

    private var base64UrlSafe: Base64 = Base64 { encodeToUrlSafe = true }

    override val decodeFailureDataSet: Set<Data<String, Any?>> = setOf(
        Data("SGVsbG8gV29ybGQ^", expected = null, message = "Character '^' should return null")
    )

    override val decodeSuccessHelloWorld: Data<String, ByteArray> =
        Data(raw = "SGVsbG8gV29ybGQh", expected = "Hello World!".encodeToByteArray())

    override val decodeSuccessDataSet: Set<Data<String, ByteArray>> = setOf(
        decodeSuccessHelloWorld,
        Data(
            raw = "U2FsdGVkX1_4ZC61vUIS40oz3-re25V1W1fBbNbK_mnRgdvTyYP0kbNMJx7ud1YTXThgcgceR08A_p_NsaNTZQ==",
            expected = ("53 61 6c 74 65 64 5f 5f f8 64 2e b5 bd 42 12 e3 4a 33 df ea de db 95 " +
                    "75 5b 57 c1 6c d6 ca fe 69 d1 81 db d3 c9 83 f4 91 b3 4c 27 1e ee 77 56 " +
                    "13 5d 38 60 72 07 1e 47 4f 00 fe 9f cd b1 a3 53 65").decodeHexToByteArray()
        ),
        Data(
            raw = "U2FsdGVkX1_G-ROS6e4Z2orBGMlsnqVt2692l90V0tpcV311HF5pr6WOZcDHPqCH",
            expected = ("53 61 6c 74 65 64 5f 5f c6 f9 13 92 e9 ee 19 da 8a c1 18 c9 6c 9e a5 " +
                    "6d db af 76 97 dd 15 d2 da 5c 57 7d 75 1c 5e 69 af a5 8e 65 c0 c7 3e a0 " +
                    "87").decodeHexToByteArray()
        ),
        Data(
            raw = "U2FsdGVkX1_zlJBu-p0Q8bHf4A9AFQE6l-wfSLp2rak=",
            expected = ("53 61 6c 74 65 64 5f 5f f3 94 90 6e fa 9d 10 f1 b1 df e0 0f 40 15 01 " +
                    "3a 97 ec 1f 48 ba 76 ad a9").decodeHexToByteArray()
        ),
        Data(raw = "======", expected = ByteArray(0), message = "Decoding a String containing only padding '=' should return an empty ByteArray"),
        Data(raw = "Zg==", expected = "f".encodeToByteArray()),
        Data(raw = "Zm8=", expected = "fo".encodeToByteArray()),
        Data(raw = "Zm9v", expected = "foo".encodeToByteArray()),
        Data(raw = "Zm9vIA==", expected = "foo ".encodeToByteArray()),
        Data(raw = "Zm9vIGI=", expected = "foo b".encodeToByteArray()),
        Data(raw = "Zm9vIGJh", expected = "foo ba".encodeToByteArray()),
        Data(raw = "Zm9vIGJhcg==", expected = "foo bar".encodeToByteArray()),
        Data(raw = "Rg==", expected = "F".encodeToByteArray()),
        Data(raw = "Rk8=", expected = "FO".encodeToByteArray()),
        Data(raw = "Rk9P", expected = "FOO".encodeToByteArray()),
        Data(raw = "Rk9PIA==", expected = "FOO ".encodeToByteArray()),
        Data(raw = "Rk9PIEI=", expected = "FOO B".encodeToByteArray()),
        Data(raw = "Rk9PIEJB", expected = "FOO BA".encodeToByteArray()),
        Data(raw = "Rk9PIEJBUg==", expected = "FOO BAR".encodeToByteArray()),
        Data(raw = "cg==", expected = "r".encodeToByteArray()),
        Data(raw = "cmE=", expected = "ra".encodeToByteArray()),
        Data(raw = "cmFi", expected = "rab".encodeToByteArray()),
        Data(raw = "cmFiIA==", expected = "rab ".encodeToByteArray()),
        Data(raw = "cmFiIG8=", expected = "rab o".encodeToByteArray()),
        Data(raw = "cmFiIG9v", expected = "rab oo".encodeToByteArray()),
        Data(raw = "cmFiIG9vZg==", expected = "rab oof".encodeToByteArray()),
        Data(raw = "Ug==", expected = "R".encodeToByteArray()),
        Data(raw = "UkE=", expected = "RA".encodeToByteArray()),
        Data(raw = "UkFC", expected = "RAB".encodeToByteArray()),
        Data(raw = "UkFCIA==", expected = "RAB ".encodeToByteArray()),
        Data(raw = "UkFCIE8=", expected = "RAB O".encodeToByteArray()),
        Data(raw = "UkFCIE9P", expected = "RAB OO".encodeToByteArray()),
        Data(raw = "UkFCIE9PRg==", expected = "RAB OOF".encodeToByteArray()),
    )

    private fun getDecodeSuccessDataSetWithoutPadding(): Set<Data<String, ByteArray>> {
        val newSet: MutableSet<Data<String, ByteArray>> = LinkedHashSet(decodeSuccessDataSet.size)

        for (data in decodeSuccessDataSet) {
            newSet.add(
                Data(
                    raw = data.raw.dropLastWhile { it == '=' },
                    expected = data.expected
                )
            )
        }

        return newSet
    }

    override val encodeSuccessDataSet: Set<Data<String, String>> = setOf(
        Data(raw = "Hello World!", expected = "SGVsbG8gV29ybGQh"),
        Data(raw = "f", expected = "Zg=="),
        Data(raw = "fo", expected = "Zm8="),
        Data(raw = "foo", expected = "Zm9v"),
        Data(raw = "foo ", expected = "Zm9vIA=="),
        Data(raw = "foo b", expected = "Zm9vIGI="),
        Data(raw = "foo ba", expected = "Zm9vIGJh"),
        Data(raw = "foo bar", expected = "Zm9vIGJhcg=="),
        Data(raw = "F", expected = "Rg=="),
        Data(raw = "FO", expected = "Rk8="),
        Data(raw = "FOO", expected = "Rk9P"),
        Data(raw = "FOO ", expected = "Rk9PIA=="),
        Data(raw = "FOO B", expected = "Rk9PIEI="),
        Data(raw = "FOO BA", expected = "Rk9PIEJB"),
        Data(raw = "FOO BAR", expected = "Rk9PIEJBUg=="),
        Data(raw = "r", expected = "cg=="),
        Data(raw = "ra", expected = "cmE="),
        Data(raw = "rab", expected = "cmFi"),
        Data(raw = "rab ", expected = "cmFiIA=="),
        Data(raw = "rab o", expected = "cmFiIG8="),
        Data(raw = "rab oo", expected = "cmFiIG9v"),
        Data(raw = "rab oof", expected = "cmFiIG9vZg=="),
        Data(raw = "R", expected = "Ug=="),
        Data(raw = "RA", expected = "UkE="),
        Data(raw = "RAB", expected = "UkFC"),
        Data(raw = "RAB ", expected = "UkFCIA=="),
        Data(raw = "RAB O", expected = "UkFCIE8="),
        Data(raw = "RAB OO", expected = "UkFCIE9P"),
        Data(raw = "RAB OOF", expected = "UkFCIE9PRg=="),
    )

    override fun decode(data: String): ByteArray? {
        return data.decodeToByteArrayOrNull(base64UrlSafe)
    }

    override fun encode(data: ByteArray): String {
        return data.encodeToString(base64UrlSafe)
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
    fun givenString_whenEncodedWithoutPaddingExpressed_returnsExpected() {
        base64UrlSafe = Base64 {
            encodeToUrlSafe = true
            padEncoded = false
        }

        checkDecodeSuccessForDataSet(
            getDecodeSuccessDataSetWithoutPadding()
        )
    }

    @Test
    fun givenUniversalDecoderParameters_whenChecked_areSuccessful() {
        checkUniversalDecoderParameters()
    }

    @Test
    fun givenUniversalEncoderParameters_whenChecked_areSuccessful() {
        checkUniversalEncoderParameters()
    }

}
