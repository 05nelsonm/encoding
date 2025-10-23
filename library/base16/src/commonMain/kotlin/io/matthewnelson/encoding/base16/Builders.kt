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
@file:Suppress("DEPRECATION")

package io.matthewnelson.encoding.base16

import io.matthewnelson.encoding.base16.Base16.Config
import kotlin.jvm.JvmField
import kotlin.jvm.JvmOverloads
import kotlin.jvm.JvmSynthetic

/**
 * DEPRECATED
 * @suppress
 * @see [Base16.Builder]
 * @see [Base16.Companion.Builder]
 * */
@Deprecated("Use Base16.Builder or Base16.Companion.Builder")
public fun Base16(
    config: Config?,
    block: Base16ConfigBuilder.() -> Unit
): Base16 = Base16ConfigBuilder(config).apply(block).buildCompat()

/**
 * DEPRECATED
 * @suppress
 * @see [Base16.Builder]
 * @see [Base16.Companion.Builder]
 * */
@Deprecated("Use Base16.Builder or Base16.Companion.Builder")
public fun Base16(
    block: Base16ConfigBuilder.() -> Unit,
): Base16 {
    return Base16(config = null, block)
}

/**
 * DEPRECATED
 * @suppress
 * @see [Base16.Builder]
 * @see [Base16.Companion.Builder]
 * */
@JvmOverloads
@Deprecated("Use Base16.Builder or Base16.Companion.Builder")
public fun Base16(strict: Boolean = false): Base16 = Base16.Builder { if (strict) strictSpec() }

/**
 * DEPRECATED
 * @suppress
 * @see [Base16.Builder]
 * @see [Base16.Companion.Builder]
 * */
@Deprecated("Use Base16.Builder or Base16.Companion.Builder")
public class Base16ConfigBuilder {

    private val compat: Base16.Builder

    public constructor(): this(config = null)
    public constructor(config: Config?) {
        compat = Base16.Builder(other = config)
        isLenient = compat._isLenient
        lineBreakInterval = compat._lineBreakInterval
        encodeToLowercase = compat._encodeLowercase
    }

    /**
     * Refer to [Base16.Builder.isLenient] documentation.
     * */
    @JvmField
    public var isLenient: Boolean = true

    /**
     * Refer to [Base16.Builder.lineBreak] documentation.
     * */
    @JvmField
    public var lineBreakInterval: Byte = 0

    /**
     * Refer to [Base16.Builder.encodeLowercase] documentation.
     * */
    @JvmField
    public var encodeToLowercase: Boolean = false

    /**
     * Refer to [Base16.Builder.strictSpec] documentation.
     * */
    public fun strict(): Base16ConfigBuilder {
        isLenient = false
        lineBreakInterval = 0
        encodeToLowercase = false
        return this
    }

    /**
     * Refer to [Base16.Builder.build] documentation.
     * */
    public fun build(): Config = buildCompat().config

    @JvmSynthetic
    internal fun buildCompat(): Base16 = compat
        .isLenient(isLenient)
        .lineBreak(lineBreakInterval)
        .encodeLowercase(encodeToLowercase)
        .build()

    /** @suppress */
    @JvmField
    @Deprecated(message = "Implementation is always constant time. Performance impact is negligible.")
    public var isConstantTime: Boolean = true
}
