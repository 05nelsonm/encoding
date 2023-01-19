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
import io.matthewnelson.encoding.core.util.char
import kotlin.jvm.JvmField

/**
 * Base abstraction which expose [Encoder] and [Decoder] (sealed
 * classes) such that inheriting classes must implement both.
 *
 * @see [Configuration]
 * @see [Feed]
 * @sample [io.matthewnelson.encoding.base16.Base16]
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
     * @param [paddingByte] The byte used when padding the output for
     *   the given encoding; NOT "if padding should be
     *   used". (e.g. '='.code.toByte()).
     *   If the encoding specification does not ues padding, pass `null`.
     * @sample [io.matthewnelson.encoding.base16.Base16.Configuration]
     * */
    public abstract class Configuration
    @ExperimentalEncodingApi
    constructor(
        @JvmField
        public val isLenient: Boolean,
        @JvmField
        public val paddingByte: Byte?,
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
         *
         * [isLenient] and [paddingByte] are automatically added.
         *
         * Output of [toString] is used in [equals] and [hashCode], so
         * this affects their outcome.
         *
         * e.g.
         *   override fun toStringAddSettings(sb: StringBuilder) {
         *       with(sb) {
         *           // already starting on a new line
         *           append("    setting1: ") // 4 space indent + colon + single space
         *           append(setting1)
         *           appendLine()             // Add new line if multiple settings
         *           append("    setting2: ")
         *           append(setting2)
         *           // a new line is automatically added after
         *       }
         *   }
         *
         * @see [toString]
         * @sample [io.matthewnelson.encoding.base16.Base16.Configuration.toStringAddSettings]
         * */
        protected abstract fun toStringAddSettings(sb: StringBuilder)

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
                append(paddingByte?.char)
                appendLine()
                toStringAddSettings(this)
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

    /**
     * Base abstraction for encoding/decoding data.
     *
     * After pushing all data through [update], call
     * [doFinal] to complete encoding/decoding.
     *
     * @see [Encoder.Feed]
     * @see [Decoder.Feed]
     * */
    public sealed class Feed {
        public var isClosed: Boolean = false
            private set

        // Only throws exception if decoding
        @Throws(EncodingException::class)
        protected abstract fun updateProtected(input: Byte)

        // Only throws exception if decoding
        @Throws(EncodingException::class)
        protected abstract fun doFinalProtected()

        @ExperimentalEncodingApi
        @Throws(EncodingException::class)
        public fun update(input: Byte) {
            if (isClosed) throw closedException()
            updateProtected(input)
        }

        @ExperimentalEncodingApi
        @Throws(EncodingException::class)
        public fun doFinal() {
            if (isClosed) throw closedException()
            isClosed = true
            doFinalProtected()
        }
    }
}
