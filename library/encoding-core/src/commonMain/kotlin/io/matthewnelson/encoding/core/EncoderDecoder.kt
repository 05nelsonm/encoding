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
@file:Suppress("SpellCheckingInspection", "RemoveRedundantQualifierName")

package io.matthewnelson.encoding.core

import io.matthewnelson.encoding.core.internal.calculatedOutputNegativeEncodingSizeException
import io.matthewnelson.encoding.core.internal.closedException
import io.matthewnelson.encoding.core.internal.isSpaceOrNewLine
import io.matthewnelson.encoding.core.util.DecoderInput
import kotlin.jvm.JvmField
import kotlin.jvm.JvmStatic

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
public abstract class EncoderDecoder<C: EncoderDecoder.Config>
@ExperimentalEncodingApi
constructor(config: C): Encoder<C>(config) {

    /**
     * Base configuration for an [EncoderDecoder]. More options
     * may be specified by the implementing class.
     *
     * @param [isLenient] If true, decoding will skip over spaces
     *   and new lines ('\n', '\r', ' ', '\t'). If false, an
     *   [EncodingException] will be thrown when encountering those
     *   characters. See [isSpaceOrNewLine]. If null, those bytes
     *   are sent to the [EncoderDecoder].
     * @param [lineBreakInterval] If greater than 0 and [isLenient]
     *   is not **false** (i.e. null or true), line breaks will be
     *   output at the expressed [lineBreakInterval].
     * @param [paddingChar] The byte used when padding the output for
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
        lineBreakInterval: Byte,
        @JvmField
        public val paddingChar: Char?,
    ) {

        @JvmField
        public val lineBreakInterval: Byte = if (isLenient != false && lineBreakInterval > 0) {
            lineBreakInterval
        } else {
            0
        }

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
                throw calculatedOutputNegativeEncodingSizeException(outSize)
            }
            return outSize
        }

        /**
         * Calculates and returns the maximum size of the output after
         * decoding would occur, based off of the [Config] options set
         * for the implementation. Should always prefer using
         * [decodeOutMaxSizeOrFail] if input data is already known.
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
         * @see [decodeOutMaxSizeOrFail]
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
                throw calculatedOutputNegativeEncodingSizeException(outSize)
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

                if (c == paddingChar) {
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
            if (outSize < 0) {
                throw calculatedOutputNegativeEncodingSizeException(outSize)
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
         * [isLenient] and [paddingChar] are automatically added.
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
                append("    lineBreakInterval: ")
                append(lineBreakInterval)
                appendLine()
                append("    paddingChar: ")
                append(paddingChar)
                appendLine()
                toStringAddSettings(this)
                appendLine()
                append(']')
            }.toString()
        }

        public companion object {

            /**
             * Helper for generating an [EncodingSizeException] when the
             * pre-calculated encoded/decoded output size exceeds the maximum for
             * the given encoding/decoding specification.
             * */
            @JvmStatic
            public fun outSizeExceedsMaxEncodingSizeException(
                inputSize: Number,
                maxSize: Number
            ): EncodingSizeException {
                return EncodingSizeException(
                    "Size[$inputSize] of input would exceed the maximum output Size[$maxSize] for this operation."
                )
            }
        }
    }

    /**
     * Base abstraction for encoding/decoding of data.
     *
     * After feeding all data through [Decoder.Feed.consume] or
     * [Encoder.Feed.consume], call [doFinal] to complete encoding/decoding.
     * Alternatively, utilize the [use] extension function which will
     * call [doFinal] for you when you're done feeding data through,
     * or will call [close] in the event there is an error while
     * encoding/decoding.
     *
     * @see [use]
     * @see [Encoder.Feed]
     * @see [Decoder.Feed]
     * */
    public sealed class Feed<C: EncoderDecoder.Config>(public val config: C) {

        /**
         * [close]s the [Decoder.Feed]/[Encoder.Feed] and finalizes the
         * encoding/decoding, such as applying padding (encoding), or
         * processing remaining data in its buffer before dumping them
         * to [Decoder.OutFeed]/[Encoder.OutFeed].
         *
         * Can only be called once. Any sucessive calls to [doFinal],
         * [Decoder.Feed.consume], or [Encoder.Feed.consume] will be
         * considered an error and throw an [EncodingException].
         *
         * @see [use]
         * @see [close]
         * @throws [EncodingException] if [isClosed] is true, or
         *   there was an error encoding/decoding.
         * */
        @ExperimentalEncodingApi
        @Throws(EncodingException::class)
        public fun doFinal() {
            if (isClosed()) throw closedException()
            close()
            doFinalProtected()
        }

        /**
         * Closes the feed rendering it useless.
         *
         * After [close] has been called, any invocation of
         * [Decoder.Feed.consume], [Encoder.Feed.consume],
         * or [doFinal] will be considered an error and throw an
         * [EncodingException].
         *
         * [close] can be called as many times as desired and
         * will not be considered an error if already closed.
         *
         * @see [use]
         * */
        @ExperimentalEncodingApi
        public abstract fun close()

        public abstract fun isClosed(): Boolean

        @Throws(EncodingException::class)
        protected abstract fun doFinalProtected()
    }

    /**
     * The name of the [EncoderDecoder]. This is utilized in the
     * output of [toString], [equals], and [hashCode].
     *
     * e.g.
     *
     *     override fun name() = "Base16"
     *     override fun name() = "Base32.Crockford"
     *     override fun name() = "Base64"
     * */
    protected abstract fun name(): String

    final override fun equals(other: Any?): Boolean {
        return  other is EncoderDecoder<*>
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
