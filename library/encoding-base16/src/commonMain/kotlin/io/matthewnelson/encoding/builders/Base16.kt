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
import io.matthewnelson.encoding.base16.Base16.Configuration
import io.matthewnelson.encoding.core.EncoderDecoder
import io.matthewnelson.encoding.core.EncodingException
import kotlin.jvm.JvmField

/**
 * Creates a configured [Base16] encoder/decoder.
 *
 * @param [config] inherit settings from.
 * @see [Base16Builder]
 * */
public fun Base16(
    config: Configuration?,
    block: Base16Builder.() -> Unit
): Base16 {
    val builder = Base16Builder(config)
    block.invoke(builder)
    return Base16(builder.build())
}

/**
 * Creates a configured [Base16] encoder/decoder.
 *
 * @see [Base16Builder]
 * */
public fun Base16(
    block: Base16Builder.() -> Unit
): Base16 {
    return Base16(config = null, block)
}

/**
 * Builder for creating a [Base16.Configuration].
 *
 * @see [io.matthewnelson.encoding.builders.Base16]
 * */
public class Base16Builder {

    public constructor()
    public constructor(config: Configuration?): this() {
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
     * Builds a [Base16.Configuration] for the provided settings.
     * */
    public fun build(): Configuration = Configuration.from(this)
}
