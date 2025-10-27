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
@file:Suppress("DEPRECATION", "FunctionName", "SpellCheckingInspection")

package io.matthewnelson.encoding.base32

import io.matthewnelson.encoding.base32.internal.isCheckSymbol
import io.matthewnelson.encoding.core.Decoder
import io.matthewnelson.encoding.core.Encoder
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
@Deprecated(
    message = "Use Base32.Crockford.Builder or Base32.Crockford.Companion.Builder",
    level = DeprecationLevel.WARNING,
)
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
@Deprecated(
    message = "Use Base32.Crockford.Builder or Base32.Crockford.Companion.Builder",
    level = DeprecationLevel.WARNING,
)
public fun Base32Crockford(
    block: Base32CrockfordConfigBuilder.() -> Unit,
): Base32.Crockford = Base32Crockford(config = null, block)

/**
 * DEPRECATED
 * @suppress
 * @see [Base32.Crockford.Builder]
 * @see [Base32.Crockford.Companion.Builder]
 * */
@Deprecated(
    message = "Use Base32.Crockford.Builder or Base32.Crockford.Companion.Builder",
    level = DeprecationLevel.WARNING,
)
@JvmOverloads
public fun Base32Crockford(
    strict: Boolean = false,
): Base32.Crockford = Base32Crockford { if (strict) strict() }

/**
 * DEPRECATED
 * @suppress
 * @see [Base32.Default.Builder]
 * @see [Base32.Default.Companion.Builder]
 * */
@Deprecated(
    message = "Use Base32.Default.Builder or Base32.Default.Companion.Builder",
    level = DeprecationLevel.WARNING,
)
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
@Deprecated(
    message = "Use Base32.Default.Builder or Base32.Default.Companion.Builder",
    level = DeprecationLevel.WARNING,
)
public fun Base32Default(
    block: Base32DefaultConfigBuilder.() -> Unit,
): Base32.Default = Base32Default(config = null, block)

/**
 * DEPRECATED
 * @suppress
 * @see [Base32.Default.Builder]
 * @see [Base32.Default.Companion.Builder]
 * */
@Deprecated(
    message = "Use Base32.Default.Builder or Base32.Default.Companion.Builder",
    level = DeprecationLevel.WARNING,
)
@JvmOverloads
public fun Base32Default(
    strict: Boolean = false,
): Base32.Default = Base32.Default.Builder { if (strict) strictSpec() }

/**
 * DEPRECATED
 * @suppress
 * @see [Base32.Hex.Builder]
 * @see [Base32.Hex.Companion.Builder]
 * */
@Deprecated(
    message = "Use Base32.Hex.Builder or Base32.Hex.Companion.Builder",
    level = DeprecationLevel.WARNING,
)
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
@Deprecated(
    message = "Use Base32.Hex.Builder or Base32.Hex.Companion.Builder",
    level = DeprecationLevel.WARNING,
)
public fun Base32Hex(
    block: Base32HexConfigBuilder.() -> Unit,
): Base32.Hex = Base32Hex(config = null, block)

/**
 * DEPRECATED
 * @suppress
 * @see [Base32.Hex.Builder]
 * @see [Base32.Hex.Companion.Builder]
 * */
@JvmOverloads
@Deprecated(
    message = "Use Base32.Hex.Builder or Base32.Hex.Companion.Builder",
    level = DeprecationLevel.WARNING,
)
public fun Base32Hex(
    strict: Boolean = false,
): Base32.Hex = Base32.Hex.Builder { if (strict) strictSpec() }

/**
 * DEPRECATED
 * @suppress
 * @see [Base32.Crockford.Builder]
 * @see [Base32.Crockford.Companion.Builder]
 * */
@Deprecated(
    message = "Use Base32.Crockford.Builder or Base32.Crockford.Companion.Builder",
    level = DeprecationLevel.WARNING,
)
public class Base32CrockfordConfigBuilder {

    private val compat: Base32.Crockford.Builder

    public constructor(): this(config = null)
    public constructor(config: Base32.Crockford.Config?) {
        compat = Base32.Crockford.Builder(other = config)
        isLenient = compat._isLenient
        encodeToLowercase = compat._encodeLowercase
        hyphenInterval = compat._hyphenInterval
        checkSymbol = compat._checkSymbol
        finalizeWhenFlushed = config?.finalizeWhenFlushed ?: false
    }

    /**
     * Refer to [Base32.Crockford.Builder.isLenient] documentation.
     * */
    @JvmField
    public var isLenient: Boolean = true

    /**
     * Refer to [Base32.Crockford.Builder.encodeLowercase] documentation.
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
     * Refer to [Base32.Crockford.Builder.check] documentation.
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
     * DEFAULT: `false`
     *
     * **NOTE:** Configurability of this option has been removed from the new
     * [Base32.Crockford.Builder] which utilizes a default value of `true`. For compatability
     * purposes, this builder maintains the default value of `false` and configures the
     * [Base32.Crockford.Builder] as such (if not modified here) when [build] is called.
     *
     * If `true`:
     *  - Encoding: Whenever [Encoder.Feed.flush] is called, in addition to processing any
     *  buffered input, the [checkSymbol] will be appended and counter for [hyphenInterval]
     *  will be reset (if they were configured).
     *  - Decoding: Whenever [Decoder.Feed.flush] is called, verification that the [checkSymbol]
     *  was present for that decoding will be had, prior to processing any buffered input.
     *  Verification is ignored if no [checkSymbol] was configured, or there was no input.
     *
     * If `false`:
     *  - Encoding: Whenever [Encoder.Feed.flush] is called, only processing of buffered
     *  input will occur; no [checkSymbol] will be appended, and the counter for [hyphenInterval]
     *  will not be reset.
     *  - Decoding: Whenever [Decoder.Feed.flush] is called, Verification of the presence
     *  of the [checkSymbol] only occurs on the final decoding. If no [checkSymbol] was
     *  configured, or there was no input, then this is ignored.
     *
     * **NOTE:** This setting is ignored if neither [hyphenInterval] interval nor [checkSymbol]
     * are configured.
     *
     * e.g. (Behavior when `true`)
     *
     *     val sb = StringBuilder()
     *     Base32Crockford {
     *         hyphenInterval = 4
     *         checkSymbol('*')
     *         finalizeWhenFlushed = true
     *     }.newEncoderFeed { encodedChar ->
     *         sb.append(encodedChar)
     *     }.use { feed ->
     *         bytes1.forEach { b -> feed.consume(b) }
     *         feed.flush()
     *         bytes2.forEach { b -> feed.consume(b) }
     *     }
     *     println(sb.toString())
     *     // 91JP-RV3F-*41BP-YWKC-CGGG-*
     *
     * e.g. (Behavior when `false`)
     *
     *     val sb = StringBuilder()
     *     Base32.Crockford.Builder {
     *         hyphenInterval = 4
     *         checkSymbol('*')
     *         finalizeWhenFlushed = false
     *     }.newEncoderFeed { encodedChar ->
     *         sb.append(encodedChar)
     *     }.use { feed ->
     *         bytes1.forEach { b -> feed.consume(b) }
     *         feed.flush()
     *         bytes2.forEach { b -> feed.consume(b) }
     *     }
     *     println(sb.toString())
     *     // 91JP-RV3F-41BP-YWKC-CGGG-*
     * */
    @JvmField
    public var finalizeWhenFlushed: Boolean = false

    /**
     * Refer to [Base32.Crockford.Builder.strictSpec] documentation.
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
        .encodeLowercase(encodeToLowercase)
        .hyphen(hyphenInterval)
        .check(checkSymbol)
        .apply { _finalizeWhenFlushed = finalizeWhenFlushed }
        .build()

    /**
     * DEPRECATED
     * @suppress
     * */
    @JvmField
    @Deprecated(
        message = "Implementation is always constant time. Performance impact is negligible.",
        level = DeprecationLevel.ERROR,
    )
    public var isConstantTime: Boolean = true
}

/**
 * DEPRECATED
 * @suppress
 * @see [Base32.Default.Builder]
 * @see [Base32.Default.Companion.Builder]
 * */
@Deprecated(
    message = "Use Base32.Default.Builder or Base32.Default.Companion.Builder",
    level = DeprecationLevel.WARNING,
)
public class Base32DefaultConfigBuilder {

    private val compat: Base32.Default.Builder

    public constructor(): this(config = null)
    public constructor(config: Base32.Default.Config?) {
        compat = Base32.Default.Builder(other = config)
        isLenient = compat._isLenient
        lineBreakInterval = compat._lineBreakInterval
        encodeToLowercase = compat._encodeLowercase
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
     * Refer to [Base32.Default.Builder.encodeLowercase] documentation.
     * */
    @JvmField
    public var encodeToLowercase: Boolean = false

    /**
     * Refer to [Base32.Default.Builder.padEncoded] documentation.
     * */
    @JvmField
    public var padEncoded: Boolean = true

    /**
     * Refer to [Base32.Default.Builder.strictSpec] documentation.
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
        .encodeLowercase(encodeToLowercase)
        .padEncoded(padEncoded)
        .build()

    /**
     * DEPRECATED
     * @suppress
     * */
    @JvmField
    @Deprecated(
        message = "Implementation is always constant time. Performance impact is negligible.",
        level = DeprecationLevel.ERROR,
    )
    public var isConstantTime: Boolean = true
}

/**
 * DEPRECATED
 * @suppress
 * @see [Base32.Hex.Builder]
 * @see [Base32.Hex.Companion.Builder]
 * */
@Deprecated(
    message = "Use Base32.Hex.Builder or Base32.Hex.Companion.Builder",
    level = DeprecationLevel.WARNING,
)
public class Base32HexConfigBuilder {

    private val compat: Base32.Hex.Builder

    public constructor(): this(config = null)
    public constructor(config: Base32.Hex.Config?) {
        compat = Base32.Hex.Builder(other = config)
        isLenient = compat._isLenient
        lineBreakInterval = compat._lineBreakInterval
        encodeToLowercase = compat._encodeLowercase
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
     * Refer to [Base32.Hex.Builder.encodeLowercase] documentation.
     * */
    @JvmField
    public var encodeToLowercase: Boolean = false

    /**
     * Refer to [Base32.Hex.Builder.padEncoded] documentation.
     * */
    @JvmField
    public var padEncoded: Boolean = true

    /**
     * Refer to [Base32.Hex.Builder.strictSpec] documentation.
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
        .encodeLowercase(encodeToLowercase)
        .padEncoded(padEncoded)
        .build()

    /**
     * DEPRECATED
     * @suppress
     * */
    @JvmField
    @Deprecated(
        message = "Implementation is always constant time. Performance impact is negligible.",
        level = DeprecationLevel.ERROR,
    )
    public var isConstantTime: Boolean = true
}
