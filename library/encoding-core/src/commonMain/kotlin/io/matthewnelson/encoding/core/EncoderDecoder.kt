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

import io.matthewnelson.encoding.core.internal.closedException
import io.matthewnelson.encoding.core.internal.isSpaceOrNewLine
import io.matthewnelson.encoding.core.util.DecoderInput
import io.matthewnelson.encoding.core.util.byte
import io.matthewnelson.encoding.core.util.char
import kotlin.jvm.JvmField
import kotlin.jvm.JvmName

/**
 * Base abstraction which expose [Encoder] and [Decoder] (sealed
 * classes) such that inheriting classes must implement both.
 *
 * @see [Config]
 * @see [Feed]
 * @see [Encoder]
 * @see [Decoder]
 * @sample [io.matthewnelson.encoding.base16.Base16]
 * */
public abstract class EncoderDecoder
@ExperimentalEncodingApi
constructor(config: Config): Encoder(config) {

    /**
     * Base configuration for an [EncoderDecoder]. More options
     * may be specified by the implementing class.
     *
     * @param [isLenient] If true, decoding will skip over spaces
     *   and new lines ('\n', '\r', ' ', '\t'). If false, an
     *   [EncodingException] will be thrown when encountering those
     *   characters. See [isSpaceOrNewLine]. If null, those bytes
     *   are sent to the [EncoderDecoder].
     * @param [paddingByte] The byte used when padding the output for
     *   the given encoding; NOT "if padding should be
     *   used". (e.g. '='.code.toByte()).
     *   If the encoding specification does not ues padding, pass `null`.
     * @sample [io.matthewnelson.encoding.base16.Base16.Config]
     * @sample [io.matthewnelson.encoding.base32.Base32.Default.Config]
     * */
    public abstract class Config
    @ExperimentalEncodingApi
    constructor(
        @JvmField
        public val isLenient: Boolean?,
        @JvmField
        public val paddingByte: Byte?,
    ) {

        /**
         * Calculates and returns the size of the output after encoding
         * would occur, based off of the configuration options set for the
         * [Config] implementation.
         *
         * Will always return a value greater than or equal to 0.
         *
         * @param [unEncodedSize] The size of the data being encoded.
         * @throws [EncodingSizeException] if there was an error calculating
         *   the size, or [unEncodedSize] was negative.
         * */
        @Throws(EncodingSizeException::class)
        public fun encodeOutSize(unEncodedSize: Long): Long {
            if (unEncodedSize < 0L) {
                throw EncodingSizeException("unEncodedSize cannot be negative")
            }

            // return early
            if (unEncodedSize == 0L) return 0L

            val outSize = encodeOutSizeProtected(unEncodedSize)
            if (outSize < 0L) {
                throw EncodingSizeException("Calculated size was negative")
            }
            return outSize
        }

        /**
         * Calculates and returns the maximum size of the output after
         * decoding would occur, based off of the [Config] options set
         * for the implementation.
         *
         * The encoded data may contain spaces or new lines which are
         * ignored if [isLenient] is set to **true**, or the encoding spec
         * may allow for certain characters which are to be ignored (Base32
         * Crockford ignores hyphens). The output of this function can be
         * incorrect in those instances and the actual decoded output size
         * may be different from the value returned here; this is a "best
         * guess".
         *
         * Will always return a value greater than or equal to 0.
         *
         * @param [encodedSize] The size of the encoded data being decoded.
         * @throws [EncodingSizeException] if there was an error calculating
         *   the size, or [encodedSize] was negative.
         * */
        @Throws(EncodingSizeException::class)
        public fun decodeOutMaxSize(encodedSize: Long): Long {
            if (encodedSize < 0L) {
                throw EncodingSizeException("encodedSize cannot be negative")
            }

            // return early
            if (encodedSize == 0L) return 0L

            val outSize = decodeOutMaxSizeProtected(encodedSize)
            if (outSize < 0L) {
                throw EncodingSizeException("Calculated size was negative")
            }
            return outSize
        }

        /**
         * Calculates and returns the maximum size of the output after
         * decoding would occur, based off of the [Config] options set
         * for the implementation.
         *
         * The encoded data may contain spaces or new lines which are
         * ignored if [isLenient] is set to **true**, or the encoding spec
         * may allow for certain characters which are to be ignored (Base32
         * Crockford ignores hyphens). The output of this function can be
         * incorrect in those instances and the actual decoded output size
         * may be different from the value returned here; this is a "best
         * guess".
         *
         * Will always return a value greater than or equal to 0.
         *
         * @param [input] Common input of the data which is to be decoded.
         * @see [DecoderInput]
         * @throws [EncodingSizeException] If there was an error calculating
         *   the size or [decodeOutMaxSizeOrFailProtected] returned a negative
         *   number.
         * @throws [EncodingException] If the implementation has checks to faiil
         *   quickly, and the [input] verification failed (e.g. Base32 Crockford)
         * */
        @Throws(EncodingException::class)
        public fun decodeOutMaxSizeOrFail(input: DecoderInput): Int {
            var lastRelevantChar = input.size

            while (lastRelevantChar > 0) {
                val c = input[lastRelevantChar - 1]

                if (isLenient != null && c.isSpaceOrNewLine()) {
                    if (isLenient) {
                        lastRelevantChar--
                        continue
                    } else {
                        throw EncodingException("Spaces and new lines are forbidden when isLenient[false]")
                    }
                }

                if (c.byte == paddingByte) {
                    lastRelevantChar--
                    continue
                }

                // Found our last relevant character
                // that is not a space, new line, or padding.
                break
            }

            // return early
            if (lastRelevantChar == 0) return 0

            val outSize = decodeOutMaxSizeOrFailProtected(lastRelevantChar, input)
            if (outSize < 0L) {
                throw EncodingSizeException("Calculated size was negative")
            }
            return outSize
        }

        /**
         * Will only receive values greater than 0.
         * */
        @Throws(EncodingSizeException::class)
        protected abstract fun encodeOutSizeProtected(unEncodedSize: Long): Long

        /**
         * Will only receive values greater than 0.
         * */
        @Throws(EncodingException::class)
        protected abstract fun decodeOutMaxSizeProtected(encodedSize: Long): Long

        /**
         * Will only receive values greater than 0.
         * */
        @Throws(EncodingException::class)
        protected abstract fun decodeOutMaxSizeOrFailProtected(encodedSize: Int, input: DecoderInput): Int

        /**
         * Will be called whenever [toString] is invoked, allowing
         * inheritors of [Config] to add their settings to
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
         * @sample [io.matthewnelson.encoding.base16.Base16.Config.toStringAddSettings]
         * @sample [io.matthewnelson.encoding.base32.Base32.Crockford.Config.toStringAddSettings]
         * */
        protected abstract fun toStringAddSettings(sb: StringBuilder)

        final override fun equals(other: Any?): Boolean {
            return  other is Config
                    && other::class == this::class
                    && other.toString() == toString()
        }

        final override fun hashCode(): Int {
            return 17 * 31 + toString().hashCode()
        }

        final override fun toString(): String {
            return StringBuilder().apply {
                append("EncoderDecoder.Config [")
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

    /**
     * Base abstraction for encoding/decoding of data.
     *
     * After feeding all data through [consume], call [doFinal]
     * to complete encoding/decoding. Alternatively, utilize the
     * [use] extension function which will call [doFinal] for you
     * when you're done feeding data through [consume], or will
     * call [close] in the event there is an error while
     * encoding/decoding.
     *
     * @see [use]
     * @see [Encoder.Feed]
     * @see [Decoder.Feed]
     * */
    public sealed class Feed(public val config: Config) {
        @get:JvmName("isClosed")
        public var isClosed: Boolean = false
            private set

        private var isPaddingSet = false

        // Only throws exception if decoding
        @Throws(EncodingException::class)
        protected abstract fun consumeProtected(input: Byte)

        // Only throws exception if decoding
        @Throws(EncodingException::class)
        protected abstract fun doFinalProtected()

        /**
         * Updates the [Feed] with a new byte to encode/decode.
         *
         * @throws [EncodingException] if [isClosed] is true, or
         *   there was an error encoding/decoding.
         * */
        @ExperimentalEncodingApi
        @Throws(EncodingException::class)
        public fun consume(input: Byte) {
            if (isClosed) throw closedException()

            try {
                if (this is Decoder.Feed) {
                    if (config.isLenient != null && input.char.isSpaceOrNewLine()) {
                        if (config.isLenient) {
                            return
                        } else {
                            throw EncodingException("Spaces and new lines are forbidden when isLenient[false]")
                        }
                    }

                    // if paddingByte is null, it will never equal
                    // input, thus never set isPaddingSet
                    if (config.paddingByte == input) {
                        isPaddingSet = true
                        return
                    }

                    if (isPaddingSet) {
                        // Trying to decode something else that is not
                        // a space, new line, or padding. Fail
                        throw EncodingException(
                            "Padding[${config.paddingByte?.char}] was previously passed, " +
                            "but decoding operations are still being attempted."
                        )
                    }
                }

                consumeProtected(input)
            } catch (t: Throwable) {
                close()
                throw t
            }
        }

        /**
         * Closes the [Feed] and finalizes the encoding/decoding, such
         * as applying padding (encoding) or processing remaining bytes
         * in its buffer before dumping them to [OutFeed].
         *
         * Can only be called once. Any sucessive calls will be considered
         * an error and throw an [EncodingException].
         *
         * @see [use]
         * @throws [EncodingException] if [isClosed] is true, or
         *   there was an error encoding/decoding.
         * */
        @ExperimentalEncodingApi
        @Throws(EncodingException::class)
        public fun doFinal() {
            if (isClosed) throw closedException()
            close()
            doFinalProtected()
        }

        /**
         * Closes the feed, rendering it useless.
         *
         * After [close] has been called, any invocation of [consume]
         * or [doFinal] will be considered an error and throw an
         * [EncodingException].
         *
         * @see [use]
         * */
        @ExperimentalEncodingApi
        public fun close() {
            isClosed = true
        }
    }

    /**
     * The name of the [EncoderDecoder]. This is utilized in the
     * output of [toString], [equals], and [hashCode].
     *
     * e.g.
     *   Base16
     *   Base32.Crockford
     *   Base64
     * */
    protected abstract fun name(): String

    final override fun equals(other: Any?): Boolean {
        return  other is EncoderDecoder
                && other::class == this::class
                && other.name() == name()
                && other.config.hashCode() == config.hashCode()
    }

    final override fun hashCode(): Int {
        var result = 17
        result = result * 31 + name().hashCode()
        result = result * 31 + config.hashCode()
        return result
    }

    final override fun toString(): String {
        return "EncoderDecoder[${name()}]@${hashCode()}"
    }
}
