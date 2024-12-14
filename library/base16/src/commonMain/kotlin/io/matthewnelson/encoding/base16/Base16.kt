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

package io.matthewnelson.encoding.base16

import io.matthewnelson.encoding.core.*
import io.matthewnelson.encoding.core.util.DecoderInput
import io.matthewnelson.encoding.core.util.FeedBuffer
import kotlin.jvm.JvmField
import kotlin.jvm.JvmSynthetic

/**
 * Base16 (aka "hex") encoding/decoding in accordance with
 * RFC 4648 section 8.
 *
 * https://www.ietf.org/rfc/rfc4648.html#section-8
 *
 * e.g.
 *
 *     val base16 = Base16 {
 *         isLenient = true
 *         lineBreakInterval = 64
 *         encodeToLowercase = true
 *     }
 *
 *     val text = "Hello World!"
 *     val bytes = text.encodeToByteArray()
 *     val encoded = bytes.encodeToString(base16)
 *     println(encoded) // 48656c6c6f20576f726c6421
 *
 *     // Alternatively, use the static implementaton instead of
 *     // configuring your own settings.
 *     val decoded = encoded.decodeToByteArray(Base16).decodeToString()
 *     assertEquals(text, decoded)
 *
 * @see [io.matthewnelson.encoding.base16.Base16]
 * @see [Base16.Config]
 * @see [Base16.CHARS_UPPER]
 * @see [Base16.CHARS_LOWER]
 * @see [Base16.Companion]
 * @see [EncoderDecoder]
 * @see [Decoder.decodeToByteArray]
 * @see [Decoder.decodeToByteArrayOrNull]
 * @see [Encoder.encodeToString]
 * @see [Encoder.encodeToCharArray]
 * @see [Encoder.encodeToByteArray]
 * */
public class Base16(config: Base16.Config): EncoderDecoder<Base16.Config>(config) {

    /**
     * Configuration for [Base16] encoding/decoding.
     *
     * Use [Base16ConfigBuilder] to create.
     *
     * @see [Base16ConfigBuilder]
     * @see [EncoderDecoder.Config]
     * */
    public class Config private constructor(
        isLenient: Boolean,
        lineBreakInterval: Byte,
        @JvmField
        public val encodeToLowercase: Boolean,
        @JvmField
        public val isConstantTime: Boolean,
    ): EncoderDecoder.Config(
        isLenient = isLenient,
        lineBreakInterval = lineBreakInterval,
        paddingChar = null
    ) {

        protected override fun decodeOutMaxSizeProtected(encodedSize: Long): Long {
            return encodedSize / 2L
        }

        protected override fun decodeOutMaxSizeOrFailProtected(encodedSize: Int, input: DecoderInput): Int {
            return encodedSize / 2
        }

        @Throws(EncodingSizeException::class)
        protected override fun encodeOutSizeProtected(unEncodedSize: Long): Long {
            if (unEncodedSize > (Long.MAX_VALUE / 2)) {
                throw outSizeExceedsMaxEncodingSizeException(unEncodedSize, Long.MAX_VALUE)
            }

            return unEncodedSize * 2L
        }

        protected override fun toStringAddSettings(): Set<Setting> {
            return buildSet {
                add(Setting(name = "encodeToLowercase", value = encodeToLowercase))
                add(Setting(name = "isConstantTime", value = isConstantTime))
            }
        }

        internal companion object {

            @JvmSynthetic
            internal fun from(builder: Base16ConfigBuilder): Config {
                return Config(
                    isLenient = builder.isLenient,
                    lineBreakInterval = builder.lineBreakInterval,
                    encodeToLowercase = builder.encodeToLowercase,
                    isConstantTime = builder.isConstantTime,
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
     *         .encodeToString(Base16)
     *
     *     println(encoded) // 48656c6c6f20576f726c6421
     *
     * */
    public companion object: EncoderDecoder<Base16.Config>(
        config = Base16ConfigBuilder().apply { lineBreakInterval = 64 }.build()
    ) {

        private const val LETTERS_UPPER: String = "ABCDEF"
        private const val LETTERS_LOWER: String = "abcdef"

        /**
         * Uppercase Base16 encoding characters.
         * */
        public const val CHARS_UPPER: String = "0123456789$LETTERS_UPPER"

        /**
         * Lowercase Base16 encoding characters.
         * */
        public const val CHARS_LOWER: String = "0123456789$LETTERS_LOWER"

        private val DELEGATE = Base16(config)
        protected override fun name(): String = DELEGATE.name()
        protected override fun newDecoderFeedProtected(out: Decoder.OutFeed): Decoder<Base16.Config>.Feed {
            return DELEGATE.newDecoderFeedProtected(out)
        }
        protected override fun newEncoderFeedProtected(out: OutFeed): Encoder<Base16.Config>.Feed {
            return DELEGATE.newEncoderFeedProtected(out)
        }

        private val DECODE_ACTIONS = arrayOf<Pair<Iterable<Char>, Char.() -> Int>>(
            '0'..'9' to {
                // char ASCII value
                // 0     48    0
                // 9     57    9 (ASCII - 48)
                code - 48
            },
            LETTERS_UPPER.asIterable() to {
                // char ASCII value
                //   A   65    10
                //   F   70    15 (ASCII - 55)
                code - 55
            },
            LETTERS_LOWER.asIterable() to {
                // char ASCII value
                //   A   65    10
                //   F   70    15 (ASCII - 55)
                uppercaseChar().code - 55
            },
        )
    }

    protected override fun newDecoderFeedProtected(out: Decoder.OutFeed): Decoder<Config>.Feed {
        return object : Decoder<Config>.Feed() {

            private val buffer = DecodingBuffer(out)
            private val actions = when {
                // Do not include lowercase letter actions. Constant time
                // operations will uppercase the input on every invocation.
                config.isConstantTime -> arrayOf(
                    DECODE_ACTIONS[0],
                    DECODE_ACTIONS[1],
                )
                // Assume input will be lowercase letters. Reorder
                // actions to check lowercase before uppercase.
                config.encodeToLowercase -> arrayOf(
                    DECODE_ACTIONS[0],
                    DECODE_ACTIONS[2],
                    DECODE_ACTIONS[1],
                )
                else -> DECODE_ACTIONS
            }

            @Throws(EncodingException::class)
            override fun consumeProtected(input: Char) {
                var bitsFrom: (Char.() -> Int)? = null
                var target: Char? = null

                if (config.isConstantTime) {
                    val iLower = LETTERS_LOWER.iterator()
                    val iUpper = LETTERS_UPPER.iterator()

                    while (iLower.hasNext() && iUpper.hasNext()) {
                        val cLower = iLower.next()
                        val cUpper = iUpper.next()

                        if (input != cLower) continue
                        target = cUpper
                    }
                }

                if (target == null) {
                    // Either not using constant time, or input was not a lowercase letter.
                    target = input
                }

                for ((chars, action) in actions) {
                    for (c in chars) {
                        if (!config.isConstantTime && bitsFrom != null) break
                        if (target != c) continue
                        bitsFrom = action
                    }

                    if (config.isConstantTime) continue
                    if (bitsFrom != null) break
                }

                if (bitsFrom == null) {
                    throw EncodingException("Char[${input}] is not a valid Base16 character")
                }

                buffer.update(bitsFrom(target))
            }

            @Throws(EncodingException::class)
            override fun doFinalProtected() {
                buffer.finalize()
            }
        }
    }

    protected override fun newEncoderFeedProtected(out: OutFeed): Encoder<Config>.Feed {
        return object : Encoder<Config>.Feed() {

            private val table = if (config.encodeToLowercase) {
                CHARS_LOWER
            } else {
                CHARS_UPPER
            }

            override fun consumeProtected(input: Byte) {
                // A FeedBuffer is not necessary here as every 1
                // byte of input, 2 characters are output.
                val bits = input.toInt() and 0xff

                val i1 = bits shr 4
                val i2 = bits and 0x0f

                if (config.isConstantTime) {
                    var c1: Char? = null
                    var c2: Char? = null

                    table.forEachIndexed { index, c ->
                        if (index == i1) c1 = c
                        if (index == i2) c2 = c
                    }

                    out.output(c1!!)
                    out.output(c2!!)
                } else {
                    out.output(table[i1])
                    out.output(table[i2])
                }
            }

            override fun doFinalProtected() { /* no-op */ }
        }
    }

    protected override fun name(): String = "Base16"

    private inner class DecodingBuffer(out: Decoder.OutFeed): FeedBuffer(
        blockSize = 2,
        flush = { buffer ->
            var bitBuffer = 0
            for (bits in buffer) {
                bitBuffer = (bitBuffer shl 4) or bits
            }

            out.output(bitBuffer.toByte())
        },
        finalize = { modulus, _->
            when (modulus) {
                0 -> { /* no-op */ }
                else -> {
                    // 4*1 = 4 bits. Truncated, fail.
                    throw truncatedInputEncodingException(modulus)
                }
            }
        }
    )
}
