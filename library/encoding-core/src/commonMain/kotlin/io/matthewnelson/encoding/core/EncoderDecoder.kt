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
@file:Suppress("SpellCheckingInspection")

package io.matthewnelson.encoding.core

import io.matthewnelson.encoding.core.util.DecoderInput
import kotlin.jvm.JvmField

/**
 * Base abstraction which expose [Encoder] and [Decoder] (sealed
 * classes) such that inheriting classes must implement
 * both.
 *
 * @see [Configuration]
 * */
public abstract class EncoderDecoder
@ExperimentalEncodingApi
constructor(config: Configuration): Encoder(config) {

    /**
     * The name of the [EncoderDecoder]. Utilized in the
     * output of [toString].
     *
     * e.g.
     *   Base16
     *   Base32.Hex
     *   Base64.UrlSafe
     * */
    protected abstract fun name(): String

    /**
     * Base configuration for an [EncoderDecoder]. More options
     * may be specified by the implementing class.
     *
     * @param [isLenient] If true, decoding will skip over spaces
     *   and new lines ('\n', '\r', ' ', '\t'). If false, an
     *   [EncodingException] will be thrown when encountering those
     *   characters.
     * @param [paddingChar] The character used when padding the
     *   output for the given encoding; NOT "if padding should be
     *   used". If the encoding specification does not ues padding,
     *   pass `null`.
     * */
    public abstract class Configuration
    @ExperimentalEncodingApi
    constructor(
        @JvmField
        public val isLenient: Boolean,
        @JvmField
        public val paddingChar: Char?,
    ) {

        /**
         * Calculates and returns the maximum size of the output after
         * decoding occurs, based off of the configuration options set
         * for the [Configuration] implementation.
         *
         * @see [DecoderInput]
         * @param [encodedSize] size of the data being decoded.
         * @param [input] Provided for additional analysis in the event
         *   the decoding specification has checks to fail early.
         * */
        @Throws(EncodingException::class)
        public abstract fun decodeOutMaxSizeOrFail(encodedSize: Int, input: DecoderInput): Int

        /**
         * Calculates and returns the size of the output after encoding
         * occurs, based off of the configuration options set for the
         * [Configuration] implementation.
         * */
        public abstract fun encodeOutSize(unencodedSize: Int): Int

        /**
         * Will be called whenever [toString] is invoked, allowing
         * inheritors of [Configuration] to add their settings to
         * the output.
         * */
        protected abstract fun toString(sb: StringBuilder)

        final override fun equals(other: Any?): Boolean {
            return  other is Configuration
                    && other::class == this::class
                    && other.toString() == toString()
        }

        final override fun hashCode(): Int {
            return 17 * 31 + toString().hashCode()
        }

        final override fun toString(): String {
            return StringBuilder().apply {
                append("EncoderDecoder.Configuration [")
                appendLine()
                append("    isLenient: ")
                append(isLenient)
                appendLine()
                append("    paddingChar: ")
                append(paddingChar)
                appendLine()
                toString(this)
                appendLine()
                append(']')
            }.toString()
        }
    }

    final override fun equals(other: Any?): Boolean {
        return  other is EncoderDecoder
                && other::class == this::class
                && other.name() == name()
                && other.config.hashCode() == config.hashCode()
    }

    final override fun hashCode(): Int {
        var result = 17
        result = result * 31 + toString().hashCode()
        result = result * 31 + config.hashCode()
        return result
    }

    final override fun toString(): String {
        return "EncoderDecoder[${name()}]"
    }
}
