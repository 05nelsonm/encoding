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
import io.matthewnelson.encoding.core.util.LineBreakOutFeed
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

    public companion object {

        /**
         * A "default" buffer size of `8 * 1024`
         * */
        public const val DEFAULT_BUFFER_SIZE: Int = 8 * 1024 // Note: If changing, update documentation.
    }

    /**
     * Base configuration for an [EncoderDecoder]. More options may be specified by the implementation.
     * */
    public abstract class Config private constructor(

        /**
         * If `true`, the characters ('\n', '\r', ' ', '\t') will be skipped over (i.e.
         * allowed but ignored) during decoding operations. If `false`, a [MalformedEncodingException]
         * will be thrown when those characters are encountered. If `null`, those characters
         * are passed along to the [Decoder.Feed] implementation as input.
         * */
        @JvmField
        public val isLenient: Boolean?,

        /**
         * If greater than `0`, [Encoder.newEncoderFeed] may use a [LineBreakOutFeed] such that
         * for every [lineBreakInterval] number of encoded characters output by the [Encoder.Feed],
         * the next encoded character output will be preceded with a new line character `\n`.
         *
         * **NOTE:** This setting will always be `0` if [isLenient] is `false`.
         *
         * @see [Encoder.newEncoderFeed]
         * */
        @JvmField
        public val lineBreakInterval: Byte,

        /**
         * If and only if [Encoder.newEncoderFeed] wraps the [Encoder.OutFeed] passed to it with a
         * [LineBreakOutFeed], will this setting be used for [LineBreakOutFeed.resetOnFlush]. If
         * `true` and [Encoder.newEncoderFeed] wrapped its provided [Encoder.OutFeed], then
         * [LineBreakOutFeed.reset] will be called after every invocation of [Encoder.Feed.flush].
         *
         * @see [LineBreakOutFeed]
         * @see [Encoder.newEncoderFeed]
         * */
        @JvmField
        public val lineBreakResetOnFlush: Boolean,

        /**
         * The character that is used when padding encoded output. This is used by [Decoder.Feed]
         * to mark input as "completing" such that further non-padding input can be exceptionally
         * rejected with a [MalformedEncodingException]. If the encoding specification does not
         * use padding, `null` may be specified.
         *
         * **NOTE:** [Decoder.Feed] will not pass along padding characters to the [Decoder.Feed]
         * implementation; they will be automatically dropped. If this is undesirable, consider
         * specifying `null` and managing it in the implementation.
         * */
        @JvmField
        public val paddingChar: Char?,

        /**
         * The maximum number of bytes that the implementation's [Decoder.Feed] can potentially
         * emit on a single invocation of [Decoder.Feed.consume], [Decoder.Feed.flush], or
         * [Decoder.Feed.doFinal].
         *
         * For example, `Base16` decoding will emit `1` byte for every `2` characters of input,
         * so its maximum emission is `1`. `Base32` decoding will emit `5` bytes for every `8`
         * characters of input, so its maximum emission is `5`. `UTF8` "decoding" (i.e. text to
         * UTF-8 byte transformations) can emit `4` bytes, but also depending on the size of the
         * replacement byte sequence being used, can emit more; its maximum emission size is
         * required to be calculated, such as `(replacementStrategy.size * 2).coerceAtLeast(4)`.
         *
         * Value will be between `1` and `255` (inclusive), or `-1` which indicates that the
         * [EncoderDecoder.Config] implementation has not updated to the new constructor introduced
         * in version `2.6.0` and as such is unable to be used with `:core` module APIs dependent
         * on this value (such as [Decoder.decodeBuffered] or [Decoder.decodeBufferedAsync]).
         * */
        @JvmField
        public val maxDecodeEmit: Int,

        /**
         * When the functions [Encoder.encodeToString], [Encoder.encodeToCharArray],
         * [Decoder.decodeToByteArray], [Decoder.decodeBuffered], and [Decoder.decodeBufferedAsync]
         * are utilized, they may allocate an appropriate medium (a buffer) to store encoded/decoded
         * data (e.g. a [StringBuilder], [CharArray], or [ByteArray]). Depending on the underlying
         * encoding/decoding operation, such as an array over-allocation due to [encodeOutMaxSize]
         * or [decodeOutMaxSize], those initially allocated buffers may not be returned as the
         * function's result. Prior versions of this library always back-filled them with `0` or a
         * space character, but that can be computationally expensive for large datasets and
         * potentially unnecessary if data is known to not be sensitive in nature.
         *
         * If `true`, any non-result buffer allocations are back-filled before being de-referenced
         * by function return. If `false`, back-filling is skipped.
         * */
        @JvmField
        public val backFillBuffers: Boolean,

        // NOTE: Adding any parameters requires updating equals/hashCode/toString
        @Suppress("UNUSED_PARAMETER") unused: Any?,
    ) {

        /**
         * Instantiates a new [Config] instance.
         *
         * @throws [IllegalArgumentException] If [maxDecodeEmit] is less than `1` or greater than `255`.
         * */
        protected constructor(
            isLenient: Boolean?,
            lineBreakInterval: Byte,
            lineBreakResetOnFlush: Boolean,
            paddingChar: Char?,
            maxDecodeEmit: Int,
            backFillBuffers: Boolean,
        ): this(
            isLenient = isLenient,
            lineBreakInterval = lineBreakIntervalOrZero(isLenient, lineBreakInterval),
            lineBreakResetOnFlush = lineBreakResetOnFlush,
            paddingChar = paddingChar,
            maxDecodeEmit = maxDecodeEmit,
            backFillBuffers = backFillBuffers,
            unused = null,
        ) {
            require(maxDecodeEmit > 0) { "maxDecodeEmit must be greater than 0" }
            require(maxDecodeEmit < 256) { "maxDecodeEmit must be less than 256" }
        }

        /**
         * Pre-calculates and returns the maximum size of the output, after encoding would occur,
         * based off the [Config] options set for the implementation. Most implementations (such
         * as `Base16`, `Base32`, and `Base64`) are able to return an exact size whereby no
         * post-encoding resize is necessary, while others (such as `UTF-8`) return a maximum
         * and may require a post-encoding resize.
         *
         * Will always return a value greater than or equal to `0`.
         *
         * @param [unEncodedSize] The size of the data which is to be encoded.
         *
         * @throws [EncodingSizeException] If [unEncodedSize] is negative, or the calculated
         *   size exceeds [Long.MAX_VALUE].
         * */
        @Throws(EncodingSizeException::class)
        public fun encodeOutMaxSize(unEncodedSize: Long): Long = encodeOutMaxSize(unEncodedSize, lineBreakInterval)

        /**
         * Pre-calculates and returns the maximum size of the output, after encoding would occur,
         * based off the [Config] options set for the implementation and expressed [lineBreakInterval].
         * Most implementations (such as `Base16`, `Base32`, and `Base64`) are able to return an
         * exact size whereby no post-encoding resize is necessary, while others (such as `UTF-8`)
         * return a maximum and may require a post-encoding resize.
         *
         * Will always return a value greater than or equal to `0`.
         *
         * @param [unEncodedSize] The size of the data which is to be encoded.
         * @param [lineBreakInterval] The interval at which new line characters are to be inserted.
         *
         * @throws [EncodingSizeException] If [unEncodedSize] is negative, or the calculated
         *   size exceeds [Long.MAX_VALUE].
         * */
        @Throws(EncodingSizeException::class)
        public fun encodeOutMaxSize(unEncodedSize: Long, lineBreakInterval: Byte): Long {
            if (unEncodedSize < 0L) {
                throw EncodingSizeException("unEncodedSize cannot be negative")
            }

            // Return early
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
         *   the calculated output size exceeded [Long.MAX_VALUE].
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
         * @throws [EncodingSizeException] If the calculated output size
         *   exceeds [Int.MAX_VALUE].
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
                        throw MalformedEncodingException("Spaces and new lines are forbidden when isLenient[false]")
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
         * Calculate and return an exact (preferably), or maximum, size that an encoding would be
         * for the [unEncodedSize] data.
         *
         * Implementations of this function **must not** take [lineBreakInterval] into consideration
         * when pre-calculating the output size; that is already handled by [encodeOutMaxSize] based
         * off of the return value for this function.
         *
         * Will only receive values greater than `0`.
         *
         * @see [encodeOutMaxSize]
         * */
        @Throws(EncodingSizeException::class)
        protected abstract fun encodeOutSizeProtected(unEncodedSize: Long): Long

        /**
         * Calculate and return a maximum size that a decoding would be for the [encodedSize] data.
         *
         * Implementations of this function **must not** take [lineBreakInterval] into consideration
         * when pre-calculating the output size, as data being decoded may not have been encoded using
         * this [Config].
         *
         * Will only receive values greater than `0`.
         *
         * @see [decodeOutMaxSize]
         * */
        @Throws(EncodingException::class)
        protected abstract fun decodeOutMaxSizeProtected(encodedSize: Long): Long

        /**
         * Calculate and return a maximum size that a decoding would be for the [encodedSize] data.
         *
         * Implementations of this function **must not** take [lineBreakInterval] into consideration
         * when pre-calculating the output size, as data being decoded may not have been encoded using
         * this [Config]. Additionally, [input] should only be parsed when absolutely necessary, such
         * as validation of a checksum.
         *
         * Will only receive values greater than `0`.
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
            if (other.lineBreakInterval != this.lineBreakInterval) return false
            if (other.lineBreakResetOnFlush != this.lineBreakResetOnFlush) return false
            if (other.paddingChar != this.paddingChar) return false
            if (other.maxDecodeEmit != this.maxDecodeEmit) return false
            if (other.backFillBuffers != this.backFillBuffers) return false
            if (other::class != this::class) return false
            return other._toStringAddSettings == this._toStringAddSettings
        }

        /** @suppress */
        public final override fun hashCode(): Int {
            var result = 17
            result = result * 31 + isLenient.hashCode()
            result = result * 31 + lineBreakInterval.hashCode()
            result = result * 31 + lineBreakResetOnFlush.hashCode()
            result = result * 31 + paddingChar.hashCode()
            result = result * 31 + maxDecodeEmit.hashCode()
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
            append("    lineBreakResetOnFlush: ")
            appendLine(lineBreakResetOnFlush)
            append("    paddingChar: ")
            appendLine(paddingChar)
            append("    maxDecodeEmit: ")
            appendLine(maxDecodeEmit)
            append("    backFillBuffers: ")
            append(backFillBuffers) // last one uses append, not appendLine

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
         * DEPRECATED since `2.6.0`
         * @see [encodeOutMaxSize]
         * @suppress
         * */
        @Deprecated(
            message = "Function name changed.",
            replaceWith = ReplaceWith("encodeOutMaxSize(unEncodedSize)"),
            level = DeprecationLevel.WARNING,
        )
        public fun encodeOutSize(unEncodedSize: Long): Long = encodeOutMaxSize(unEncodedSize, lineBreakInterval)

        /**
         * DEPRECATED since `2.6.0`
         * @see [encodeOutMaxSize]
         * @suppress
         * */
        @Deprecated(
            message = "Function name changed.",
            replaceWith = ReplaceWith("encodeOutMaxSize(unEncodedSize, lineBreakInterval)"),
            level = DeprecationLevel.WARNING,
        )
        public fun encodeOutSize(unEncodedSize: Long, lineBreakInterval: Byte): Long = encodeOutMaxSize(unEncodedSize, lineBreakInterval)

        /**
         * DEPRECATED since `2.6.0`
         * @suppress
         * */
        @Deprecated(
            message = "Parameters, lineBreakResetOnFlush, maxDecodeEmit, and backFillBuffers were added. Use the new constructor.",
            replaceWith = ReplaceWith(
                expression = "EncoderDecoder.Config(isLenient, lineBreakInterval, lineBreakResetOnFlush = false, paddingChar, maxDecodeEmit = 0 /* TODO */, backFillBuffers = true)"),
            level = DeprecationLevel.WARNING,
        )
        public constructor(
            isLenient: Boolean?,
            lineBreakInterval: Byte,
            paddingChar: Char?,
        ): this(
            isLenient = isLenient,
            lineBreakInterval = lineBreakIntervalOrZero(isLenient, lineBreakInterval),
            lineBreakResetOnFlush = false,
            paddingChar = paddingChar,
            maxDecodeEmit = -1, // NOTE: NEVER change.
            backFillBuffers = true,
            unused = null,
        )
    }

    /**
     * The base abstraction for [Decoder.Feed] and [Encoder.Feed].
     *
     * [Feed]s are meant to be single use disposables for the
     * given encoding/decoding operation.
     *
     * TL;DR [Feed]s only care about [Byte]s and [Char]s, not the medium
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

        public abstract fun isClosed(): Boolean

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

            // Implementations may do special things in their doFinallyProtected
            // implementation if they are only being flushed. This provides a way
            // for them to differentiate between what is, and is not a flush (by
            // checking isClosed).
            when (this) {
                is Decoder.Feed -> markAsClosed()
                is Encoder.Feed -> markAsClosed()
            }

            try {
                doFinalProtected()
            } finally {
                // Will de-reference {Encoder/Decoder}.OutFeed
                close()
            }
        }

        /**
         * Closes the feed rendering it useless.
         *
         * [close] can be called as many times as desired and
         * will not be considered an error if already closed.
         *
         * After [close] has been called, any invocation of
         * [Decoder.Feed.consume], [Encoder.Feed.consume], [flush]
         * or [doFinal] will be considered an error and throw an
         * [EncodingException].
         *
         * @see [use]
         * */
        public abstract fun close()

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

@Suppress("NOTHING_TO_INLINE")
private inline fun lineBreakIntervalOrZero(isLenient: Boolean?, interval: Byte): Byte {
    return if (isLenient != false && interval > 0) interval else 0
}
