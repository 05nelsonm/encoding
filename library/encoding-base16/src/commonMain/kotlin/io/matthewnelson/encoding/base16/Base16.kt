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

package io.matthewnelson.encoding.base16

import io.matthewnelson.encoding.builders.Base16ConfigBuilder
import io.matthewnelson.encoding.core.*
import io.matthewnelson.encoding.core.internal.EncodingTable
import io.matthewnelson.encoding.core.internal.InternalEncodingApi
import io.matthewnelson.encoding.core.util.DecoderInput
import io.matthewnelson.encoding.core.util.buffer.DecodingBuffer
import io.matthewnelson.encoding.core.util.char
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
 *         acceptLowercase = true
 *         encodeToLowercase = false
 *     }
 *
 *     val text = "Hello World!"
 *     val bytes = text.encodeToByteArray()
 *     val encoded = bytes.encodeToString(base16)
 *     println(encoded) // 48656C6C6F20576F726C6421
 *     val decoded = encoded.decodeToByteArray(base16).decodeToString()
 *     assertEquals(text, decoded)
 *
 * @see [io.matthewnelson.encoding.builders.Base16]
 * @see [Base16.Config]
 * @see [Base16.CHARS]
 * @see [EncoderDecoder]
 * @see [Decoder.decodeToByteArray]
 * @see [Decoder.decodeToByteArrayOrNull]
 * @see [Encoder.encodeToString]
 * @see [Encoder.encodeToCharArray]
 * @see [Encoder.encodeToByteArray]
 * */
@OptIn(ExperimentalEncodingApi::class, InternalEncodingApi::class)
public class Base16(config: Config): EncoderDecoder(config) {

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
        @JvmField
        public val encodeToLowercase: Boolean,
    ): EncoderDecoder.Config(isLenient, paddingByte = null) {

        override fun decodeOutMaxSizeProtected(encodedSize: Long): Long {
            return encodedSize / 2L
        }

        override fun decodeOutMaxSizeOrFailProtected(encodedSize: Int, input: DecoderInput): Int {
            return encodedSize / 2
        }

        @Throws(EncodingSizeException::class)
        override fun encodeOutSizeProtected(unEncodedSize: Long): Long {
            if (unEncodedSize > (Long.MAX_VALUE / 2)) {
                throw DecoderInput.outSizeExceedsMaxEncodingSizeException(unEncodedSize, Long.MAX_VALUE)
            }

            return unEncodedSize * 2L
        }

        override fun toStringAddSettings(sb: StringBuilder) {
            with(sb) {
                append("    encodeToLowercase: ")
                append(encodeToLowercase)
            }
        }

        internal companion object {

            @JvmSynthetic
            internal fun from(builder: Base16ConfigBuilder): Config {
                return Config(
                    isLenient = builder.isLenient,
                    encodeToLowercase = builder.encodeToLowercase,
                )
            }
        }
    }

    public companion object {

        /**
         * Base16 encoding characters.
         * */
        public const val CHARS: String = "0123456789ABCDEF"
        private val TABLE = EncodingTable.from(CHARS)
        private val TABLE_LOWERCASE = EncodingTable.from(CHARS.lowercase())
    }

    @ExperimentalEncodingApi
    override fun newEncoderFeed(out: OutFeed): Encoder.Feed {
        return object : Encoder.Feed() {

            private val table = if ((config as Config).encodeToLowercase) {
                TABLE_LOWERCASE
            } else {
                TABLE
            }

            override fun updateProtected(input: Byte) {
                val bits = input.toInt() and 0xff
                out.invoke(table[bits shr    4])
                out.invoke(table[bits and 0x0f])
            }

            override fun doFinalProtected() { /* no-op */ }
        }
    }

    @ExperimentalEncodingApi
    override fun newDecoderFeed(out: OutFeed): Decoder.Feed {
        return object : Decoder.Feed() {

            private val buffer = Base16DecodingBuffer(out)

            @Throws(EncodingException::class)
            override fun updateProtected(input: Byte) {
                val bits: Int = when (val char = input.char.uppercaseChar()) {
                    in '0'..'9' -> {
                        // char ASCII value
                        // 0     48    0
                        // 9     57    9 (ASCII - 48)
                        char.code - 48
                    }
                    in 'A'..'F' -> {
                        // char ASCII value
                        //   A   65    10
                        //   F   70    15 (ASCII - 55)
                        char.code - 55
                    }
                    else -> {
                        throw EncodingException("Char[${input.char}] is not a valid Base16 character")
                    }
                }

                buffer.update(bits)
            }

            @Throws(EncodingException::class)
            override fun doFinalProtected() {
                buffer.finalize()
            }
        }
    }

    override fun name(): String = "Base16"

    private inner class Base16DecodingBuffer(out: OutFeed): DecodingBuffer(
        blockSize = 2,
        flush = { buffer ->
            var bitBuffer = 0
            for (bits in buffer) {
                bitBuffer = (bitBuffer shl 4) or bits
            }

            out.invoke(bitBuffer.toByte())
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
