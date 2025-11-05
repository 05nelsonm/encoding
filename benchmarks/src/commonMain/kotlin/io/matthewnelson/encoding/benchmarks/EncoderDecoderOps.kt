/*
 * Copyright (c) 2024 Matthew Nelson
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 **/
@file:Suppress("UNUSED")

package io.matthewnelson.encoding.benchmarks

import io.matthewnelson.encoding.base16.Base16
import io.matthewnelson.encoding.base32.Base32
import io.matthewnelson.encoding.base64.Base64
import io.matthewnelson.encoding.core.Decoder
import io.matthewnelson.encoding.core.Decoder.Companion.decodeToByteArray
import io.matthewnelson.encoding.core.Encoder
import io.matthewnelson.encoding.core.Encoder.Companion.encodeToString
import io.matthewnelson.encoding.core.EncoderDecoder
import io.matthewnelson.encoding.utf8.UTF8
import kotlinx.benchmark.*

abstract class EncoderDecoderBenchmarkBase {

    // "<Char>:<Byte>"
    abstract var params: String
    protected abstract val encoder: EncoderDecoder<*>

    private var byte: Byte = 0
    private var char = '_'
    private val feedDecoder: Decoder<*>.Feed by lazy { encoder.newDecoderFeed {} }
    private val feedEncoder: Encoder<*>.Feed by lazy { encoder.newEncoderFeed {} }

    @Setup
    fun setup() {
        params.split(':').let { params ->
            char = params[0][0]
            byte = params[1].toByte()
        }
        feedDecoder
        feedEncoder
    }

    @Benchmark
    fun decode() { feedDecoder.consume(char) }

    @Benchmark
    fun encode() { feedEncoder.consume(byte) }
}

@State(Scope.Benchmark)
@BenchmarkMode(Mode.AverageTime)
@OutputTimeUnit(BenchmarkTimeUnit.NANOSECONDS)
@Warmup(iterations = ITERATIONS_WARMUP, time = TIME_WARMUP)
@Measurement(iterations = ITERATIONS_MEASURE, time = TIME_MEASURE)
open class Base16Benchmark: EncoderDecoderBenchmarkBase() {
    // CHARS: 0123456789ABCDEF
    @Param("3:0", "d:122")
    override var params: String = "<Char>:<Byte>"
    override val encoder: EncoderDecoder<*> = Base16.Builder {}
}

@State(Scope.Benchmark)
@BenchmarkMode(Mode.AverageTime)
@OutputTimeUnit(BenchmarkTimeUnit.NANOSECONDS)
@Warmup(iterations = ITERATIONS_WARMUP, time = TIME_WARMUP)
@Measurement(iterations = ITERATIONS_MEASURE, time = TIME_MEASURE)
open class Base32CrockfordBenchmark: EncoderDecoderBenchmarkBase() {
    // CHARS: 0123456789ABCDEFGHJKMNPQRSTVWXYZ
    @Param("3:-6", "x:115")
    override var params: String = "<Char>:<Byte>"
    override val encoder: EncoderDecoder<*> = Base32.Crockford.Builder {}
}

@State(Scope.Benchmark)
@BenchmarkMode(Mode.AverageTime)
@OutputTimeUnit(BenchmarkTimeUnit.NANOSECONDS)
@Warmup(iterations = ITERATIONS_WARMUP, time = TIME_WARMUP)
@Measurement(iterations = ITERATIONS_MEASURE, time = TIME_MEASURE)
open class Base32DefaultBenchmark: EncoderDecoderBenchmarkBase() {
    // CHARS: ABCDEFGHIJKLMNOPQRSTUVWXYZ234567
    @Param("C:-123", "w:15")
    override var params: String = "<Char>:<Byte>"
    override val encoder: EncoderDecoder<*> = Base32.Default.Builder {}
}

@State(Scope.Benchmark)
@BenchmarkMode(Mode.AverageTime)
@OutputTimeUnit(BenchmarkTimeUnit.NANOSECONDS)
@Warmup(iterations = ITERATIONS_WARMUP, time = TIME_WARMUP)
@Measurement(iterations = ITERATIONS_MEASURE, time = TIME_MEASURE)
open class Base32HexBenchmark: EncoderDecoderBenchmarkBase() {
    // CHARS: 0123456789ABCDEFGHIJKLMNOPQRSTUV
    @Param("A:-12", "r:42")
    override var params: String = "<Char>:<Byte>"
    override val encoder: EncoderDecoder<*> = Base32.Hex.Builder {}
}

@State(Scope.Benchmark)
@BenchmarkMode(Mode.AverageTime)
@OutputTimeUnit(BenchmarkTimeUnit.NANOSECONDS)
@Warmup(iterations = ITERATIONS_WARMUP, time = TIME_WARMUP)
@Measurement(iterations = ITERATIONS_MEASURE, time = TIME_MEASURE)
open class Base64Benchmark: EncoderDecoderBenchmarkBase() {
    // CHARS: ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz0123456789
    @Param("2:84", "w:22")
    override var params: String = "<Char>:<Byte>"
    override val encoder: EncoderDecoder<*> = Base64.Builder {}
}

@State(Scope.Benchmark)
@BenchmarkMode(Mode.AverageTime)
@OutputTimeUnit(BenchmarkTimeUnit.NANOSECONDS)
@Warmup(iterations = ITERATIONS_WARMUP, time = TIME_WARMUP)
@Measurement(iterations = ITERATIONS_MEASURE, time = TIME_MEASURE)
open class UTF8Benchmark: EncoderDecoderBenchmarkBase() {
    @Param("¿:-84")
    override var params: String = "<Char>:<Byte>"
    override val encoder: EncoderDecoder<*> = UTF8

    private val bytes = "7f e0a080 e09fafeda080edbfbfedaf41 e0a080 efbfbf efaf7a f0808080 c480"
        .decodeToByteArray(Base16)
    private val string = bytes.encodeToString(encoder)

    @Benchmark
    fun toBytesEncoding() { string.decodeToByteArray(encoder) }

    @Benchmark
    fun toStringEncoding() { bytes.encodeToString(encoder) }

    @Benchmark
    fun toBytesKotlin() { string.encodeToByteArray() }

    @Benchmark
    fun toStringKotlin() { bytes.decodeToString() }
}
