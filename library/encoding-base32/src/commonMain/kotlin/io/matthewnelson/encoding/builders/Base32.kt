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

package io.matthewnelson.encoding.builders

import io.matthewnelson.encoding.base32.Base32
import io.matthewnelson.encoding.base32.internal.isCheckSymbol
import io.matthewnelson.encoding.core.EncodingException
import io.matthewnelson.encoding.core.Encoder
import kotlin.jvm.JvmField
import kotlin.jvm.JvmName
import kotlin.jvm.JvmOverloads

/**
 * Deprecated
 *
 * @see [io.matthewnelson.encoding.base32.Base32Crockford]
 * */
@Deprecated(
    message = """
        Moved to package io.matthewnelson.encoding.base32
    
        Will be removed in 2.0.0 because of an issue with
        Java 9 modules and JPMS not allowing split packages
        
        See: https://github.com/05nelsonm/encoding/blob/master/MIGRATION.md
    """
    ,
    replaceWith = ReplaceWith(
        expression = "Base32Crockford(config) { block() }",
        imports = [
            "io.matthewnelson.encoding.base32.Base32Crockford"
        ]
    )
)
public fun Base32Crockford(
    config: Base32.Crockford.Config?,
    block: Base32CrockfordConfigBuilder.() -> Unit,
): Base32.Crockford {
    val builder = Base32CrockfordConfigBuilder(config)
    block.invoke(builder)
    return Base32.Crockford(builder.build())
}

/**
 * Deprecated
 *
 * @see [io.matthewnelson.encoding.base32.Base32Crockford]
 * */
@Deprecated(
    message = """
    Moved to package io.matthewnelson.encoding.base32
    
    Will be removed in 2.0.0 because of an issue with
    Java 9 modules and JPMS not allowing split packages
    
    See: https://github.com/05nelsonm/encoding/blob/master/MIGRATION.md
    """,
    replaceWith = ReplaceWith(
        expression = "Base32Crockford { block() }",
        imports = [
            "io.matthewnelson.encoding.base32.Base32Crockford"
        ]
    )
)
public fun Base32Crockford(
    block: Base32CrockfordConfigBuilder.() -> Unit,
): Base32.Crockford {
    return Base32Crockford(config = null, block)
}

/**
 * Deprecated
 *
 * @see [io.matthewnelson.encoding.base32.Base32Crockford]
 * */
@Deprecated(
    message = """
        Moved to package io.matthewnelson.encoding.base32
        
        Will be removed in 2.0.0 because of an issue with
        Java 9 modules and JPMS not allowing split packages
        
        See: https://github.com/05nelsonm/encoding/blob/master/MIGRATION.md
    """,
    replaceWith = ReplaceWith(
        expression = "Base32Crockford(strict)",
        imports = [
            "io.matthewnelson.encoding.base32.Base32Crockford"
        ]
    )
)
@JvmOverloads
public fun Base32Crockford(strict: Boolean = false): Base32.Crockford = Base32Crockford { if (strict) strict() }

/**
 * Deprecated
 *
 * @see [io.matthewnelson.encoding.base32.Base32Default]
 * */
@Deprecated(
    message = """
        Moved to package io.matthewnelson.encoding.base32
        
        Will be removed in 2.0.0 because of an issue with
        Java 9 modules and JPMS not allowing split packages
        
        See: https://github.com/05nelsonm/encoding/blob/master/MIGRATION.md
    """,
    replaceWith = ReplaceWith(
        expression = "Base32Default(config) { block() }",
        imports = [
            "io.matthewnelson.encoding.base32.Base32Default"
        ]
    )
)
public fun Base32Default(
    config: Base32.Default.Config?,
    block: Base32DefaultConfigBuilder.() -> Unit,
): Base32.Default {
    val builder = Base32DefaultConfigBuilder(config)
    block.invoke(builder)
    return Base32.Default(builder.build())
}

/**
 * Deprecated
 *
 * @see [io.matthewnelson.encoding.base32.Base32Default]
 * */
@Deprecated(
    message = """
        Moved to package io.matthewnelson.encoding.base32
        
        Will be removed in 2.0.0 because of an issue with
        Java 9 modules and JPMS not allowing split packages
        
        See: https://github.com/05nelsonm/encoding/blob/master/MIGRATION.md
    """,
    replaceWith = ReplaceWith(
        expression = "Base32Default { block() }",
        imports = [
            "io.matthewnelson.encoding.base32.Base32Default"
        ]
    )
)
public fun Base32Default(
    block: Base32DefaultConfigBuilder.() -> Unit,
): Base32.Default {
    return Base32Default(config = null, block)
}

/**
 * Deprecated
 *
 * @see [io.matthewnelson.encoding.base32.Base32Default]
 * */
@Deprecated(
    message = """
        Moved to package io.matthewnelson.encoding.base32
        
        Will be removed in 2.0.0 because of an issue with
        Java 9 modules and JPMS not allowing split packages
        
        See: https://github.com/05nelsonm/encoding/blob/master/MIGRATION.md
    """,
    replaceWith = ReplaceWith(
        expression = "Base32Default(strict)",
        imports = [
            "io.matthewnelson.encoding.base32.Base32Default"
        ]
    )
)
@JvmOverloads
public fun Base32Default(strict: Boolean = false): Base32.Default = Base32Default { if (strict) strict() }

/**
 * Deprecated
 *
 * @see [io.matthewnelson.encoding.base32.Base32Hex]
 * */
@Deprecated(
    message = """
        Moved to package io.matthewnelson.encoding.base32
        
        Will be removed in 2.0.0 because of an issue with
        Java 9 modules and JPMS not allowing split packages
        
        See: https://github.com/05nelsonm/encoding/blob/master/MIGRATION.md
    """,
    replaceWith = ReplaceWith(
        expression = "Base32Hex(config) { block() }",
        imports = [
            "io.matthewnelson.encoding.base32.Base32Hex"
        ]
    )
)
public fun Base32Hex(
    config: Base32.Hex.Config?,
    block: Base32HexConfigBuilder.() -> Unit,
): Base32.Hex {
    val builder = Base32HexConfigBuilder(config)
    block.invoke(builder)
    return Base32.Hex(builder.build())
}

/**
 * Deprecated
 *
 * @see [io.matthewnelson.encoding.base32.Base32Hex]
 * */
@Deprecated(
    message = """
        Moved to package io.matthewnelson.encoding.base32
        
        Will be removed in 2.0.0 because of an issue with
        Java 9 modules and JPMS not allowing split packages
        
        See: https://github.com/05nelsonm/encoding/blob/master/MIGRATION.md
    """,
    replaceWith = ReplaceWith(
        expression = "Base32Hex { block() }",
        imports = [
            "io.matthewnelson.encoding.base32.Base32Hex"
        ]
    )
)
public fun Base32Hex(
    block: Base32HexConfigBuilder.() -> Unit,
): Base32.Hex {
    return Base32Hex(config = null, block)
}

/**
 * Deprecated
 *
 * @see [io.matthewnelson.encoding.base32.Base32Hex]
 * */
@Deprecated(
    message = """
        Moved to package io.matthewnelson.encoding.base32
        
        Will be removed in 2.0.0 because of an issue with
        Java 9 modules and JPMS not allowing split packages
        
        See: https://github.com/05nelsonm/encoding/blob/master/MIGRATION.md
    """,
    replaceWith = ReplaceWith(
        expression = "Base32Hex(strict)",
        imports = [
            "io.matthewnelson.encoding.base32.Base32Hex"
        ]
    )
)
@JvmOverloads
public fun Base32Hex(strict: Boolean = false): Base32.Hex = Base32Hex { if (strict) strict() }

/**
 * Deprecated
 *
 * @see [io.matthewnelson.encoding.base32.Base32CrockfordConfigBuilder]
 * */
@Deprecated(
    message = """
        Moved to package io.matthewnelson.encoding.base32
        
        Will be removed in 2.0.0 because of an issue with
        Java 9 modules and JPMS not allowing split packages
        
        See: https://github.com/05nelsonm/encoding/blob/master/MIGRATION.md
    """,
    replaceWith = ReplaceWith(
        expression = "Base32CrockfordConfigBuilder",
        imports = [
            "io.matthewnelson.encoding.base32.Base32CrockfordConfigBuilder"
        ]
    )
)
public class Base32CrockfordConfigBuilder {

    public constructor()
    public constructor(config: Base32.Crockford.Config?): this() {
        if (config == null) return
        isLenient = config.isLenient ?: true
        encodeToLowercase = config.encodeToLowercase
        hyphenInterval = config.hyphenInterval
        checkSymbol = config.checkSymbol
        finalizeWhenFlushed = config.finalizeWhenFlushed
    }

    /**
     * If true, spaces and new lines ('\n', '\r', ' ', '\t')
     * will be skipped over when decoding (against Crockford spec).
     *
     * If false, an [EncodingException] will be thrown if
     * those characters are encountered when decoding.
     * */
    @JvmField
    public var isLenient: Boolean = true

    /**
     * If true, will output lowercase characters when
     * encoding (against Crockford spec).
     *
     * If false, will output uppercase characters when
     * encoding.
     * */
    @JvmField
    public var encodeToLowercase: Boolean = false

    /**
     * For every [hyphenInterval] of encoded output, a
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
     * Enable by setting to a value between 1 and 127.
     * */
    @JvmField
    public var hyphenInterval: Byte = 0

    @get:JvmName("checkSymbol")
    public var checkSymbol: Char? = null
        private set

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
     * If true, whenever [Encoder.Feed.flush] is invoked:
     *  - The [checkSymbol] will be appended.
     *  - The counter for [hyphenInterval] insertion will be reset.
     *
     * If false
     *  - Appendage of the [checkSymbol] will only occur when
     *   [Encoder.Feed.doFinal] is invoked.
     *  - The counter for [hyphenInterval] insertion will not be reset.
     *
     * If neither [checkSymbol] or [hyphenInterval] are set, this has
     * no effect when encoding.
     *
     * e.g.
     *
     *     // WHEN FALSE
     *     val sb = StringBuilder()
     *     Base32Crockford {
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
     *
     *     println(sb.toString())
     *     // 91JP-RV3F-41BP-YWKC-CGGG-*
     *
     *     // WHEN TRUE
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
     *
     *         // Could do something here like insert a line
     *         // break. It has its use cases.
     *
     *         bytes2.forEach { b -> feed.consume(b) }
     *     }
     *
     *     println(sb.toString())
     *     // 91JP-RV3F-*41BP-YWKC-CGGG-*
     *
     * */
    @JvmField
    public var finalizeWhenFlushed: Boolean = false

    /**
     * A shortcut for configuring things to be in strict
     * adherence with the Crockford spec.
     * */
    public fun strict(): Base32CrockfordConfigBuilder {
        isLenient = false
        encodeToLowercase = false
        finalizeWhenFlushed = false
        return this
    }

    /**
     * Builds a [Base32.Crockford.Config] for the provided settings.
     * */
    public fun build(): Base32.Crockford.Config {
        val b = io.matthewnelson.encoding.base32.Base32CrockfordConfigBuilder()
        b.isLenient = isLenient
        b.encodeToLowercase = encodeToLowercase
        b.hyphenInterval = hyphenInterval
        b.checkSymbol(checkSymbol)
        b.finalizeWhenFlushed = finalizeWhenFlushed
        return b.build()
    }
}

/**
 * Deprecated
 *
 * @see [io.matthewnelson.encoding.base32.Base32DefaultConfigBuilder]
 * */
@Deprecated(
    message = """
        Moved to package io.matthewnelson.encoding.base32
        
        Will be removed in 2.0.0 because of an issue with
        Java 9 modules and JPMS not allowing split packages
        
        See: https://github.com/05nelsonm/encoding/blob/master/MIGRATION.md
    """,
    replaceWith = ReplaceWith(
        expression = "Base32DefaultConfigBuilder",
        imports = [
            "io.matthewnelson.encoding.base32.Base32DefaultConfigBuilder"
        ]
    )
)
public class Base32DefaultConfigBuilder {

    public constructor()
    public constructor(config: Base32.Default.Config?): this() {
        if (config == null) return
        isLenient = config.isLenient ?: true
        lineBreakInterval = config.lineBreakInterval
        encodeToLowercase = config.encodeToLowercase
        padEncoded = config.padEncoded
    }

    /**
     * If true, spaces and new lines ('\n', '\r', ' ', '\t')
     * will be skipped over when decoding (against RFC 4648).
     *
     * If false, an [EncodingException] will be thrown if
     * those characters are encountered when decoding.
     * */
    @JvmField
    public var isLenient: Boolean = true

    /**
     * For every [lineBreakInterval] of encoded data, a
     * line break will be output.
     *
     * Will **ONLY** output line breaks if [isLenient] is
     * set to **true**.
     *
     * e.g.
     *
     *     isLenient = true
     *     lineBreakInterval = 0
     *     // JBSWY3DPEBLW64TMMQQQ====
     *
     *     isLenient = true
     *     lineBreakInterval = 16
     *     // JBSWY3DPEBLW64TM
     *     // MQQQ====
     *
     *     isLenient = false
     *     lineBreakInterval = 16
     *     // JBSWY3DPEBLW64TMMQQQ====
     *
     * Enable by setting to a value between 1 and 127, and
     * setting [isLenient] to true.
     *
     * A great value is 64
     * */
    @JvmField
    public var lineBreakInterval: Byte = 0

    /**
     * If true, will output lowercase characters when
     * encoding (against RFC 4648).
     *
     * If false, will output uppercase characters when
     * encoding.
     * */
    @JvmField
    public var encodeToLowercase: Boolean = false

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
        lineBreakInterval = 0
        encodeToLowercase = false
        padEncoded = true
        return this
    }

    /**
     * Builds a [Base32.Default.Config] for the provided settings.
     * */
    public fun build(): Base32.Default.Config {
        val b = io.matthewnelson.encoding.base32.Base32DefaultConfigBuilder()
        b.isLenient = isLenient
        b.lineBreakInterval = lineBreakInterval
        b.encodeToLowercase = encodeToLowercase
        b.padEncoded = padEncoded
        return b.build()
    }
}

/**
 * Deprecated
 *
 * @see [io.matthewnelson.encoding.base32.Base32HexConfigBuilder]
 * */
@Deprecated(
    message = """
        Moved to package io.matthewnelson.encoding.base32
        
        Will be removed in 2.0.0 because of an issue with
        Java 9 modules and JPMS not allowing split packages
        
        See: https://github.com/05nelsonm/encoding/blob/master/MIGRATION.md
    """,
    replaceWith = ReplaceWith(
        expression = "Base32HexConfigBuilder",
        imports = [
            "io.matthewnelson.encoding.base32.Base32HexConfigBuilder"
        ]
    )
)
public class Base32HexConfigBuilder {

    public constructor()
    public constructor(config: Base32.Hex.Config?): this() {
        if (config == null) return
        isLenient = config.isLenient ?: true
        lineBreakInterval = config.lineBreakInterval
        encodeToLowercase = config.encodeToLowercase
        padEncoded = config.padEncoded
    }

    /**
     * If true, spaces and new lines ('\n', '\r', ' ', '\t')
     * will be skipped over when decoding (against RFC 4648).
     *
     * If false, an [EncodingException] will be thrown if
     * those characters are encountered when decoding.
     * */
    @JvmField
    public var isLenient: Boolean = true

    /**
     * For every [lineBreakInterval] of encoded data, a
     * line break will be output.
     *
     * Will **ONLY** output line breaks if [isLenient] is
     * set to **true**.
     *
     * e.g.
     *
     *     isLenient = true
     *     lineBreakInterval = 0
     *     // 91IMOR3F41BMUSJCCGGG====
     *
     *     isLenient = true
     *     lineBreakInterval = 16
     *     // 91IMOR3F41BMUSJC
     *     // CGGG====
     *
     *     isLenient = false
     *     lineBreakInterval = 16
     *     // 91IMOR3F41BMUSJCCGGG====
     *
     * Enable by setting to a value between 1 and 127, and
     * setting [isLenient] to true.
     *
     * A great value is 64
     * */
    @JvmField
    public var lineBreakInterval: Byte = 0

    /**
     * If true, will output lowercase characters when
     * encoding (against RFC 4648).
     *
     * If false, will output uppercase characters when
     * encoding.
     * */
    @JvmField
    public var encodeToLowercase: Boolean = false

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
        lineBreakInterval = 0
        encodeToLowercase = false
        padEncoded = true
        return this
    }

    /**
     * Builds a [Base32.Hex.Config] for the provided settings.
     * */
    public fun build(): Base32.Hex.Config {
        val b = io.matthewnelson.encoding.base32.Base32HexConfigBuilder()
        b.isLenient = isLenient
        b.lineBreakInterval = lineBreakInterval
        b.encodeToLowercase = encodeToLowercase
        b.padEncoded = padEncoded
        return b.build()
    }
}
