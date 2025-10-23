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
@file:Suppress("FunctionName", "SpellCheckingInspection", "DEPRECATION")

package io.matthewnelson.encoding.base32

import io.matthewnelson.encoding.base32.internal.isCheckSymbol
import kotlin.jvm.JvmField
import kotlin.jvm.JvmName
import kotlin.jvm.JvmOverloads
import kotlin.jvm.JvmSynthetic

/**
 * DEPRECATED
 * @suppress
 * @see [Base32.Crockford.Builder]
 * @see [Base32.Crockford.Companion.Builder]
 * */
@Deprecated("Use Base32.Crockford.Builder or Base32.Crockford.Companion.Builder")
public fun Base32Crockford(
    config: Base32.Crockford.Config?,
    block: Base32CrockfordConfigBuilder.() -> Unit,
): Base32.Crockford = Base32CrockfordConfigBuilder(config).apply(block).buildCompat()

/**
 * DEPRECATED
 * @suppress
 * @see [Base32.Crockford.Builder]
 * @see [Base32.Crockford.Companion.Builder]
 * */
@Deprecated("Use Base32.Crockford.Builder or Base32.Crockford.Companion.Builder")
public fun Base32Crockford(
    block: Base32CrockfordConfigBuilder.() -> Unit,
): Base32.Crockford {
    return Base32Crockford(config = null, block)
}

/**
 * DEPRECATED
 * @suppress
 * @see [Base32.Crockford.Builder]
 * @see [Base32.Crockford.Companion.Builder]
 * */
@Deprecated("Use Base32.Crockford.Builder or Base32.Crockford.Companion.Builder")
@JvmOverloads
public fun Base32Crockford(strict: Boolean = false): Base32.Crockford = Base32.Crockford.Builder { if (strict) strict() }

/**
 * DEPRECATED
 * @suppress
 * @see [Base32.Default.Builder]
 * @see [Base32.Default.Companion.Builder]
 * */
@Deprecated("Use Base32.Default.Builder or Base32.Default.Companion.Builder")
public fun Base32Default(
    config: Base32.Default.Config?,
    block: Base32DefaultConfigBuilder.() -> Unit,
): Base32.Default = Base32DefaultConfigBuilder(config).apply(block).buildCompat()

/**
 * DEPRECATED
 * @suppress
 * @see [Base32.Default.Builder]
 * @see [Base32.Default.Companion.Builder]
 * */
@Deprecated("Use Base32.Default.Builder or Base32.Default.Companion.Builder")
public fun Base32Default(
    block: Base32DefaultConfigBuilder.() -> Unit,
): Base32.Default {
    return Base32Default(config = null, block)
}

/**
 * DEPRECATED
 * @suppress
 * @see [Base32.Default.Builder]
 * @see [Base32.Default.Companion.Builder]
 * */
@Deprecated("Use Base32.Default.Builder or Base32.Default.Companion.Builder")
@JvmOverloads
public fun Base32Default(strict: Boolean = false): Base32.Default = Base32.Default.Builder { if (strict) strict() }

/**
 * DEPRECATED
 * @suppress
 * @see [Base32.Hex.Builder]
 * @see [Base32.Hex.Companion.Builder]
 * */
@Deprecated("Use Base32.Hex.Builder or Base32.Hex.Companion.Builder")
public fun Base32Hex(
    config: Base32.Hex.Config?,
    block: Base32HexConfigBuilder.() -> Unit,
): Base32.Hex = Base32HexConfigBuilder(config).apply(block).buildCompat()

/**
 * DEPRECATED
 * @suppress
 * @see [Base32.Hex.Builder]
 * @see [Base32.Hex.Companion.Builder]
 * */
@Deprecated("Use Base32.Hex.Builder or Base32.Hex.Companion.Builder")
public fun Base32Hex(
    block: Base32HexConfigBuilder.() -> Unit,
): Base32.Hex {
    return Base32Hex(config = null, block)
}

/**
 * DEPRECATED
 * @suppress
 * @see [Base32.Hex.Builder]
 * @see [Base32.Hex.Companion.Builder]
 * */
@JvmOverloads
@Deprecated("Use Base32.Hex.Builder or Base32.Hex.Companion.Builder")
public fun Base32Hex(strict: Boolean = false): Base32.Hex = Base32.Hex.Builder { if (strict) strict() }

/**
 * DEPRECATED
 * @suppress
 * @see [Base32.Crockford.Builder]
 * @see [Base32.Crockford.Companion.Builder]
 * */
@Deprecated("Use Base32.Crockford.Builder or Base32.Crockford.Companion.Builder")
public class Base32CrockfordConfigBuilder {

    private val compat: Base32.Crockford.Builder

    public constructor(): this(config = null)
    public constructor(config: Base32.Crockford.Config?) {
        compat = Base32.Crockford.Builder(other = config)
        isLenient = compat._isLenient
        encodeToLowercase = compat._encodeToLowercase
        hyphenInterval = compat._hyphenInterval
        checkSymbol = compat._checkSymbol
        finalizeWhenFlushed = compat._finalizeWhenFlushed
    }

    /**
     * Refer to [Base32.Crockford.Builder.isLenient] documentation.
     * */
    @JvmField
    public var isLenient: Boolean = true

    /**
     * Refer to [Base32.Crockford.Builder.encodeToLowercase] documentation.
     * */
    @JvmField
    public var encodeToLowercase: Boolean = false

    /**
     * Refer to [Base32.Crockford.Builder.hyphen] documentation.
     * */
    @JvmField
    public var hyphenInterval: Byte = 0

    @get:JvmName("checkSymbol")
    public var checkSymbol: Char? = null
        private set

    /**
     * Refer to [Base32.Crockford.Builder.checkSymbol] documentation.
     *
     * @throws [IllegalArgumentException] If not a valid check symbol.
     * */
    @Throws(IllegalArgumentException::class)
    public fun checkSymbol(symbol: Char?): Base32CrockfordConfigBuilder {
        when {
            symbol == null || symbol.isCheckSymbol() -> {
                checkSymbol = symbol
            }
            else -> {
                throw IllegalArgumentException(
                    "CheckSymbol[$symbol] not recognized.\n" +
                    "Must be one of the following characters: *, ~, \$, =, U, u\n" +
                    "OR null to omit"
                )
            }
        }

        return this
    }

    /**
     * Refer to [Base32.Crockford.Builder.finalizeWhenFlushed] documentation.
     * */
    @JvmField
    public var finalizeWhenFlushed: Boolean = false

    /**
     * Refer to [Base32.Crockford.Builder.strict] documentation.
     * */
    public fun strict(): Base32CrockfordConfigBuilder {
        isLenient = false
        encodeToLowercase = false
        finalizeWhenFlushed = false
        return this
    }

    /**
     * Refer to [Base32.Crockford.Builder.build] documentation.
     * */
    public fun build(): Base32.Crockford.Config = buildCompat().config

    @JvmSynthetic
    internal fun buildCompat(): Base32.Crockford = compat
        .isLenient(isLenient)
        .encodeToLowercase(encodeToLowercase)
        .hyphen(hyphenInterval)
        .checkSymbol(checkSymbol)
        .finalizeWhenFlushed(finalizeWhenFlushed)
        .build()

    /** @suppress */
    @JvmField
    @Deprecated(message = "Implementation is always constant time. Performance impact is negligible.")
    public var isConstantTime: Boolean = true
}

/**
 * DEPRECATED
 * @suppress
 * @see [Base32.Default.Builder]
 * @see [Base32.Default.Companion.Builder]
 * */
@Deprecated("Use Base32.Default.Builder or Base32.Default.Companion.Builder")
public class Base32DefaultConfigBuilder {

    private val compat: Base32.Default.Builder

    public constructor(): this(config = null)
    public constructor(config: Base32.Default.Config?) {
        compat = Base32.Default.Builder(other = config)
        isLenient = compat._isLenient
        lineBreakInterval = compat._lineBreakInterval
        encodeToLowercase = compat._encodeToLowercase
        padEncoded = compat._padEncoded
    }

    /**
     * Refer to [Base32.Default.Builder.isLenient] documentation.
     * */
    @JvmField
    public var isLenient: Boolean = true

    /**
     * Refer to [Base32.Default.Builder.lineBreak] documentation.
     * */
    @JvmField
    public var lineBreakInterval: Byte = 0

    /**
     * Refer to [Base32.Default.Builder.encodeToLowercase] documentation.
     * */
    @JvmField
    public var encodeToLowercase: Boolean = false

    /**
     * Refer to [Base32.Default.Builder.padEncoded] documentation.
     * */
    @JvmField
    public var padEncoded: Boolean = true

    /**
     * Refer to [Base32.Default.Builder.strict] documentation.
     * */
    public fun strict(): Base32DefaultConfigBuilder {
        isLenient = false
        lineBreakInterval = 0
        encodeToLowercase = false
        padEncoded = true
        return this
    }

    /**
     * Refer to [Base32.Default.Builder.build] documentation.
     * */
    public fun build(): Base32.Default.Config = buildCompat().config

    @JvmSynthetic
    internal fun buildCompat(): Base32.Default = compat
        .isLenient(isLenient)
        .lineBreak(lineBreakInterval)
        .encodeToLowercase(encodeToLowercase)
        .padEncoded(padEncoded)
        .build()

    /** @suppress */
    @JvmField
    @Deprecated(message = "Implementation is always constant time. Performance impact is negligible.")
    public var isConstantTime: Boolean = true
}

/**
 * DEPRECATED
 * @suppress
 * @see [Base32.Hex.Builder]
 * @see [Base32.Hex.Companion.Builder]
 * */
@Deprecated("Use Base32.Hex.Builder or Base32.Hex.Companion.Builder")
public class Base32HexConfigBuilder {

    private val compat: Base32.Hex.Builder

    public constructor(): this(config = null)
    public constructor(config: Base32.Hex.Config?) {
        compat = Base32.Hex.Builder(other = config)
        isLenient = compat._isLenient
        lineBreakInterval = compat._lineBreakInterval
        encodeToLowercase = compat._encodeToLowercase
        padEncoded = compat._padEncoded
    }

    /**
     * Refer to [Base32.Hex.Builder.isLenient] documentation.
     * */
    @JvmField
    public var isLenient: Boolean = true

    /**
     * Refer to [Base32.Hex.Builder.lineBreak] documentation.
     * */
    @JvmField
    public var lineBreakInterval: Byte = 0

    /**
     * Refer to [Base32.Hex.Builder.encodeToLowercase] documentation.
     * */
    @JvmField
    public var encodeToLowercase: Boolean = false

    /**
     * Refer to [Base32.Hex.Builder.padEncoded] documentation.
     * */
    @JvmField
    public var padEncoded: Boolean = true

    /**
     * Refer to [Base32.Hex.Builder.strict] documentation.
     * */
    public fun strict(): Base32HexConfigBuilder {
        isLenient = false
        lineBreakInterval = 0
        encodeToLowercase = false
        padEncoded = true
        return this
    }

    /**
     * Refer to [Base32.Hex.Builder.build] documentation.
     * */
    public fun build(): Base32.Hex.Config = buildCompat().config

    @JvmSynthetic
    internal fun buildCompat(): Base32.Hex = compat
        .isLenient(isLenient)
        .lineBreak(lineBreakInterval)
        .encodeToLowercase(encodeToLowercase)
        .padEncoded(padEncoded)
        .build()

    /** @suppress */
    @JvmField
    @Deprecated(message = "Implementation is always constant time. Performance impact is negligible.")
    public var isConstantTime: Boolean = true
}
