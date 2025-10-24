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
@file:Suppress("FunctionName", "PropertyName", "RedundantModalityModifier", "RedundantVisibilityModifier", "RemoveRedundantQualifierName")

package io.matthewnelson.encoding.base64

import io.matthewnelson.encoding.base64.internal.build
import io.matthewnelson.encoding.core.Decoder
import io.matthewnelson.encoding.core.Encoder
import io.matthewnelson.encoding.core.EncoderDecoder
import io.matthewnelson.encoding.core.EncodingException
import io.matthewnelson.encoding.core.util.DecoderInput
import io.matthewnelson.encoding.core.util.FeedBuffer
import kotlin.contracts.ExperimentalContracts
import kotlin.contracts.InvocationKind
import kotlin.contracts.contract
import kotlin.jvm.JvmField
import kotlin.jvm.JvmName
import kotlin.jvm.JvmStatic
import kotlin.jvm.JvmSynthetic

/**
 * Base64 encoding/decoding in accordance with [RFC 4648 section 4](https://www.ietf.org/rfc/rfc4648.html#section-4)
 * and [RFC 4648 section 5](https://www.ietf.org/rfc/rfc4648.html#section-5).
 *
 * **NOTE:** All instances decode both [Default.CHARS] and [UrlSafe.CHARS] interchangeably;
 * no special configuration is needed.
 *
 * e.g.
 *
 *     val base64 = Base64.Builder {
 *         isLenient(enable = true)
 *         lineBreak(interval = 64)
 *         encodeUrlSafe(enable = false)
 *         padEncoded(enable = true)
 *     }
 *
 *     val text = "Hello World!"
 *     val bytes = text.encodeToByteArray()
 *     val encoded = bytes.encodeToString(base64)
 *     println(encoded) // SGVsbG8gV29ybGQh
 *
 *     // Alternatively, use the static implementation containing
 *     // pre-configured settings, instead of creating your own.
 *     var decoded = encoded.decodeToByteArray(Base64.Default).decodeToString()
 *     assertEquals(text, decoded)
 *     decoded = encoded.decodeToByteArray(Base64.UrlSafe).decodeToString()
 *     assertEquals(text, decoded)
 *
 * @see [Builder]
 * @see [Companion.Builder]
 * @see [Default]
 * @see [UrlSafe]
 * @see [Encoder.Companion]
 * @see [Decoder.Companion]
 * */
public class Base64: EncoderDecoder<Base64.Config> {

    public companion object {

        /**
         * Syntactic sugar for Kotlin consumers that like lambdas.
         * */
        @JvmStatic
        @JvmName("-Builder")
        @OptIn(ExperimentalContracts::class)
        public inline fun Builder(block: Builder.() -> Unit): Base64 {
            contract { callsInPlace(block, InvocationKind.EXACTLY_ONCE) }
            return Builder(other = null, block)
        }

        /**
         * Syntactic sugar for Kotlin consumers that like lambdas.
         * */
        @JvmStatic
        @JvmName("-Builder")
        @OptIn(ExperimentalContracts::class)
        public inline fun Builder(other: Base64.Config?, block: Builder.() -> Unit): Base64 {
            contract { callsInPlace(block, InvocationKind.EXACTLY_ONCE) }
            return Builder(other).apply(block).build()
        }

        private const val NAME = "Base64"
    }

    /**
     * A Builder
     *
     * @see [Companion.Builder]
     * */
    public class Builder {

        public constructor(): this(other = null)
        public constructor(other: Config?) {
            if (other == null) return
            this._isLenient = other.isLenient ?: true
            this._lineBreakInterval = other.lineBreakInterval
            this._encodeUrlSafe = other.encodeUrlSafe
            this._padEncoded = other.padEncoded
        }

        @JvmSynthetic
        internal var _isLenient: Boolean = true
        @JvmSynthetic
        internal var _lineBreakInterval: Byte = 0
        @JvmSynthetic
        internal var _encodeUrlSafe: Boolean = false
        @JvmSynthetic
        internal var _padEncoded: Boolean = true

        /**
         * DEFAULT: `true`
         *
         * If `true`, the characters ('\n', '\r', ' ', '\t') will be skipped over (i.e.
         * allowed but ignored) during decoding operations. This is non-compliant with
         * `RFC 4648`.
         *
         * If `false`, an [EncodingException] will be thrown.
         * */
        public fun isLenient(enable: Boolean): Builder = apply { _isLenient = enable }

        /**
         * DEFAULT: `0` (i.e. disabled)
         *
         * If greater than `0`, when [interval] number of encoded characters have been
         * output, the next encoded character will be preceded with the new line character
         * `\n`.
         *
         * A great value is `64`, and is what both [Default.config] and [UrlSafe.config] use.
         *
         * **NOTE:** This setting is ignored if [isLenient] is set to `false`.
         *
         * e.g.
         *
         *     isLenient(enable = true)
         *     lineBreak(interval = 0)
         *     // SGVsbG8gV29ybGQh
         *
         *     isLenient(enable = true)
         *     lineBreak(interval = 10)
         *     // SGVsbG8gV29ybGQh
         *     // 9ybGQh
         *
         *     isLenient(enable = false)
         *     lineBreak(interval = 10)
         *     // SGVsbG8gV29ybGQh
         * */
        public fun lineBreak(interval: Byte): Builder = apply { _lineBreakInterval = interval }

        /**
         * DEFAULT: `false`
         *
         * If `true`, characters from table [UrlSafe.CHARS] will be output during encoding
         * operations.
         *
         * If `false`, characters from table [Default.CHARS] will be output during encoding
         * operations.
         *
         * **NOTE:** This does not affect decoding operations. [Base64] is designed to accept
         * characters from both tables when decoding.
         * */
        public fun encodeUrlSafe(enable: Boolean): Builder = apply { _encodeUrlSafe = enable }

        /**
         * DEFAULT: `true`
         *
         * If `true`, encoded output will have the appropriate number of padding character(s)
         * `=` appended to it.
         *
         * If `false`, padding character(s) will be omitted from output. This is non-compliant
         * with `RFC 4648`.
         * */
        public fun padEncoded(enable: Boolean): Builder = apply { _padEncoded = enable }

        /**
         * Helper for configuring the builder with settings which are compliant with the
         * `RFC 4648` specification.
         *
         *  - [isLenient] will be set to `false`.
         *  - [padEncoded] will be set to `true`.
         * */
        public fun strictSpec(): Builder = apply {
            _isLenient = false
            _padEncoded = true
        }

        /**
         * Commits configured options to [Config], creating the [Base64] instance.
         * */
        public fun build(): Base64 = Config.build(this)
    }

    /**
     * Holder of a configuration for the [Base64] encoder/decoder instance.
     *
     * @see [Builder]
     * @see [Companion.Builder]
     * */
    public class Config private constructor(
        isLenient: Boolean,
        lineBreakInterval: Byte,
        @JvmField
        public val encodeUrlSafe: Boolean,
        @JvmField
        public val padEncoded: Boolean,
    ): EncoderDecoder.Config(isLenient, lineBreakInterval, '=') {

        protected override fun decodeOutMaxSizeProtected(encodedSize: Long): Long {
            // TODO: Check for overflow?
            return (encodedSize * 6L / 8L)
        }

        protected override fun decodeOutMaxSizeOrFailProtected(encodedSize: Int, input: DecoderInput): Int {
            // TODO: Check for overflow?
            return decodeOutMaxSizeProtected(encodedSize.toLong()).toInt()
        }

        protected override fun encodeOutSizeProtected(unEncodedSize: Long): Long {
            // TODO: Check for overflow?
            var outSize: Long = (unEncodedSize + 2L) / 3L * 4L
            if (padEncoded) return outSize

            when (unEncodedSize - (unEncodedSize - unEncodedSize % 3)) {
                0L -> { /* no-op */ }
                1L -> outSize -= 2L
                2L -> outSize -= 1L
            }

            return outSize
        }

        protected override fun toStringAddSettings(): Set<Setting> = buildSet {
            add(Setting(name = "encodeUrlSafe", value = encodeUrlSafe))
            add(Setting(name = "padEncoded", value = padEncoded))
            add(Setting(name = "isConstantTime", value = isConstantTime))
        }

        internal companion object {

            @JvmSynthetic
            internal fun build(b: Builder): Base64 = ::Config.build(b, ::Base64)

            @get:JvmSynthetic
            internal val DEFAULT: Config = Config(
                isLenient = true,
                lineBreakInterval = 64,
                encodeUrlSafe = false,
                padEncoded = true
            )

            @get:JvmSynthetic
            internal val URL_SAFE: Config = Config(
                isLenient = DEFAULT.isLenient ?: true,
                lineBreakInterval = DEFAULT.lineBreakInterval,
                encodeUrlSafe = true,
                padEncoded = DEFAULT.padEncoded,
            )
        }

        /** @suppress */
        @JvmField
        @Deprecated("Variable name changed.", ReplaceWith("encodeUrlSafe"))
        public val encodeToUrlSafe: Boolean = encodeUrlSafe
        /** @suppress */
        @JvmField
        public val isConstantTime: Boolean = true
    }

    /**
     * A static instance of [EncoderDecoder] configured with a [Base64.Builder.lineBreak]
     * interval of `64`, and remaining [Base64.Builder] `DEFAULT` values.
     * */
    public object Default: EncoderDecoder<Base64.Config>(config = Base64.Config.DEFAULT) {

        /**
         * Base64 Default encoding characters.
         * */
        public const val CHARS: String = "ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz0123456789+/"

        @get:JvmSynthetic
        internal val DELEGATE = Base64(config, unused = null)
        override fun name(): String = DELEGATE.name()
        override fun newDecoderFeedProtected(out: Decoder.OutFeed): Decoder<Base64.Config>.Feed {
            return DELEGATE.newDecoderFeedProtected(out)
        }
        override fun newEncoderFeedProtected(out: Encoder.OutFeed): Encoder<Base64.Config>.Feed {
            return DELEGATE.newEncoderFeedProtected(out)
        }
    }

    /**
     * A static instance of [EncoderDecoder] configured with a [Base64.Builder.lineBreak]
     * interval of `64`, a [Base64.Builder.encodeUrlSafe] set to `true`, and remaining
     * [Base64.Builder] `DEFAULT` values.
     * */
    public object UrlSafe: EncoderDecoder<Base64.Config>(config = Base64.Config.URL_SAFE) {

        /**
         * Base64 UrlSafe encoding characters.
         * */
        public const val CHARS: String = "ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz0123456789-_"

        @get:JvmSynthetic
        internal val DELEGATE = Base64(config, unused = null)
        override fun name(): String = DELEGATE.name()
        override fun newDecoderFeedProtected(out: Decoder.OutFeed): Decoder<Base64.Config>.Feed {
            return DELEGATE.newDecoderFeedProtected(out)
        }
        override fun newEncoderFeedProtected(out: Encoder.OutFeed): Encoder<Base64.Config>.Feed {
            return DELEGATE.newEncoderFeedProtected(out)
        }
    }

    protected final override fun name(): String = NAME

    protected final override fun newDecoderFeedProtected(out: Decoder.OutFeed): Decoder<Base64.Config>.Feed {
        return object : Decoder<Base64.Config>.Feed() {

            private val buffer = DecodingBuffer(out)

            override fun consumeProtected(input: Char) {
                val code = input.code

                val ge0:   Byte = if (code >= '0'.code) 1 else 0
                val le9:   Byte = if (code <= '9'.code) 1 else 0
                val geA:   Byte = if (code >= 'A'.code) 1 else 0
                val leZ:   Byte = if (code <= 'Z'.code) 1 else 0
                val gea:   Byte = if (code >= 'a'.code) 1 else 0
                val lez:   Byte = if (code <= 'z'.code) 1 else 0
                val eqPlu: Byte = if (code == '+'.code) 1 else 0
                val eqMin: Byte = if (code == '-'.code) 1 else 0
                val eqSla: Byte = if (code == '/'.code) 1 else 0
                val eqUSc: Byte = if (code == '_'.code) 1 else 0

                var diff = 0

                // char ASCII value
                //  0     48   52
                //  9     57   61 (ASCII + 4)
                diff += if (ge0 + le9 == 2) 4 else 0

                // char ASCII value
                //  A     65    0
                //  Z     90   25 (ASCII - 65)
                diff += if (geA + leZ == 2) -65 else 0

                // char ASCII value
                //  a     97   26
                //  z    122   51 (ASCII - 71)
                diff += if (gea + lez == 2) -71 else 0

                val h = 62 - code
                val k = 63 - code
                diff += if (eqPlu + eqMin == 1) h else 0
                diff += if (eqSla + eqUSc == 1) k else 0

                if (diff == 0) {
                    throw EncodingException("Char[$input] is not a valid Base64 character")
                }

                buffer.update(code + diff)
            }

            override fun doFinalProtected() { buffer.finalize() }
        }
    }

    protected final override fun newEncoderFeedProtected(out: Encoder.OutFeed): Encoder<Base64.Config>.Feed {
        return object : Encoder<Base64.Config>.Feed() {

            private val buffer = EncodingBuffer(
                out = out,
                table = if (config.encodeUrlSafe) UrlSafe.CHARS else Default.CHARS,
                paddingChar = if (config.padEncoded) config.paddingChar else null,
            )

            override fun consumeProtected(input: Byte) { buffer.update(input.toInt()) }

            override fun doFinalProtected() { buffer.finalize() }
        }
    }

    private inner class DecodingBuffer(out: Decoder.OutFeed): FeedBuffer(
        blockSize = 4,
        flush = { buffer ->
            var bitBuffer = 0

            // Append each char's 6 bits to the bitBuffer.
            for (bits in buffer) {
                bitBuffer = bitBuffer shl 6 or bits
            }

            // For every 4 chars of input, we accumulate 24 bits of output. Emit 3 bytes.
            out.output((bitBuffer shr 16).toByte())
            out.output((bitBuffer shr  8).toByte())
            out.output((bitBuffer       ).toByte())
        },
        finalize = { modulus, buffer ->
            if (modulus == 1) {
                // We read 1 char followed by "===". But 6 bits is a truncated byte! Fail.
                throw truncatedInputEncodingException(modulus)
            }

            var bitBuffer = 0

            // Append each char remaining in the buffer to the bitBuffer.
            for (i in 0 until modulus) {
                bitBuffer = bitBuffer shl 6 or buffer[i]
            }

            when (modulus) {
                0 -> { /* no-op */ }
                2 -> {
                    // We read 2 chars followed by "==". Emit 1 byte with 8 of those 12 bits.
                    bitBuffer = bitBuffer shl 12
                    out.output((bitBuffer shr 16).toByte())
                }
                3 -> {
                    // We read 3 chars, followed by "=". Emit 2 bytes for 16 of those 18 bits.
                    bitBuffer = bitBuffer shl  6
                    out.output((bitBuffer shr 16).toByte())
                    out.output((bitBuffer shr  8).toByte())
                }
            }
        },
    )

    private inner class EncodingBuffer(
        out: Encoder.OutFeed,
        table: CharSequence,
        paddingChar: Char?,
    ): FeedBuffer(
        blockSize = 3,
        flush = { buffer ->
            // For every 3 bytes of input, we accumulate
            // 24 bits of output. Emit 4 characters.
            val b0 = buffer[0]
            val b1 = buffer[1]
            val b2 = buffer[2]

            val i1 = (b0 and 0xff shr 2)
            val i2 = (b0 and 0x03 shl 4) or (b1 and 0xff shr 4)
            val i3 = (b1 and 0x0f shl 2) or (b2 and 0xff shr 6)
            val i4 = (b2 and 0x3f)

            out.output(table[i1])
            out.output(table[i2])
            out.output(table[i3])
            out.output(table[i4])
        },
        finalize = { modulus, buffer ->
            val padCount: Int = when (modulus) {
                0 -> { 0 }
                1 -> {
                    val b0 = buffer[0]

                    val i1 = b0 and 0xff shr 2
                    val i2 = b0 and 0x03 shl 4

                    out.output(table[i1])
                    out.output(table[i2])

                    2
                }
                // 2
                else -> {
                    val b0 = buffer[0]
                    val b1 = buffer[1]

                    val i1 = (b0 and 0xff shr 2)
                    val i2 = (b0 and 0x03 shl 4) or (b1 and 0xff shr 4)
                    val i3 = (b1 and 0x0f shl 2)

                    out.output(table[i1])
                    out.output(table[i2])
                    out.output(table[i3])

                    1
                }
            }

            if (paddingChar != null) {
                repeat(padCount) { out.output(paddingChar) }
            }
        }
    )

    @Deprecated(
        message = "This constructor is scheduled for removal. Use Base64.Builder or Base64.Companion.Builder.",
        level = DeprecationLevel.WARNING,
    )
    public constructor(config: Config): this(config, unused = null)

    @Suppress("UNUSED_PARAMETER")
    private constructor(config: Config, unused: Any?): super(config)
}
