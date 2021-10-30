package io.matthewnelson.component.encoding.base32

import io.matthewnelson.component.encoding.test.BaseEncodingTestBase
import kotlin.test.Test

class Base32HexUnitTest: BaseEncodingTestBase() {

    override val decodeFailureDataSet: Set<Data<String, Any?>> = setOf(
        Data(raw = "AW", expected = null, message = "Character 'W' should return null"),
        Data(raw = "AX", expected = null, message = "Character 'X' should return null"),
        Data(raw = "AY", expected = null, message = "Character 'Y' should return null"),
        Data(raw = "AZ", expected = null, message = "Character 'Z' should return null"),
        Data(raw = "91IMOR3F41BMUSJCCGGg====", expected = null, message = "Lowercase characters should return null"),
    )

    override val decodeSuccessHelloWorld: Data<String, ByteArray> =
        Data(raw = "91IMOR3F41BMUSJCCGGG====", expected = "Hello World!".encodeToByteArray())

    override val decodeSuccessDataSet: Set<Data<String, ByteArray>> = setOf(
        decodeSuccessHelloWorld,
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
        return data.decodeBase32ToArray(Base32.Hex)
    }
    override fun encode(data: ByteArray): String {
        return data.encodeBase32(Base32.Hex)
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

}
