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
@file:Suppress("RedundantVisibilityModifier", "RemoveRedundantQualifierName")

package io.matthewnelson.encoding.core

import io.matthewnelson.encoding.core.internal.calculatedOutputNegativeEncodingSizeException
import io.matthewnelson.encoding.core.internal.closedException
import io.matthewnelson.encoding.core.internal.isSpaceOrNewLine
import io.matthewnelson.encoding.core.util.DecoderInput
import kotlin.jvm.JvmField
import kotlin.jvm.JvmStatic

/**
 * Base abstraction which exposes [Encoder] and [Decoder] (sealed classes) such that inheriting
 * classes must implement both.
 *
 * @see [Config]
 * @see [Feed]
 * @see [Encoder]
 * @see [Decoder]
 * */
public abstract class EncoderDecoder<C: EncoderDecoder.Config>(config: C): Encoder<C>(config) {

    /**
     * Base configuration for an [EncoderDecoder]. More options may be specified by the implementation.
     * */
    public abstract class Config(

        /**
         * If `true`, the characters ('\n', '\r', ' ', '\t') will be skipped over (i.e.
         * allowed but ignored) during decoding operations. If `false`, an [EncodingException]
         * will be thrown when those characters are encountered. If `null`, those characters
         * are passed along to the [Decoder.Feed] implementation as input.
         * */
        @JvmField
        public val isLenient: Boolean?,

        lineBreakInterval: Byte,

        /**
         * The character that is used when padding encoded output. This is used by [Decoder.Feed]
         * to mark input as "completing" such that further non-padding input can be exceptionally
         * rejected. If the encoding specification does not use padding, `null` may be specified.
         *
         * **NOTE:** [Decoder.Feed] will not pass along padding characters to the [Decoder.Feed]
         * implementation; they will be automatically dropped. If this is undesirable, consider
         * specifying `null` and managing it in the implementation.
         * */
        @JvmField
        public val paddingChar: Char?,

        /**
         * When the [Encoder.encodeToString], [Encoder.encodeToCharArray], and [Decoder.decodeToByteArray]
         * functions are utilized, an initial buffer is allocated based on the pre-calculated return values
         * of [encodeOutSize] or [decodeOutMaxSize] (respectively). After encoding/decoding operations have
         * completed, the initial buffer may be trimmed down to size in the event of an over-allocation. If
         * that happens, the initial buffer is then dropped and the correct sized copy is returned. Prior
         * versions always back-filled the initial buffer when this occurred, but that can be expensive for
         * large data sets and potentially unnecessary if data is known to not be sensitive in nature.
         *
         * If `true`, the initial buffer (if it was trimmed to size) is back-filled. If `false`, back-filling
         * is skipped.
         * */
        @JvmField
        public val backFillBuffers: Boolean,
    ) {

        /**
         * If greater than `0`, when [lineBreakInterval] number of encoded characters have been output,
         * the next encoded character will be preceded with the new line character `\n` when utilizing
         * the [Encoder.encodeToString] and [Encoder.encodeToCharArray] functions.
         *
         * **NOTE:** This setting will always be `0` if [isLenient] is `false`.
         * */
        @JvmField
        public val lineBreakInterval: Byte = if (isLenient != false && lineBreakInterval > 0) {
            lineBreakInterval
        } else {
            0
        }

        // TODO: Deprecate and replace with encodeOutMaxSize(unEncodedSize: Long): Long
        /**
         * Pre-calculates and returns the size of the output, after encoding
         * would occur, based off of the [Config] options set.
         *
         * Will always return a value greater than or equal to 0.
         *
         * @param [unEncodedSize] The size of the data which is to be encoded.
         * @throws [EncodingSizeException] If [unEncodedSize] is negative, or
         *   the calculated size exceeded [Long.MAX_VALUE].
         * */
        @Throws(EncodingSizeException::class)
        public fun encodeOutSize(unEncodedSize: Long): Long = encodeOutSize(unEncodedSize, lineBreakInterval)

        // TODO: Deprecate and replace with encodeOutMaxSize(unEncodedSize: Long): Long
        /**
         * Pre-calculates and returns the size of the output, after encoding
         * would occur, based off of the [Config] options set and expressed
         * [lineBreakInterval].
         *
         * Will always return a value greater than or equal to 0.
         *
         * @param [unEncodedSize] The size of the data which is to be encoded.
         * @param [lineBreakInterval] The interval at which linebreaks are to
         *   be inserted.
         * @throws [EncodingSizeException] If [unEncodedSize] is negative, or
         *   the calculated size exceeded [Long.MAX_VALUE].
         * */
        @Throws(EncodingSizeException::class)
        public fun encodeOutSize(unEncodedSize: Long, lineBreakInterval: Byte): Long {
            if (unEncodedSize < 0L) {
                throw EncodingSizeException("unEncodedSize cannot be negative")
            }

            // return early
            if (unEncodedSize == 0L) return 0L

            var outSize = encodeOutSizeProtected(unEncodedSize)
            if (outSize < 0L) {
                // Long.MAX_VALUE was exceeded and encodeOutSizeProtected
                // did not implement checks to throw an exception.
                throw calculatedOutputNegativeEncodingSizeException(outSize)
            }

            if (lineBreakInterval > 0) {
                var lineBreakCount: Double = (outSize / lineBreakInterval) - 1.0

                if (lineBreakCount > 0.0) {
                    if (lineBreakCount.rem(1.0) > 0.0) {
                        lineBreakCount++
                    }

                    if (outSize > (Long.MAX_VALUE - lineBreakCount)) {
                        throw outSizeExceedsMaxEncodingSizeException(unEncodedSize, Long.MAX_VALUE)
                    }

                    outSize += lineBreakCount.toLong()
                }
            }

            return outSize
        }

        /**
         * Pre-calculates and returns the maximum size of the output, after
         * decoding would occur, for input that is not yet known (i.e.
         * cannot be wrapped in [DecoderInput], such as the contents of a
         * File where only the file size is known) based off of the [Config]
         * options set for the implementation.
         *
         * [decodeOutMaxSizeOrFail] should always be preferred when:
         *  - Input data is known
         *  - Decoded output will be stored in a medium that
         *    has a maximum capacity of Int.MAX_VALUE, such as
         *    an Array.
         *
         * Encoded data may contain spaces or new lines which are
         * ignored if [isLenient] is set to **true**, or the encoding spec
         * may allow for certain characters which are to be ignored (Base32
         * Crockford ignores hyphens). The output of this function can be
         * incorrect in those instances and the actual decoded output size
         * may be smaller than the value returned here.
         *
         * This is a "best guess" and assumes that every character for
         * [encodedSize] will be decoded.
         *
         * Will always return a value greater than or equal to 0.
         *
         * @see [decodeOutMaxSizeOrFail]
         * @param [encodedSize] The size of the encoded data being decoded.
         * @throws [EncodingSizeException] If [encodedSize] is negative, or
         *   the calculated size exceeded [Long.MAX_VALUE].
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
                // Long.MAX_VALUE was exceeded and decodeOutMaxSizeProtected
                // did not implement checks to throw an exception.
                throw calculatedOutputNegativeEncodingSizeException(outSize)
            }
            return outSize
        }

        /**
         * Pre-calculates and returns the maximum size of the output, after
         * decoding would occur, for input that is known based off of the
         * [Config] options set for the implementation.
         *
         * Encoded data may contain spaces or new lines which are
         * ignored if [isLenient] is set to **true**, or the encoding spec
         * may allow for certain characters which are to be ignored (Base32
         * Crockford ignores hyphens). The output of this function can be
         * incorrect in those instances and the actual decoded output size
         * may be smaller than the value returned here.
         *
         * This is a "best guess" and assumes that every character for
         * [input] will be decoded.
         *
         * Will always return a value greater than or equal to 0.
         *
         * @param [input] The data which is to be decoded.
         * @see [DecoderInput]
         * @throws [EncodingSizeException] If the calculates size exceeded
         *   [Int.MAX_VALUE].
         * @throws [EncodingException] If the implementation has integrity
         *   checks to fail quickly and verification of the [input]
         *   failed (e.g. Base32 Crockford's checkSymbol).
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
                // Long.MAX_VALUE was exceeded and decodeOutMaxSizeOrFailProtected
                // did not implement checks to throw an exception.
                throw calculatedOutputNegativeEncodingSizeException(outSize)
            }
            return outSize
        }

        /**
         * Will only receive values greater than 0.
         *
         * Implementations of this function **should not** take [lineBreakInterval]
         * into consideration when pre-calculating the output size; that is already
         * handled by [encodeOutSize] based off of the return value for this function.
         *
         * @see [encodeOutSize]
         * */
        @Throws(EncodingSizeException::class)
        protected abstract fun encodeOutSizeProtected(unEncodedSize: Long): Long

        /**
         * Will only receive values greater than 0.
         *
         * Implementations of this function **should not** take [lineBreakInterval]
         * into consideration when pre-calculating the output size. Data being
         * decoded may not have been encoded using this [EncoderDecoder].
         *
         * @see [decodeOutMaxSize]
         * */
        @Throws(EncodingException::class)
        protected abstract fun decodeOutMaxSizeProtected(encodedSize: Long): Long

        /**
         * Will only receive values greater than 0.
         *
         * Implementations of this function **should not** take [lineBreakInterval]
         * into consideration when pre-calculating the output size. Data being
         * decoded may not have been encoded using this [EncoderDecoder].
         *
         * @see [decodeOutMaxSizeOrFail]
         * */
        @Throws(EncodingException::class)
        protected abstract fun decodeOutMaxSizeOrFailProtected(encodedSize: Int, input: DecoderInput): Int

        /**
         * Will be called whenever [toString] is invoked, allowing
         * inheritors of [Config] to add their settings to the output.
         *
         * e.g.
         *
         *     protected override fun toStringAddSettings(): Set<Setting> {
         *         return LinkedHashSet<Setting>(3, 1.0f).apply {
         *             add(Setting(name = "setting1", value = setting1))
         *             add(Setting(name = "setting2", value = setting2))
         *         }
         *     }
         *
         * @see [Setting]
         * @see [toString]
         * */
        protected abstract fun toStringAddSettings(): Set<Setting>

        /**
         * Additional setting to [Config], unique to the implementing class. Used
         * in the [toString] output
         * */
        protected inner class Setting(name: String, @JvmField public val value: Any?) {

            @JvmField
            public val name: String = name.trim()

            /** @suppress */
            override fun equals(other: Any?): Boolean = other is Setting && other.name == name
            /** @suppress */
            override fun hashCode(): Int = 17 * 31 + name.hashCode()
            /** @suppress */
            override fun toString(): String = "$name: $value"
        }

        // Cached so is only ever called once and immediately
        // converted to what is needed for equals/hashCode/toString
        private val _toStringAddSettings: List<String> by lazy { toStringAddSettings().map { it.toString() } }

        /** @suppress */
        public final override fun equals(other: Any?): Boolean {
            if (other !is Config) return false
            if (other.isLenient != this.isLenient) return false
            if (other.paddingChar != this.paddingChar) return false
            if (other.lineBreakInterval != this.lineBreakInterval) return false
            if (other.backFillBuffers != this.backFillBuffers) return false
            if (other::class != this::class) return false
            return other._toStringAddSettings == this._toStringAddSettings
        }

        /** @suppress */
        public final override fun hashCode(): Int {
            var result = 17
            result = result * 31 + isLenient.hashCode()
            result = result * 31 + paddingChar.hashCode()
            result = result * 31 + lineBreakInterval.hashCode()
            result = result * 31 + backFillBuffers.hashCode()
            result = result * 31 + this::class.hashCode()
            result = result * 31 + _toStringAddSettings.hashCode()
            return result
        }

        /** @suppress */
        public final override fun toString(): String = StringBuilder().apply {
            appendLine("EncoderDecoder.Config [")
            append("    isLenient: ")
            appendLine(isLenient)
            append("    lineBreakInterval: ")
            appendLine(lineBreakInterval)
            append("    paddingChar: ")
            append(paddingChar)
            append("    backFillBuffers: ")
            append(backFillBuffers)

            for (setting in _toStringAddSettings) {
                appendLine()
                append("    ")
                append(setting)
            }

            appendLine()
            append(']')
        }.toString()

        public companion object {

            /**
             * Helper for generating an [EncodingSizeException] when the
             * pre-calculated encoded/decoded output size exceeds the maximum for
             * the given encoding/decoding specification.
             * */
            @JvmStatic
            public fun outSizeExceedsMaxEncodingSizeException(
                inputSize: Number,
                maxSize: Number,
            ): EncodingSizeException = EncodingSizeException(
                "Size[$inputSize] of input would exceed the maximum output Size[$maxSize] for this operation."
            )
        }

        /**
         * DEPRECATED
         * @suppress
         * */
        @Deprecated(
            message = "Parameter backFillBuffers was added. Use new constructor.",
            replaceWith = ReplaceWith(
                expression = "EncoderDecoder.Config(isLenient, lineBreakInterval, paddingChar, backFillBuffers = true)"),
            level = DeprecationLevel.WARNING,
        )
        public constructor(
            isLenient: Boolean?,
            lineBreakInterval: Byte,
            paddingChar: Char?,
        ): this(
            isLenient = isLenient,
            lineBreakInterval = lineBreakInterval,
            paddingChar = paddingChar,
            backFillBuffers = true,
        )
    }

    /**
     * The base abstraction for [Decoder.Feed] and [Encoder.Feed].
     *
     * [Feed]s are meant to be single use disposables for the
     * given encoding/decoding operation.
     *
     * TLDR; [Feed]s only care about [Byte]s and [Char]s, not the medium
     * for which they come from or are going to. Use the [use] extension
     * function.
     *
     * Their primary use case is for breaking the process of encoding
     * and decoding into their individual parts. This allows for input
     * and output type transformations to occur at the call site, instead
     * of within the encoding/decoding process.
     *
     * After a [Feed] consumes all the data you have for it via
     * [Decoder.Feed.consume]/[Encoder.Feed.consume], call [doFinal] to
     * complete the encoding/decoding operation.
     *
     * Alternatively, utilize the [use] extension function (highly
     * recommended) which will call [doFinal] (or [close] if there was
     * an error with the operation) for you.
     *
     * If encoding/decoding multiple chunks of data (e.g. encoding 2
     * ByteArrays and concatenating them with a separator character),
     * you can call [flush] between chunks to perform final operations
     * on that chunk without closing the [Feed].
     *
     * @see [use]
     * @see [Encoder.Feed]
     * @see [Decoder.Feed]
     * */
    public sealed class Feed<C: EncoderDecoder.Config>(public val config: C) {

        /**
         * Flushes any buffered input of the [Feed] without
         * closing it, performing final encoding/decoding
         * operations for that chunk of data.
         *
         * Useful in the event you are performing encoding/decoding
         * operations with a single feed on multiple inputs.
         *
         * @see [Decoder.Feed.flush]
         * @see [Encoder.Feed.flush]
         * @throws [EncodingException] if [isClosed] is true, or
         *   there was an error encoding/decoding.
         * */
        @Throws(EncodingException::class)
        public abstract fun flush()

        /**
         * [close]s the [Decoder.Feed]/[Encoder.Feed] and finalizes the
         * encoding/decoding, such as applying padding (encoding), or
         * processing remaining data in its buffer before dumping them
         * to [Decoder.OutFeed]/[Encoder.OutFeed].
         *
         * Can only be called once. Any successive calls to [doFinal],
         * [Decoder.Feed.consume], or [Encoder.Feed.consume] will be
         * considered an error and throw an [EncodingException].
         *
         * @see [use]
         * @see [close]
         * @throws [EncodingException] if [isClosed] is true, or
         *   there was an error encoding/decoding.
         * */
        @Throws(EncodingException::class)
        public fun doFinal() {
            if (isClosed()) throw closedException()

            // Close the feed before calling doFinalProtected
            // so feed implementations can check if flush or doFinal
            // was called (if necessary).
            close()

            doFinalProtected()
        }

        /**
         * Closes the feed rendering it useless.
         *
         * [close] can be called as many times as desired and
         * will not be considered an error if already closed.
         *
         * After [close] has been called, any invocation of
         * [Decoder.Feed.consume], [Encoder.Feed.consume],
         * or [doFinal] will be considered an error and throw an
         * [EncodingException].
         *
         * @see [use]
         * */
        public abstract fun close()

        public abstract fun isClosed(): Boolean

        /**
         * Implementations should perform final operations on
         * their buffered input, **AND** reset any stateful
         * variables they may have. This is called by both
         * [flush] and [doFinal].
         *
         * Implementations can check which was called, if
         * necessary, by:
         *  - If [isClosed] is true, [doFinal] was invoked.
         *  - If [isClosed] is false, [flush] was invoked.
         * */
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
     *     override fun name() = "UTF-8"
     * */
    protected abstract fun name(): String

    /** @suppress */
    public final override fun equals(other: Any?): Boolean {
        if (other !is EncoderDecoder<*>) return false
        if (other.name() != this.name()) return false
        // Config equals override checks config ::class equality
        // so if and only if the other EncoderDecoder has the same
        // Config class type will the EncoderDecoder equal this one.
        return other.config == this.config
    }

    /** @suppress */
    public final override fun hashCode(): Int {
        var result = 17
        result = result * 31 + name().hashCode()
        result = result * 31 + config.hashCode()
        return result
    }

    /** @suppress */
    public final override fun toString(): String {
        return "EncoderDecoder[${name()}]@${hashCode()}"
    }
}
