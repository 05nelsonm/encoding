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

import io.matthewnelson.encoding.base64.Base64.Default
import io.matthewnelson.encoding.builders.Base64ConfigBuilder
import io.matthewnelson.encoding.core.*
import io.matthewnelson.encoding.core.internal.EncodingTable
import io.matthewnelson.encoding.core.internal.InternalEncodingApi
import io.matthewnelson.encoding.core.util.DecoderInput
import io.matthewnelson.encoding.core.util.buffer.DecodingBuffer
import io.matthewnelson.encoding.core.util.buffer.EncodingBuffer
import io.matthewnelson.encoding.core.util.byte
import io.matthewnelson.encoding.core.util.char
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
 *         encodeToUrlSafe = false
 *         padEncoded = true
 *     }
 *
 *     val text = "Hello World!"
 *     val bytes = text.encodeToByteArray()
 *     val encoded = bytes.encodeToString(base64)
 *     println(encoded) // SGVsbG8gV29ybGQh
 *     val decoded = encoded.decodeToByteArray(base64).decodeToString()
 *     assertEquals(text, decoded)
 *
 * @see [io.matthewnelson.encoding.builders.Base64]
 * @see [Base64.Config]
 * @see [Default.CHARS]
 * @see [UrlSafe.CHARS]
 * @see [EncoderDecoder]
 * @see [Decoder.decodeToByteArray]
 * @see [Decoder.decodeToByteArrayOrNull]
 * @see [Encoder.encodeToString]
 * @see [Encoder.encodeToCharArray]
 * @see [Encoder.encodeToByteArray]
 * */
@OptIn(ExperimentalEncodingApi::class, InternalEncodingApi::class)
public class Base64(config: Base64.Config): EncoderDecoder(config) {

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
        @JvmField
        public val encodeToUrlSafe: Boolean,
        @JvmField
        public val padEncoded: Boolean,
    ): EncoderDecoder.Config(isLenient, paddingByte = '='.byte) {

        override fun decodeOutMaxSizeProtected(encodedSize: Long): Long {
            return (encodedSize * 6L / 8L)
        }

        @Throws(EncodingException::class)
        override fun decodeOutMaxSizeOrFailProtected(encodedSize: Int, input: DecoderInput): Int {
            return decodeOutMaxSizeProtected(encodedSize.toLong()).toInt()
        }

        override fun encodeOutSizeProtected(unEncodedSize: Long): Long {
            var outSize: Long = (unEncodedSize + 2L) / 3L * 4L
            if (padEncoded) return outSize

            when (unEncodedSize - (unEncodedSize - unEncodedSize % 3)) {
                0L -> { /* no-op */ }
                1L -> outSize -= 2L
                2L -> outSize -= 1L
            }

            return outSize
        }

        override fun toStringAddSettings(sb: StringBuilder) {
            with(sb) {
                append("    encodeToUrlSafe: ")
                append(encodeToUrlSafe)
                appendLine()
                append("    padEncoded: ")
                append(padEncoded)
            }
        }

        internal companion object {

            @JvmSynthetic
            internal fun from(builder: Base64ConfigBuilder): Config {
                return Config(
                    isLenient = builder.isLenient,
                    encodeToUrlSafe = builder.encodeToUrlSafe,
                    padEncoded = builder.padEncoded,
                )
            }
        }
    }

    public object Default {

        /**
         * Base64 Default encoding characters.
         * */
        public const val CHARS: String = "ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz0123456789+/"
    }

    public object UrlSafe {

        /**
         * Base64 UrlSafe encoding characters.
         * */
        public const val CHARS: String = "ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz0123456789-_"
    }

    @ExperimentalEncodingApi
    override fun newDecoderFeed(out: OutFeed): Decoder.Feed {
        return object : Decoder.Feed() {

            private val buffer = Base64DecodingBuffer(out)

            override fun updateProtected(input: Byte) {
                val bits: Int = when (val c = input.char) {
                    in '0'..'9' -> {
                        // char ASCII value
                        //  0    48    52
                        //  9    57    61 (ASCII + 4)
                        c.code + 4
                    }
                    in 'A'..'Z' -> {
                        // char ASCII value
                        //  A    65    0
                        //  Z    90    25 (ASCII - 65)
                        c.code - 65
                    }
                    in 'a'..'z' -> {
                        // char ASCII value
                        //  a    97    26
                        //  z    122   51 (ASCII - 71)
                        c.code - 71
                    }
                    '+', '-' -> {
                        62
                    }
                    '/', '_' -> {
                        63
                    }
                    else -> {
                        throw EncodingException("Char[$c] is not a valid Base64 character")
                    }
                }

                buffer.update(bits)
            }

            override fun doFinalProtected() {
                buffer.finalize()
            }
        }
    }

    @ExperimentalEncodingApi
    override fun newEncoderFeed(out: OutFeed): Encoder.Feed {
        return object : Encoder.Feed() {

            private val buffer = Base64EncodingBuffer(
                out = out,
                table = if ((config as Base64.Config).encodeToUrlSafe) {
                    TABLE_URL_SAFE
                } else {
                    TABLE_DEFAULT
                },
                paddingByte = if ((config as Base64.Config).padEncoded) {
                    config.paddingByte
                } else {
                    null
                },
            )

            override fun updateProtected(input: Byte) {
                buffer.update(input)
            }

            override fun doFinalProtected() {
                buffer.finalize()
            }
        }
    }

    override fun name(): String = "Base64"

    private inner class Base64DecodingBuffer(out: OutFeed): DecodingBuffer(
        blockSize = 4,
        flush = { buffer ->
            var bitBuffer = 0

            // Append each char's 6 bits to the bitBuffer.
            for (bits in buffer) {
                bitBuffer = bitBuffer shl 6 or bits
            }

            // For every 4 chars of input, we accumulate 24 bits of output. Emit 3 bytes.
            out.invoke((bitBuffer shr 16).toByte())
            out.invoke((bitBuffer shr  8).toByte())
            out.invoke((bitBuffer       ).toByte())
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
                    out.invoke((bitBuffer shr 16).toByte())
                }
                3 -> {
                    // We read 3 chars, followed by "=". Emit 2 bytes for 16 of those 18 bits.
                    bitBuffer = bitBuffer shl  6
                    out.invoke((bitBuffer shr 16).toByte())
                    out.invoke((bitBuffer shr  8).toByte())
                }
            }
        },
    )

    private inner class Base64EncodingBuffer(
        out: OutFeed,
        table: EncodingTable,
        paddingByte: Byte?,
    ): EncodingBuffer(
        blockSize = 3,
        flush = { buffer ->
            // For every 3 chars of input, we accumulate
            // 24 bits of output. Emit 4 bytes.
            val b0 = buffer[0].toInt()
            val b1 = buffer[1].toInt()
            val b2 = buffer[2].toInt()
            out.invoke(table[(b0 and 0xff shr 2)])
            out.invoke(table[(b0 and 0x03 shl 4) or (b1 and 0xff shr 4)])
            out.invoke(table[(b1 and 0x0f shl 2) or (b2 and 0xff shr 6)])
            out.invoke(table[(b2 and 0x3f)])
        },
        finalize = { modulus, buffer ->
            val padCount: Int = when (modulus) {
                0 -> { 0 }
                1 -> {
                    val b0 = buffer[0].toInt()
                    out.invoke(table[b0 and 0xff shr 2])
                    out.invoke(table[b0 and 0x03 shl 4])
                    2
                }
                // 2
                else -> {
                    val b0 = buffer[0].toInt()
                    val b1 = buffer[1].toInt()
                    out.invoke(table[(b0 and 0xff shr 2)])
                    out.invoke(table[(b0 and 0x03 shl 4) or (b1 and 0xff shr 4)])
                    out.invoke(table[(b1 and 0x0f shl 2)])
                    1
                }
            }

            paddingByte?.let { byte ->
                repeat(padCount) {
                    out.invoke(byte)
                }
            }
        }
    )

    private companion object {
        private val TABLE_DEFAULT = EncodingTable.from(Default.CHARS)
        private val TABLE_URL_SAFE = EncodingTable.from(UrlSafe.CHARS)
    }
}
