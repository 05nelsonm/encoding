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
@file:Suppress("FunctionName", "SpellCheckingInspection")

package io.matthewnelson.encoding.builders

import io.matthewnelson.encoding.base32.Base32
import io.matthewnelson.encoding.core.EncodingException
import io.matthewnelson.encoding.core.util.byte
import io.matthewnelson.encoding.core.util.char
import kotlin.jvm.JvmField
import kotlin.jvm.JvmName

/**
 * Creates a configured [Base32.Crockford] encoder/decoder.
 *
 * @param [config] inherit settings from.
 * @see [Base32CrockfordBuilder]
 * */
public fun Base32Crockford(
    config: Base32.Crockford.Configuration?,
    block: Base32CrockfordBuilder.() -> Unit,
): Base32.Crockford {
    val builder = Base32CrockfordBuilder(config)
    block.invoke(builder)
    return Base32.Crockford(builder.build())
}

/**
 * Creates a configured [Base32.Crockford] encoder/decoder.
 *
 * @see [Base32CrockfordBuilder]
 * */
public fun Base32Crockford(
    block: Base32CrockfordBuilder.() -> Unit,
): Base32.Crockford {
    return Base32Crockford(config = null, block)
}

/**
 * Creates a configured [Base32.Default] encoder/decoder.
 *
 * @param [config] to inherit from.
 * @see [Base32DefaultBuilder]
 * */
public fun Base32Default(
    config: Base32.Default.Configuration?,
    block: Base32DefaultBuilder.() -> Unit,
): Base32.Default {
    val builder = Base32DefaultBuilder(config)
    block.invoke(builder)
    return Base32.Default(builder.build())
}

/**
 * Creates a configured [Base32.Default] encoder/decoder.
 *
 * @see [Base32DefaultBuilder]
 * */
public fun Base32Default(
    block: Base32DefaultBuilder.() -> Unit,
): Base32.Default {
    return Base32Default(config = null, block)
}

/**
 * Creates a configured [Base32.Hex] encoder/decoder.
 *
 * @param [config] to inherit from.
 * @see [Base32HexBuilder]
 * */
public fun Base32Hex(
    config: Base32.Hex.Configuration?,
    block: Base32HexBuilder.() -> Unit,
): Base32.Hex {
    val builder = Base32HexBuilder(config)
    block.invoke(builder)
    return Base32.Hex(builder.build())
}

/**
 * Creates a configured [Base32.Hex] encoder/decoder.
 *
 * @see [Base32HexBuilder]
 * */
public fun Base32Hex(
    block: Base32HexBuilder.() -> Unit,
): Base32.Hex {
    return Base32Hex(config = null, block)
}

/**
 * Builder for creating a [Base32.Crockford.Configuration].
 *
 * @see [Base32Crockford]
 * */
public class Base32CrockfordBuilder {

    public constructor()
    public constructor(config: Base32.Crockford.Configuration?): this() {
        if (config == null) return
        isLenient = config.isLenient
        acceptLowercase = config.acceptLowercase
        hyphenInterval = config.hyphenInterval
        checkByte = config.checkByte
    }

    /**
     * If true, spaces and new lines ('\n', '\r', ' ', '\t')
     * will be skipped over when decoding.
     *
     * If false, an [EncodingException] will be thrown if
     * those characters are encountered when decoding.
     * */
    @JvmField
    public var isLenient: Boolean = true

    /**
     * If true, will accept lowercase **AND** uppercase
     * characters when decoding (against Crockford spec).
     *
     * If false, an [EncodingException] will be thrown if
     * lowercase characters are encountered when decoding.
     * */
    @JvmField
    public var acceptLowercase: Boolean = false

    /**
     * The interval at which hyphens ("-") should be inserted
     * when encoding data.
     *
     * Enable by setting to a value greater than 0.
     * */
    @JvmField
    public var hyphenInterval: Short = 0

    @get:JvmName("checkByte")
    public var checkByte: Byte? = null
        private set
    @get:JvmName("checkSymbol")
    public val checkSymbol: Char? get() = checkByte?.char

    /**
     * Specify an optional check symbol to be appended to the encoded
     * output, and verified when decoding (fail quickly).
     *
     * Valid check symbols are:
     *  - '*', '~', '$', '=', 'U', 'u'
     *  - or null to omit
     *
     * @throws [IllegalArgumentException] If not a valid check symbol.
     * */
    @Throws(IllegalArgumentException::class)
    public fun checkByte(checkSymbol: Char?): Base32CrockfordBuilder {
        when (checkSymbol) {
            null, '*', '~', '$', '=', 'U', 'u' -> {
                checkByte = checkSymbol?.byte
            }
            else -> {
                throw IllegalArgumentException(
                    "checkSymbol[$checkSymbol] not recognized.\n" +
                    "Must be one of the following characters: *, ~, \$, =, U, u\n" +
                    "OR null to omit"
                )
            }
        }

        return this
    }

    /**
     * Builds a [Base32.Crockford.Configuration] for the provided settings.
     * */
    public fun build(): Base32.Crockford.Configuration = Base32.Crockford.Configuration.from(this)
}

/**
 * Builder for creating a [Base32.Default.Configuration].
 *
 * @see [Base32Default]
 * */
public class Base32DefaultBuilder {

    public constructor()
    public constructor(config: Base32.Default.Configuration?): this() {
        if (config == null) return
        isLenient = config.isLenient
        acceptLowercase = config.acceptLowercase
        encodeToLowercase = config.encodeToLowercase
        padEncoded = config.padEncoded
    }

    /**
     * If true, spaces and new lines ('\n', '\r', ' ', '\t')
     * will be skipped over when decoding.
     *
     * If false, an [EncodingException] will be thrown if
     * those characters are encountered when decoding.
     * */
    @JvmField
    public var isLenient: Boolean = true

    /**
     * If true, will accept lowercase **AND** uppercase
     * characters when decoding (against RFC 4648).
     *
     * If false, an [EncodingException] will be thrown if
     * lowercase characters are encountered when decoding.
     * */
    @JvmField
    public var acceptLowercase: Boolean = true

    /**
     * If true, will output lowercase characters when
     * encoding (against RFC 4648).
     *
     * If false, will output uppercase characters when
     * encoding.
     * */
    @JvmField
    public var encodeToLowercase: Boolean = true

    /**
     * If true, padding **WILL** be applied to the encoded
     * output.
     *
     * If false, padding **WILL NOT** be applied to the
     * encoded output (against RFC 4648).
     * */
    @JvmField
    public var padEncoded: Boolean = true

    /**
     * Builds a [Base32.Default.Configuration] for the provided settings.
     * */
    public fun build(): Base32.Default.Configuration = Base32.Default.Configuration.from(this)
}

/**
 * Builder for creating a [Base32.Hex.Configuration].
 *
 * @see [Base32Hex]
 * */
public class Base32HexBuilder {

    public constructor()
    public constructor(config: Base32.Hex.Configuration?): this() {
        if (config == null) return
        isLenient = config.isLenient
        acceptLowercase = config.acceptLowercase
        encodeToLowercase = config.encodeToLowercase
        padEncoded = config.padEncoded
    }

    /**
     * If true, spaces and new lines ('\n', '\r', ' ', '\t')
     * will be skipped over when decoding.
     *
     * If false, an [EncodingException] will be thrown if
     * those characters are encountered when decoding.
     * */
    @JvmField
    public var isLenient: Boolean = true

    /**
     * If true, will accept lowercase **AND** uppercase
     * characters when decoding (against RFC 4648).
     *
     * If false, an [EncodingException] will be thrown if
     * lowercase characters are encountered when decoding.
     * */
    @JvmField
    public var acceptLowercase: Boolean = true

    /**
     * If true, will output lowercase characters when
     * encoding (against RFC 4648).
     *
     * If false, will output uppercase characters when
     * encoding.
     * */
    @JvmField
    public var encodeToLowercase: Boolean = true

    /**
     * If true, padding **WILL** be applied to the encoded
     * output.
     *
     * If false, padding **WILL NOT** be applied to the
     * encoded output (against RFC 4648).
     * */
    @JvmField
    public var padEncoded: Boolean = true

    /**
     * Builds a [Base32.Hex.Configuration] for the provided settings.
     * */
    public fun build(): Base32.Hex.Configuration = Base32.Hex.Configuration.from(this)
}
