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
import kotlin.jvm.JvmOverloads

/**
 * Creates a configured [Base32.Crockford] encoder/decoder.
 *
 * @param [config] inherit settings from.
 * @see [Base32CrockfordConfigBuilder]
 * */
public fun Base32Crockford(
    config: Base32.Crockford.Config?,
    block: Base32CrockfordConfigBuilder.() -> Unit,
): Base32.Crockford {
    val builder = Base32CrockfordConfigBuilder(config)
    block.invoke(builder)
    return Base32.Crockford(builder.build())
}

/**
 * Creates a configured [Base32.Crockford] encoder/decoder.
 *
 * @see [Base32CrockfordConfigBuilder]
 * */
public fun Base32Crockford(
    block: Base32CrockfordConfigBuilder.() -> Unit,
): Base32.Crockford {
    return Base32Crockford(config = null, block)
}

/**
 * Creates a configured [Base32.Crockford] encoder/decoder
 * using the default settings.
 *
 * @param [strict] If true, configures the encoder/decoder
 *   to be in strict accordance with the Crockford spec.
 * @see [Base32CrockfordConfigBuilder]
 * */
@JvmOverloads
public fun Base32Crockford(strict: Boolean = false): Base32.Crockford = Base32Crockford { if (strict) strict() }

/**
 * Creates a configured [Base32.Default] encoder/decoder.
 *
 * @param [config] to inherit from.
 * @see [Base32DefaultConfigBuilder]
 * */
public fun Base32Default(
    config: Base32.Default.Config?,
    block: Base32DefaultConfigBuilder.() -> Unit,
): Base32.Default {
    val builder = Base32DefaultConfigBuilder(config)
    block.invoke(builder)
    return Base32.Default(builder.build())
}

/**
 * Creates a configured [Base32.Default] encoder/decoder.
 *
 * @see [Base32DefaultConfigBuilder]
 * */
public fun Base32Default(
    block: Base32DefaultConfigBuilder.() -> Unit,
): Base32.Default {
    return Base32Default(config = null, block)
}

/**
 * Creates a configured [Base32.Default] encoder/decoder
 * using the default settings.
 *
 * @param [strict] If true, configures the encoder/decoder
 *   to be in strict accordance with RFC 4648.
 * @see [Base32DefaultConfigBuilder]
 * */
@JvmOverloads
public fun Base32Default(strict: Boolean = false): Base32.Default = Base32Default { if (strict) strict() }

/**
 * Creates a configured [Base32.Hex] encoder/decoder.
 *
 * @param [config] to inherit from.
 * @see [Base32HexConfigBuilder]
 * */
public fun Base32Hex(
    config: Base32.Hex.Config?,
    block: Base32HexConfigBuilder.() -> Unit,
): Base32.Hex {
    val builder = Base32HexConfigBuilder(config)
    block.invoke(builder)
    return Base32.Hex(builder.build())
}

/**
 * Creates a configured [Base32.Hex] encoder/decoder.
 *
 * @see [Base32HexConfigBuilder]
 * */
public fun Base32Hex(
    block: Base32HexConfigBuilder.() -> Unit,
): Base32.Hex {
    return Base32Hex(config = null, block)
}

/**
 * Creates a configured [Base32.Hex] encoder/decoder
 * using the default settings.
 *
 * @param [strict] If true, configures the encoder/decoder
 *   to be in strict accordance with RFC 4648.
 * @see [Base32HexConfigBuilder]
 * */
@JvmOverloads
public fun Base32Hex(strict: Boolean = false): Base32.Hex = Base32Hex { if (strict) strict() }

/**
 * Builder for creating a [Base32.Crockford.Config].
 *
 * @see [strict]
 * @see [Base32Crockford]
 * */
public class Base32CrockfordConfigBuilder {

    public constructor()
    public constructor(config: Base32.Crockford.Config?): this() {
        if (config == null) return
        isLenient = config.isLenient
        acceptLowercase = config.acceptLowercase
        encodeToLowercase = config.encodeToLowercase
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
    public var acceptLowercase: Boolean = true

    /**
     * If true, will output lowercase characters when
     * encoding (against Crockford spec).
     *
     * If false, will output uppercase characters when
     * encoding.
     * */
    @JvmField
    public var encodeToLowercase: Boolean = true

    /**
     * For every [hyphenInterval] of decoded output, a
     * hyphen ("-") will be inserted.
     *
     * e.g.
     *
     *     hyphenInterval = 0
     *     // 91JPRV3F41BPYWKCCGGG
     *
     *     hyphenInterval = 5
     *     // 91JPR-V3F41-BPYWK-CCGGG
     *
     *     hyphenInterval = 4
     *     // 91JP-RV3F-41BP-YWKC-CGGG
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
    public fun checkByte(checkSymbol: Char?): Base32CrockfordConfigBuilder {
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
     * A shortcut for configuring things to be in strict
     * adherence with the Crockford spec.
     * */
    public fun strict(): Base32CrockfordConfigBuilder {
        isLenient = false
        acceptLowercase = false
        encodeToLowercase = false
        return this
    }

    /**
     * Builds a [Base32.Crockford.Config] for the provided settings.
     * */
    public fun build(): Base32.Crockford.Config = Base32.Crockford.Config.from(this)
}

/**
 * Builder for creating a [Base32.Default.Config].
 *
 * @see [strict]
 * @see [Base32Default]
 * */
public class Base32DefaultConfigBuilder {

    public constructor()
    public constructor(config: Base32.Default.Config?): this() {
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
     * A shortcut for configuring things to be in strict
     * adherence with RFC 4648.
     * */
    public fun strict(): Base32DefaultConfigBuilder {
        isLenient = false
        acceptLowercase = false
        encodeToLowercase = false
        padEncoded = true
        return this
    }

    /**
     * Builds a [Base32.Default.Config] for the provided settings.
     * */
    public fun build(): Base32.Default.Config = Base32.Default.Config.from(this)
}

/**
 * Builder for creating a [Base32.Hex.Config].
 *
 * @see [strict]
 * @see [Base32Hex]
 * */
public class Base32HexConfigBuilder {

    public constructor()
    public constructor(config: Base32.Hex.Config?): this() {
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
     * A shortcut for configuring things to be in strict
     * adherence with RFC 4648.
     * */
    public fun strict(): Base32HexConfigBuilder {
        isLenient = false
        acceptLowercase = false
        encodeToLowercase = false
        padEncoded = true
        return this
    }

    /**
     * Builds a [Base32.Hex.Config] for the provided settings.
     * */
    public fun build(): Base32.Hex.Config = Base32.Hex.Config.from(this)
}
