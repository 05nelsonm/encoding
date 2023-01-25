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
package io.matthewnelson.encoding.builders

import io.matthewnelson.encoding.base64.Base64
import io.matthewnelson.encoding.core.EncodingException
import kotlin.jvm.JvmField
import kotlin.jvm.JvmOverloads

/**
 * Creates a configured [Base64] encoder/decoder.
 *
 * @param [config] inherit settings from.
 * @see [Base64ConfigBuilder]
 * */
public fun Base64(
    config: Base64.Config?,
    block: Base64ConfigBuilder.() -> Unit,
): Base64 {
    val builder = Base64ConfigBuilder(config)
    block.invoke(builder)
    return Base64(builder.build())
}

/**
 * Creates a configured [Base64] encoder/decoder.
 *
 * @see [Base64ConfigBuilder]
 * */
public fun Base64(
    block: Base64ConfigBuilder.() -> Unit,
): Base64 {
    return Base64(null, block)
}

/**
 * Creates a configured [Base64] encoder/decoder
 * using the default settings.
 *
 * @param [strict] If true, configures the encoder/decoder
 *   to be in strict accordance with RFC 4648.
 * @see [Base64ConfigBuilder]
 * */
@JvmOverloads
public fun Base64(strict: Boolean = false): Base64 = Base64 { if (strict) strict() }

/**
 * Builder for creating a [Base64.Config].
 *
 * @see [strict]
 * @see [io.matthewnelson.encoding.builders.Base64]
 * */
public class Base64ConfigBuilder {

    public constructor()
    public constructor(config: Base64.Config?): this() {
        if (config == null) return
        isLenient = config.isLenient ?: true
        lineBreakInterval = config.lineBreakInterval
        encodeToUrlSafe = config.encodeToUrlSafe
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
     *     // SGVsbG8gV29ybGQh
     *
     *     isLenient = true
     *     lineBreakInterval = 10
     *     // SGVsbG8gV2
     *     // 9ybGQh
     *
     *     isLenient = false
     *     lineBreakInterval = 10
     *     // SGVsbG8gV29ybGQh
     *
     * Enable by setting to a value between 1 and 127, and
     * setting [isLenient] to true.
     *
     * A great value is 64
     * */
    @JvmField
    public var lineBreakInterval: Byte = 0

    /**
     * If true, will output Base64 UrlSafe characters
     * when encoding.
     *
     * If false, will output Base64 Default characters
     * when encoding.
     * */
    @JvmField
    public var encodeToUrlSafe: Boolean = false

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
    public fun strict(): Base64ConfigBuilder {
        isLenient = false
        padEncoded = true
        return this
    }

    public fun build(): Base64.Config = Base64.Config.from(this)
}
