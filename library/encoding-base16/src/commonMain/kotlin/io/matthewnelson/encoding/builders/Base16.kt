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

import io.matthewnelson.encoding.base16.Base16
import io.matthewnelson.encoding.base16.Base16.Config
import io.matthewnelson.encoding.core.EncodingException
import kotlin.jvm.JvmField
import kotlin.jvm.JvmOverloads

/**
 * Creates a configured [Base16] encoder/decoder.
 *
 * @param [config] inherit settings from.
 * @see [Base16ConfigBuilder]
 * */
public fun Base16(
    config: Config?,
    block: Base16ConfigBuilder.() -> Unit
): Base16 {
    val builder = Base16ConfigBuilder(config)
    block.invoke(builder)
    return Base16(builder.build())
}

/**
 * Creates a configured [Base16] encoder/decoder.
 *
 * @see [Base16ConfigBuilder]
 * */
public fun Base16(
    block: Base16ConfigBuilder.() -> Unit
): Base16 {
    return Base16(config = null, block)
}

/**
 * Creates a configured [Base16] encoder/decoder
 * using the default settings.
 *
 * @param [strict] If true, configures the encoder/decoder
 *   to be in strict accordance with RFC 4648.
 * @see [Base16ConfigBuilder]
 * */
@JvmOverloads
public fun Base16(strict: Boolean = false): Base16 = Base16 { if (strict) strict() }


/**
 * Builder for creating a [Base16.Config].
 *
 * @see [strict]
 * @see [io.matthewnelson.encoding.builders.Base16]
 * */
public class Base16ConfigBuilder {

    public constructor()
    public constructor(config: Config?): this() {
        if (config == null) return
        isLenient = config.isLenient
        acceptLowercase = config.acceptLowercase
        encodeToLowercase = config.encodeToLowercase
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
     * A shortcut for configuring things to be in strict
     * adherence with RFC 4648.
     * */
    public fun strict(): Base16ConfigBuilder {
        isLenient = false
        acceptLowercase = false
        encodeToLowercase = false
        return this
    }

    /**
     * Builds a [Base16.Config] for the provided settings.
     * */
    public fun build(): Config = Config.from(this)
}
