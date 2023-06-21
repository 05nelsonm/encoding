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
@file:Suppress("RemoveRedundantQualifierName", "SpellCheckingInspection")

package io.matthewnelson.encoding.base64

import io.matthewnelson.encoding.core.*
import io.matthewnelson.encoding.core.util.DecoderInput
import io.matthewnelson.encoding.core.util.FeedBuffer
import kotlin.jvm.JvmField
import kotlin.jvm.JvmSynthetic

/**
 * Base64 encoding/decoding in accordance with
 * RFC 4648 section 4 & 5 (Default & UrlSafe)
 *
 * https://www.ietf.org/rfc/rfc4648.html#section-4
 * https://www.ietf.org/rfc/rfc4648.html#section-5
 *
 * Decodes both Default and UrlSafe, while encoding
 * to UrlSafe can be configured via the [Base64.Config].
 *
 * e.g.
 *
 *     val base64 = Base64 {
 *         isLenient = true
 *         lineBreakInterval = 64
 *         encodeToUrlSafe = false
 *         padEncoded = true
 *     }
 *
 *     val text = "Hello World!"
 *     val bytes = text.encodeToByteArray()
 *     val encoded = bytes.encodeToString(base64)
 *     println(encoded) // SGVsbG8gV29ybGQh
 *
 *     // Alternatively, use the static implementaton instead of
 *     // configuring your own settings.
 *     val decoded = encoded.decodeToByteArray(Base64.Default).decodeToString()
 *     assertEquals(text, decoded)
 *
 * @see [io.matthewnelson.encoding.base64.Base64]
 * @see [Base64.Config]
 * @see [Default.CHARS]
 * @see [UrlSafe.CHARS]
 * @see [Default]
 * @see [UrlSafe]
 * @see [EncoderDecoder]
 * @see [Decoder.decodeToByteArray]
 * @see [Decoder.decodeToByteArrayOrNull]
 * @see [Encoder.encodeToString]
 * @see [Encoder.encodeToCharArray]
 * @see [Encoder.encodeToByteArray]
 * */
@OptIn(ExperimentalEncodingApi::class)
public class Base64(config: Base64.Config): EncoderDecoder<Base64.Config>(config) {

    /**
     * Configuration for [Base64] encoding/decoding.
     *
     * Use [Base64ConfigBuilder] to create.
     *
     * @see [Base64ConfigBuilder]
     * @see [EncoderDecoder.Config]
     * */
    public class Config private constructor(
        isLenient: Boolean,
        lineBreakInterval: Byte,
        @JvmField
        public val encodeToUrlSafe: Boolean,
        @JvmField
        public val padEncoded: Boolean,
    ): EncoderDecoder.Config(
        isLenient = isLenient,
        lineBreakInterval = lineBreakInterval,
        paddingChar = '=',
    ) {

        protected override fun decodeOutMaxSizeProtected(encodedSize: Long): Long {
            return (encodedSize * 6L / 8L)
        }

        @Throws(EncodingException::class)
        protected override fun decodeOutMaxSizeOrFailProtected(encodedSize: Int, input: DecoderInput): Int {
            return decodeOutMaxSizeProtected(encodedSize.toLong()).toInt()
        }

        protected override fun encodeOutSizeProtected(unEncodedSize: Long): Long {
            var outSize: Long = (unEncodedSize + 2L) / 3L * 4L
            if (padEncoded) return outSize

            when (unEncodedSize - (unEncodedSize - unEncodedSize % 3)) {
                0L -> { /* no-op */ }
                1L -> outSize -= 2L
                2L -> outSize -= 1L
            }

            return outSize
        }

        protected override fun toStringAddSettings(): Set<Setting> {
            return buildSet {
                add(Setting(name = "encodeToUrlSafe", value = encodeToUrlSafe))
                add(Setting(name = "padEncoded", value = padEncoded))
            }
        }

        internal companion object {

            @JvmSynthetic
            internal fun from(builder: Base64ConfigBuilder): Config {
                return Config(
                    isLenient = builder.isLenient,
                    lineBreakInterval = builder.lineBreakInterval,
                    encodeToUrlSafe = builder.encodeToUrlSafe,
                    padEncoded = builder.padEncoded,
                )
            }
        }
    }

    /**
     * Doubles as a static implementation with default settings
     * and a lineBreakInterval of 64.
     *
     * e.g.
     *
     *     val encoded = "Hello World!"
     *         .encodeToByteArray()
     *         .encodeToString(Base64.Default)
     *
     *     println(encoded) // SGVsbG8gV29ybGQh
     *
     * */
    public object Default: EncoderDecoder<Base64.Config>(
        config = Base64ConfigBuilder().apply { lineBreakInterval = 64 }.build()
    ) {

        /**
         * Base64 Default encoding characters.
         * */
        public const val CHARS: String = "ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz0123456789+/"

        private val DELEGATE = Base64(config)
        override fun name(): String = DELEGATE.name()
        override fun newDecoderFeedProtected(out: Decoder.OutFeed): Decoder<Base64.Config>.Feed {
            return DELEGATE.newDecoderFeedProtected(out)
        }
        override fun newEncoderFeedProtected(out: OutFeed): Encoder<Base64.Config>.Feed {
            return DELEGATE.newEncoderFeedProtected(out)
        }
    }

    /**
     * Doubles as a static implementation with default settings
     * and a lineBreakInterval of 64.
     *
     * e.g.
     *
     *     val encoded = "Hello World!"
     *         .encodeToByteArray()
     *         .encodeToString(Base64.UrlSafe)
     *
     *     println(encoded) // SGVsbG8gV29ybGQh
     *
     * */
    public object UrlSafe: EncoderDecoder<Base64.Config>(
        config = Base64ConfigBuilder(Default.config).apply { encodeToUrlSafe = true }.build()
    ) {

        /**
         * Base64 UrlSafe encoding characters.
         * */
        public const val CHARS: String = "ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz0123456789-_"

        private val DELEGATE = Base64(config)
        override fun name(): String = DELEGATE.name()
        override fun newDecoderFeedProtected(out: Decoder.OutFeed): Decoder<Base64.Config>.Feed {
            return DELEGATE.newDecoderFeedProtected(out)
        }
        override fun newEncoderFeedProtected(out: OutFeed): Encoder<Base64.Config>.Feed {
            return DELEGATE.newEncoderFeedProtected(out)
        }
    }

    protected override fun newDecoderFeedProtected(out: Decoder.OutFeed): Decoder<Base64.Config>.Feed {
        return object : Decoder<Base64.Config>.Feed() {

            private val buffer = DecodingBuffer(out)

            override fun consumeProtected(input: Char) {
                val bits: Int = when (input) {
                    in '0'..'9' -> {
                        // char ASCII value
                        //  0    48    52
                        //  9    57    61 (ASCII + 4)
                        input.code + 4
                    }
                    in 'A'..'Z' -> {
                        // char ASCII value
                        //  A    65    0
                        //  Z    90    25 (ASCII - 65)
                        input.code - 65
                    }
                    in 'a'..'z' -> {
                        // char ASCII value
                        //  a    97    26
                        //  z    122   51 (ASCII - 71)
                        input.code - 71
                    }
                    '+', '-' -> {
                        62
                    }
                    '/', '_' -> {
                        63
                    }
                    else -> {
                        throw EncodingException("Char[$input] is not a valid Base64 character")
                    }
                }

                buffer.update(bits)
            }

            override fun doFinalProtected() {
                buffer.finalize()
            }
        }
    }

    protected override fun newEncoderFeedProtected(out: OutFeed): Encoder<Base64.Config>.Feed {
        return object : Encoder<Base64.Config>.Feed() {

            private val buffer = EncodingBuffer(
                out = out,
                table = if (config.encodeToUrlSafe) {
                    UrlSafe.CHARS
                } else {
                    Default.CHARS
                },
                paddingChar = if (config.padEncoded) {
                    config.paddingChar
                } else {
                    null
                },
            )

            override fun consumeProtected(input: Byte) {
                buffer.update(input.toInt())
            }

            override fun doFinalProtected() {
                buffer.finalize()
            }
        }
    }

    protected override fun name(): String = "Base64"

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
            // For every 3 chars of input, we accumulate
            // 24 bits of output. Emit 4 bytes.
            val b0 = buffer[0]
            val b1 = buffer[1]
            val b2 = buffer[2]
            out.output(table[(b0 and 0xff shr 2)])
            out.output(table[(b0 and 0x03 shl 4) or (b1 and 0xff shr 4)])
            out.output(table[(b1 and 0x0f shl 2) or (b2 and 0xff shr 6)])
            out.output(table[(b2 and 0x3f)])
        },
        finalize = { modulus, buffer ->
            val padCount: Int = when (modulus) {
                0 -> { 0 }
                1 -> {
                    val b0 = buffer[0]
                    out.output(table[b0 and 0xff shr 2])
                    out.output(table[b0 and 0x03 shl 4])
                    2
                }
                // 2
                else -> {
                    val b0 = buffer[0]
                    val b1 = buffer[1]
                    out.output(table[(b0 and 0xff shr 2)])
                    out.output(table[(b0 and 0x03 shl 4) or (b1 and 0xff shr 4)])
                    out.output(table[(b1 and 0x0f shl 2)])
                    1
                }
            }

            if (paddingChar != null) {
                repeat(padCount) {
                    out.output(paddingChar)
                }
            }
        }
    )
}
