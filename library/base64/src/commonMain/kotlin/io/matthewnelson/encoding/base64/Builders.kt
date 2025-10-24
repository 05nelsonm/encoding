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

package io.matthewnelson.encoding.base64

import kotlin.jvm.JvmField
import kotlin.jvm.JvmOverloads
import kotlin.jvm.JvmSynthetic

/**
 * DEPRECATED
 * @suppress
 * @see [Base64.Builder]
 * @see [Base64.Companion.Builder]
 * */
@Deprecated(
    message = "Use Base64.Builder or Base64.Companion.Builder",
    level = DeprecationLevel.WARNING,
)
public fun Base64(
    config: Base64.Config?,
    block: Base64ConfigBuilder.() -> Unit,
): Base64 = Base64ConfigBuilder(config).apply(block).buildCompat()

/**
 * DEPRECATED
 * @suppress
 * @see [Base64.Builder]
 * @see [Base64.Companion.Builder]
 * */
@Deprecated(
    message = "Use Base64.Builder or Base64.Companion.Builder",
    level = DeprecationLevel.WARNING,
)
public fun Base64(
    block: Base64ConfigBuilder.() -> Unit,
): Base64 = Base64(null, block)

/**
 * DEPRECATED
 * @suppress
 * @see [Base64.Builder]
 * @see [Base64.Companion.Builder]
 * */
@Deprecated(
    message = "Use Base64.Builder or Base64.Companion.Builder",
    level = DeprecationLevel.WARNING,
)
@JvmOverloads
public fun Base64(
    strict: Boolean = false,
): Base64 = Base64.Builder { if (strict) strictSpec() }

/**
 * DEPRECATED
 * @suppress
 * @see [Base64.Builder]
 * @see [Base64.Companion.Builder]
 * */
@Deprecated(
    message = "Use Base64.Builder or Base64.Companion.Builder",
    level = DeprecationLevel.WARNING,
)
public class Base64ConfigBuilder {

    private val compat: Base64.Builder

    public constructor(): this(config = null)
    public constructor(config: Base64.Config?) {
        compat = Base64.Builder(other = config)
        isLenient = compat._isLenient
        lineBreakInterval = compat._lineBreakInterval
        encodeToUrlSafe = compat._encodeUrlSafe
        padEncoded = compat._padEncoded
    }

    /**
     * Refer to [Base64.Builder.isLenient] documentation.
     * */
    @JvmField
    public var isLenient: Boolean = true

    /**
     * Refer to [Base64.Builder.lineBreak] documentation.
     * */
    @JvmField
    public var lineBreakInterval: Byte = 0

    /**
     * Refer to [Base64.Builder.encodeUrlSafe] documentation.
     * */
    @JvmField
    public var encodeToUrlSafe: Boolean = false

    /**
     * Refer to [Base64.Builder.padEncoded] documentation.
     * */
    @JvmField
    public var padEncoded: Boolean = true

    /**
     * Refer to [Base64.Builder.strictSpec] documentation.
     * */
    public fun strict(): Base64ConfigBuilder {
        isLenient = false
        padEncoded = true
        return this
    }

    /**
     * Refer to [Base64.Builder.build] documentation.
     * */
    public fun build(): Base64.Config = buildCompat().config

    @JvmSynthetic
    internal fun buildCompat(): Base64 = compat
        .isLenient(isLenient)
        .lineBreak(lineBreakInterval)
        .encodeUrlSafe(encodeToUrlSafe)
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
