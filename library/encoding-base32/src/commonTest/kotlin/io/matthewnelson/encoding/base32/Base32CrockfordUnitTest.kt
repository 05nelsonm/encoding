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

import io.matthewnelson.encoding.builders.Base32Crockford
import io.matthewnelson.encoding.core.Decoder.Companion.decodeToByteArray
import io.matthewnelson.encoding.core.Decoder.Companion.decodeToByteArrayOrNull
import io.matthewnelson.encoding.core.Encoder.Companion.encodeToString
import io.matthewnelson.encoding.test.BaseNEncodingTest
import kotlin.test.*

class Base32CrockfordUnitTest: BaseNEncodingTest() {

    private var crockford: Base32.Crockford = Base32Crockford { checkSymbol(null) }
    private val validCheckSymbols = listOf('*', '~', '$', '=', 'U', 'u')

    override val decodeFailureDataSet: Set<Data<String, Any?>> = setOf(
        Data(raw = "91JPRV3F41BPYWKCCGGG====", expected = null, message = "Typical padding character '=' should return null"),
        Data(raw = "AUU", expected = null, message = "Character 'U' should return null"),
        Data(raw = "auu", expected = null, message = "Character 'u' should return null"),
        Data(raw = "UA", expected = null, message = "Character 'U' should return null"),
        Data(raw = "ua", expected = null, message = "Character 'u' should return null"),
    )

    companion object {
        const val MESSAGE_LOWERCASE = "Lowercase characters should be accepted"
        const val MESSAGE_HYPHEN = "Hyphens '-' should be ignored"
    }

    override val decodeSuccessHelloWorld: Data<String, ByteArray> =
        Data("91JPRV3F41BPYWKCCGGG", "Hello World!".encodeToByteArray())

    override val decodeSuccessDataSet: Set<Data<String, ByteArray>> = setOf(
        decodeSuccessHelloWorld,
        Data(
            raw = "ADGPRX35CHFNZY345TTVTGGJWD537QZAVVDSAXAVAZ0PSNPAZSMX30EVTF4R7X4HPD62E7QEEXB16Q9RC1S0E7J79W0FX7YDP6HN6S8",
            expected = ("53 61 6c 74 65 64 5f 5f f8 64 2e b5 bd 42 12 e3 4a 33 df ea de db 95 75 5b 57 c1 " +
                    "6c d6 ca fe 69 d1 81 db d3 c9 83 f4 91 b3 4c 27 1e ee 77 56 13 5d 38 60 72 07 1e 47 " +
                    "4f 00 fe 9f cd b1 a3 53 65").decodeHexToByteArray()
        ),
        Data(
            raw = "ADGPRX35CHFNZHQS2E9EKVGSVA5C2669DJFAAVEVNXV9FQ8NTBD5RNVXEME5WTDFMP76BG677TG8E",
            expected = ("53 61 6c 74 65 64 5f 5f c6 f9 13 92 e9 ee 19 da 8a c1 18 c9 6c 9e a5 " +
                    "6d db af 76 97 dd 15 d2 da 5c 57 7d 75 1c 5e 69 af a5 8e 65 c0 c7 3e a0 " +
                    "87").decodeHexToByteArray()
        ),
        Data(
            raw = "ADGPRX35CHFNZWWMJ1QFN78GY6RXZR0F80AG2EMQXGFMHEKPNPMG",
            expected = ("53 61 6c 74 65 64 5f 5f f3 94 90 6e fa 9d 10 f1 b1 df e0 0f 40 15 01 " +
                    "3a 97 ec 1f 48 ba 76 ad a9").decodeHexToByteArray()
        ),
        Data(raw = " 91JP RV3F41B PYW KCC GGG  ", expected = "Hello World!".encodeToByteArray(), message = "Spaces ' ' should be ignored"),
        Data(raw = "-----", expected = ByteArray(0), message = "Decoding a String containing only hyphens '-' should return an empty ByteArray"),

        Data(raw = "CR", expected = "f".encodeToByteArray()),
        Data(raw = "cR", expected = "f".encodeToByteArray(), message = MESSAGE_LOWERCASE),
        Data(raw = "c-R", expected = "f".encodeToByteArray(), message = MESSAGE_HYPHEN),

        Data(raw = "CSQG", expected = "fo".encodeToByteArray()),
        Data(raw = "csQG", expected = "fo".encodeToByteArray(), message = MESSAGE_LOWERCASE),
        Data(raw = "-csQG-", expected = "fo".encodeToByteArray(), message = MESSAGE_HYPHEN),

        Data(raw = "CSQPY", expected = "foo".encodeToByteArray()),
        Data(raw = "CSQpy", expected = "foo".encodeToByteArray(), message = MESSAGE_LOWERCASE),
        Data(raw = "-C-s-Q-p-Y-", expected = "foo".encodeToByteArray(), message = MESSAGE_HYPHEN),

        Data(raw = "CSQPY80", expected = "foo ".encodeToByteArray()),
        Data(raw = "cSQPy80", expected = "foo ".encodeToByteArray(), message = MESSAGE_LOWERCASE),
        Data(raw = "C-SqPY80-", expected = "foo ".encodeToByteArray(), message = MESSAGE_HYPHEN),

        Data(raw = "CSQPY832", expected = "foo b".encodeToByteArray()),
        Data(raw = "CSQpY832", expected = "foo b".encodeToByteArray(), message = MESSAGE_LOWERCASE),
        Data(raw = "-CsQPY832", expected = "foo b".encodeToByteArray(), message = MESSAGE_HYPHEN),

        Data(raw = "CSQPY832C4", expected = "foo ba".encodeToByteArray()),
        Data(raw = "CSqPy832c4", expected = "foo ba".encodeToByteArray(), message = MESSAGE_LOWERCASE),
        Data(raw = "C-sQPY8-32C4-", expected = "foo ba".encodeToByteArray(), message = MESSAGE_HYPHEN),

        Data(raw = "CSQPY832C5S0", expected = "foo bar".encodeToByteArray()),
        Data(raw = "CSQPy832C5s0", expected = "foo bar".encodeToByteArray(), message = MESSAGE_LOWERCASE),
        Data(raw = "cS-QP-Y8-32-c5-S0", expected = "foo bar".encodeToByteArray(), message = MESSAGE_HYPHEN),

        Data(raw = "8R", expected = "F".encodeToByteArray()),
        Data(raw = "8r", expected = "F".encodeToByteArray(), message = MESSAGE_LOWERCASE),
        Data(raw = "8R-", expected = "F".encodeToByteArray(), message = MESSAGE_HYPHEN),

        Data(raw = "8S7G", expected = "FO".encodeToByteArray()),
        Data(raw = "8S7g", expected = "FO".encodeToByteArray(), message = MESSAGE_LOWERCASE),
        Data(raw = "8s-7G", expected = "FO".encodeToByteArray(), message = MESSAGE_HYPHEN),

        Data(raw = "8S7MY", expected = "FOO".encodeToByteArray()),
        Data(raw = "8S7my", expected = "FOO".encodeToByteArray(), message = MESSAGE_LOWERCASE),
        Data(raw = "8S-7M-Y", expected = "FOO".encodeToByteArray(), message = MESSAGE_HYPHEN),

        Data(raw = "8S7MY80", expected = "FOO ".encodeToByteArray()),
        Data(raw = "8S7mY80", expected = "FOO ".encodeToByteArray(), message = MESSAGE_LOWERCASE),
        Data(raw = "8s-7M-Y80", expected = "FOO ".encodeToByteArray(), message = MESSAGE_HYPHEN),

        Data(raw = "8S7MY822", expected = "FOO B".encodeToByteArray()),
        Data(raw = "8s7My822", expected = "FOO B".encodeToByteArray(), message = MESSAGE_LOWERCASE),
        Data(raw = "-8S-7M-Y8-22-", expected = "FOO B".encodeToByteArray(), message = MESSAGE_HYPHEN),

        Data(raw = "8S7MY82284", expected = "FOO BA".encodeToByteArray()),
        Data(raw = "8S7My82284", expected = "FOO BA".encodeToByteArray(), message = MESSAGE_LOWERCASE),
        Data(raw = "8S-7m-Y82284-", expected = "FOO BA".encodeToByteArray(), message = MESSAGE_HYPHEN),

        Data(raw = "8S7MY8228590", expected = "FOO BAR".encodeToByteArray()),
        Data(raw = "8s7My8228590", expected = "FOO BAR".encodeToByteArray(), message = MESSAGE_LOWERCASE),
        Data(raw = "-8S-7M-Y8-22-85-90-", expected = "FOO BAR".encodeToByteArray(), message = MESSAGE_HYPHEN),

        Data(raw = "E8", expected = "r".encodeToByteArray()),
        Data(raw = "e8", expected = "r".encodeToByteArray(), message = MESSAGE_LOWERCASE),
        Data(raw = "E-8", expected = "r".encodeToByteArray(), message = MESSAGE_HYPHEN),

        Data(raw = "E9GG", expected = "ra".encodeToByteArray()),
        Data(raw = "E9gG", expected = "ra".encodeToByteArray(), message = MESSAGE_LOWERCASE),
        Data(raw = "E9G-G-", expected = "ra".encodeToByteArray(), message = MESSAGE_HYPHEN),

        Data(raw = "E9GP4", expected = "rab".encodeToByteArray()),
        Data(raw = "E9Gp4", expected = "rab".encodeToByteArray(), message = MESSAGE_LOWERCASE),
        Data(raw = "E9---GP4", expected = "rab".encodeToByteArray(), message = MESSAGE_HYPHEN),

        Data(raw = "E9GP480", expected = "rab ".encodeToByteArray()),
        Data(raw = "e9gp480", expected = "rab ".encodeToByteArray(), message = MESSAGE_LOWERCASE),
        Data(raw = "---E9GP480---", expected = "rab ".encodeToByteArray(), message = MESSAGE_HYPHEN),

        Data(raw = "E9GP483F", expected = "rab o".encodeToByteArray()),
        Data(raw = "E9GP483f", expected = "rab o".encodeToByteArray(), message = MESSAGE_LOWERCASE),
        Data(raw = "--E9Gp--483F--", expected = "rab o".encodeToByteArray(), message = MESSAGE_HYPHEN),

        Data(raw = "E9GP483FDW", expected = "rab oo".encodeToByteArray()),
        Data(raw = "E9GP483fdw", expected = "rab oo".encodeToByteArray(), message = MESSAGE_LOWERCASE),
        Data(raw = "E9-GP-48-3F-DW", expected = "rab oo".encodeToByteArray(), message = MESSAGE_HYPHEN),

        Data(raw = "E9GP483FDXK0", expected = "rab oof".encodeToByteArray()),
        Data(raw = "E9GP483fDxK0", expected = "rab oof".encodeToByteArray(), message = MESSAGE_LOWERCASE),
        Data(raw = "-E9gP483FDXK0-", expected = "rab oof".encodeToByteArray(), message = MESSAGE_HYPHEN),

        Data(raw = "A8", expected = "R".encodeToByteArray()),
        Data(raw = "a8", expected = "R".encodeToByteArray(), message = MESSAGE_LOWERCASE),
        Data(raw = "A-8", expected = "R".encodeToByteArray(), message = MESSAGE_HYPHEN),

        Data(raw = "A90G", expected = "RA".encodeToByteArray()),
        Data(raw = "A90g", expected = "RA".encodeToByteArray(), message = MESSAGE_LOWERCASE),
        Data(raw = "A90G-", expected = "RA".encodeToByteArray(), message = MESSAGE_HYPHEN),

        Data(raw = "A90M4", expected = "RAB".encodeToByteArray()),
        Data(raw = "A90m4", expected = "RAB".encodeToByteArray(), message = MESSAGE_LOWERCASE),
        Data(raw = "A90m-4", expected = "RAB".encodeToByteArray(), message = MESSAGE_HYPHEN),

        Data(raw = "A90M480 ", expected = "RAB ".encodeToByteArray()),
        Data(raw = "a90m480 ", expected = "RAB ".encodeToByteArray(), message = MESSAGE_LOWERCASE),
        Data(raw = "A-90M-480 ", expected = "RAB ".encodeToByteArray(), message = MESSAGE_HYPHEN),

        Data(raw = "A90M482F", expected = "RAB O".encodeToByteArray()),
        Data(raw = "a90m482f", expected = "RAB O".encodeToByteArray(), message = MESSAGE_LOWERCASE),
        Data(raw = "-A-9-0-M-4-8-2-F-", expected = "RAB O".encodeToByteArray(), message = MESSAGE_HYPHEN),

        Data(raw = "A90M482F9W", expected = "RAB OO".encodeToByteArray()),
        Data(raw = "A90M482f9w", expected = "RAB OO".encodeToByteArray(), message = MESSAGE_LOWERCASE),
        Data(raw = "A90M-482F9W", expected = "RAB OO".encodeToByteArray(), message = MESSAGE_HYPHEN),

        Data(raw = "A90M482F9X30", expected = "RAB OOF".encodeToByteArray()),
        Data(raw = "a90m482f9x30", expected = "RAB OOF".encodeToByteArray(), message = MESSAGE_LOWERCASE),
        Data(raw = "-A9-0M-48-2F-9X-30", expected = "RAB OOF".encodeToByteArray(), message = MESSAGE_HYPHEN),
    )

    private fun getDecodeSuccessDataSetWithCheckSymbolExpected(checkSymbol: Char): Set<Data<String, ByteArray>> {
        check(validCheckSymbols.contains(checkSymbol)) { "$checkSymbol is not valid" }

        val newSet: MutableSet<Data<String, ByteArray>> = LinkedHashSet(decodeSuccessDataSet.size)
        for (data in decodeSuccessDataSet) {
            newSet.add(
                Data(
                    // add symbol to end of decode string data
                    raw = data.raw + checkSymbol,
                    expected = data.expected,
                )
            )
        }

        return newSet
    }

    override val encodeSuccessDataSet: Set<Data<String, String>> = setOf(
        Data(raw = "Hello World!", expected = "91JPRV3F41BPYWKCCGGG"),
        Data(raw = "f", expected = "CR"),
        Data(raw = "fo", expected = "CSQG"),
        Data(raw = "foo", expected = "CSQPY"),
        Data(raw = "foo ", expected = "CSQPY80"),
        Data(raw = "foo b", expected = "CSQPY832"),
        Data(raw = "foo ba", expected = "CSQPY832C4"),
        Data(raw = "foo bar", expected = "CSQPY832C5S0"),
        Data(raw = "F", expected = "8R"),
        Data(raw = "FO", expected = "8S7G"),
        Data(raw = "FOO", expected = "8S7MY"),
        Data(raw = "FOO ", expected = "8S7MY80"),
        Data(raw = "FOO B", expected = "8S7MY822"),
        Data(raw = "FOO BA", expected = "8S7MY82284"),
        Data(raw = "FOO BAR", expected = "8S7MY8228590"),
        Data(raw = "r", expected = "E8"),
        Data(raw = "ra", expected = "E9GG"),
        Data(raw = "rab", expected = "E9GP4"),
        Data(raw = "rab ", expected = "E9GP480"),
        Data(raw = "rab o", expected = "E9GP483F"),
        Data(raw = "rab oo", expected = "E9GP483FDW"),
        Data(raw = "rab oof", expected = "E9GP483FDXK0"),
        Data(raw = "R", expected = "A8"),
        Data(raw = "RA", expected = "A90G"),
        Data(raw = "RAB", expected = "A90M4"),
        Data(raw = "RAB ", expected = "A90M480"),
        Data(raw = "RAB O", expected = "A90M482F"),
        Data(raw = "RAB OO", expected = "A90M482F9W"),
        Data(raw = "RAB OOF", expected = "A90M482F9X30"),
    )

    private fun getEncodeSuccessDataSetWithCheckSymbolExpected(checkSymbol: Char): Set<Data<String, String>> {
        check(validCheckSymbols.contains(checkSymbol)) { "$checkSymbol is not valid" }

        val newSet: MutableSet<Data<String, String>> = LinkedHashSet(encodeSuccessDataSet.size)
        for (data in encodeSuccessDataSet) {
            newSet.add(
                Data(
                    raw = data.raw,

                    // Add symbol to end of expected decode string

                    // Check symbol 'u' can be decoded as is (lowercase), but
                    // if 'u' is expressed as check symbol during encoding, it must
                    // always be encoded as uppercase 'U'. So, here the expected
                    // output (encoded string) should always be uppercase 'U'.
                    expected = data.expected + checkSymbol.uppercaseChar(),
                )
            )
        }

        return newSet
    }

    override fun decode(data: String): ByteArray? {
        return data.decodeToByteArrayOrNull(crockford)
    }
    override fun encode(data: ByteArray): String {
        return data.encodeToString(crockford)
    }

    @Test
    fun givenCheckSymbol_whenNotAValidSymbol_throwsException() {
        var exception: IllegalArgumentException? = null
        try {
            (Base32Crockford { checkSymbol('0') } )
        } catch (e: IllegalArgumentException) {
            exception = e
        }

        assertNotNull(exception)
    }

    @Test
    fun givenString_whenEncoded_MatchesSpec() {
        checkEncodeSuccessForDataSet(encodeSuccessDataSet)
    }

    @Test
    fun givenBadEncoding_whenDecoded_ReturnsNull() {
        checkDecodeFailureForDataSet(decodeFailureDataSet)
    }

    @Test
    fun givenEncodedData_whenDecoded_MatchesSpec() {
        checkDecodeSuccessForDataSet(decodeSuccessDataSet)
    }

    @Test
    fun givenEncodedDataWithCheckSymbol_whenDecodedWithoutCheckSymbolExpressed_returnsFailure() {
        val data = encodeSuccessDataSet.first()

        for (symbol in validCheckSymbols) {
            val decoded = (data.expected + symbol)
                .decodeToByteArrayOrNull(crockford)
            assertNull(decoded)
        }
    }

    @Test
    fun givenEncodedDataWithCheckSymbol_whenDecodedWithCheckSymbolExpressed_returnsExpected() {
        for (symbol in validCheckSymbols) {
            crockford = Base32Crockford { checkSymbol(symbol) }
            checkEncodeSuccessForDataSet(
                getEncodeSuccessDataSetWithCheckSymbolExpected(symbol)
            )
        }
    }

    @Test
    fun givenString_whenEncodedWithCheckSymbolExpressed_returnsExpected() {
        for (symbol in validCheckSymbols) {
            crockford = Base32Crockford { checkSymbol(symbol) }
            checkDecodeSuccessForDataSet(
                getDecodeSuccessDataSetWithCheckSymbolExpected(symbol)
            )
        }
    }

    @Test
    fun givenEncodingWithMultipleCheckSymbols_decodingWithCheckSymbol_returnsNull() {
        val data = encodeSuccessDataSet.first()

        for (symbol in validCheckSymbols) {
            val newData = Data(
                raw = data.raw,
                expected = "${data.expected}$symbol$symbol"
            )
            val decoded = newData.expected.decodeToByteArrayOrNull(Base32Crockford { checkSymbol(symbol)} )
            assertNull(decoded)
        }
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
    fun givenBase32Crockford_whenEncodeToLowercase_thenOutputIsLowercase() {
        crockford = Base32Crockford {
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
    fun givenBase32Crockford_whenLowercaseAndUppercaseChars_thenMatch() {
        assertEquals(Base32.Crockford.CHARS_UPPER, Base32.Crockford.CHARS_LOWER.uppercase())
        assertEquals(Base32.Crockford.CHARS_LOWER, Base32.Crockford.CHARS_UPPER.lowercase())
    }

    @Test
    fun givenBase32Crockford_whenDecodeEncode_thenReturnsSameValue() {
        val expected = "AHM6A83HENMP6TS0C9S6YXVE41K6YY10D9TPTW3K41QQCSBJ41T6GS90DHGQMY90CHQPEBG"
        val decoded = expected.decodeToByteArray(crockford)
        val rencoded = decoded.encodeToString(crockford)
        assertEquals(expected, rencoded)
    }

    @Test
    fun givenBase32Crockford_whenEncodeDecodeRandomData_thenBytesMatch() {
        checkRandomData()
    }

    @Test
    fun givenBase32CrockfordLowercase_whenEncodeDecodeRandomData_thenBytesMatch() {
        crockford = Base32Crockford { encodeToLowercase = true }
        checkRandomData()
    }

}
