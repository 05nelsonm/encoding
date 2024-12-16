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
package io.matthewnelson.encoding.benchmarks

import io.matthewnelson.encoding.base16.Base16
import io.matthewnelson.encoding.base32.Base32
import io.matthewnelson.encoding.base32.Base32Crockford
import io.matthewnelson.encoding.base32.Base32Default
import io.matthewnelson.encoding.base32.Base32Hex
import io.matthewnelson.encoding.base64.Base64
import io.matthewnelson.encoding.core.Decoder
import io.matthewnelson.encoding.core.Decoder.Companion.decodeToByteArray
import io.matthewnelson.encoding.core.Encoder
import io.matthewnelson.encoding.core.EncoderDecoder
import kotlinx.benchmark.*

abstract class EncoderDecoderBenchmarkBase {

    // "<Characters>:<isConstantTime>"
    abstract var params: String
    protected abstract fun encoder(isConstantTime: Boolean): EncoderDecoder<*>

    private var bytes = ByteArray(0)
    private var chars = "_"
    private var feedDecoder: Decoder<*>.Feed = Base16.newDecoderFeed {}.apply { close() }
    private var feedEncoder: Encoder<*>.Feed = Base16.newEncoderFeed {}.apply { close() }

    @Setup
    fun setup() {
        val (chars, isConstantTime) = params.split(':').let {
            it[0] to (it[1] == TIME_CONST)
        }
        val encoder = encoder(isConstantTime)

        val (cLength, bSize) = when (encoder) {
            is Base16 -> 2 to 1
            is Base32<*> -> 8 to 5
            is Base64 -> 4 to 3
            else -> error("Unknown encoder >> $encoder")
        }

        this.bytes = chars.decodeToByteArray(encoder)
        this.chars = chars
        require(this.bytes.size == bSize) {
            "bytes.size[${this.bytes.size}] did not match expected size[$bSize] for $encoder"
        }
        require(this.chars.length == cLength) {
            "chars.length[${this.chars.length}] did not match expected length[$cLength] for $encoder"
        }
        feedDecoder = encoder.newDecoderFeed {}
        feedEncoder = encoder.newEncoderFeed {}
    }

    @Benchmark
    fun decode() {
        chars.forEach { c -> feedDecoder.consume(c) }
    }

    @Benchmark
    fun encode() {
        bytes.forEach { b -> feedEncoder.consume(b) }
    }
}

@State(Scope.Benchmark)
@BenchmarkMode(Mode.AverageTime)
@OutputTimeUnit(BenchmarkTimeUnit.NANOSECONDS)
@Warmup(iterations = ENC_ITERATIONS_WARMUP, time = ENC_TIME_WARMUP)
@Measurement(iterations = ENC_ITERATIONS_MEASURE, time = ENC_TIME_MEASURE)
open class Base16Benchmark: EncoderDecoderBenchmarkBase() {
    // CHARS: 0123456789ABCDEF
    @Param("0A:$TIME_QUICK", "E8:$TIME_QUICK", "0A:$TIME_CONST", "E8:$TIME_CONST")
    override var params: String = "<Characters>:<isConstantTime>"
    override fun encoder(isConstantTime: Boolean): EncoderDecoder<*> {
        return Base16 { this.isConstantTime = isConstantTime }
    }
}

@State(Scope.Benchmark)
@BenchmarkMode(Mode.AverageTime)
@OutputTimeUnit(BenchmarkTimeUnit.NANOSECONDS)
@Warmup(iterations = ENC_ITERATIONS_WARMUP, time = ENC_TIME_WARMUP)
@Measurement(iterations = ENC_ITERATIONS_MEASURE, time = ENC_TIME_MEASURE)
open class Base32CrockfordBenchmark: EncoderDecoderBenchmarkBase() {
    // CHARS: 0123456789ABCDEFGHJKMNPQRSTVWXYZ
    @Param("0AC3DFJ7:$TIME_QUICK", "T9WYR8SZ:$TIME_QUICK", "0AC3DFJ7:$TIME_CONST", "T9WYR8SZ:$TIME_CONST")
    override var params: String = "<Characters>:<isConstantTime>"
    override fun encoder(isConstantTime: Boolean): EncoderDecoder<*> {
        return Base32Crockford { this.isConstantTime = isConstantTime }
    }
}

@State(Scope.Benchmark)
@BenchmarkMode(Mode.AverageTime)
@OutputTimeUnit(BenchmarkTimeUnit.NANOSECONDS)
@Warmup(iterations = ENC_ITERATIONS_WARMUP, time = ENC_TIME_WARMUP)
@Measurement(iterations = ENC_ITERATIONS_MEASURE, time = ENC_TIME_MEASURE)
open class Base32DefaultBenchmark: EncoderDecoderBenchmarkBase() {
    // CHARS: ABCDEFGHIJKLMNOPQRSTUVWXYZ234567
    @Param("CA2EF3CB:$TIME_QUICK", "WSY2V4ZZ:$TIME_QUICK", "CA2EF3CB:$TIME_CONST", "WSY2V4ZZ:$TIME_CONST")
    override var params: String = "<Characters>:<isConstantTime>"
    override fun encoder(isConstantTime: Boolean): EncoderDecoder<*> {
        return Base32Default { this.isConstantTime = isConstantTime }
    }
}

@State(Scope.Benchmark)
@BenchmarkMode(Mode.AverageTime)
@OutputTimeUnit(BenchmarkTimeUnit.NANOSECONDS)
@Warmup(iterations = ENC_ITERATIONS_WARMUP, time = ENC_TIME_WARMUP)
@Measurement(iterations = ENC_ITERATIONS_MEASURE, time = ENC_TIME_MEASURE)
open class Base32HexBenchmark: EncoderDecoderBenchmarkBase() {
    // CHARS: 0123456789ABCDEFGHIJKLMNOPQRSTUV
    @Param("A3B4CC2A:$TIME_QUICK", "V7RS4JM6:$TIME_QUICK", "A3B4CC2A:$TIME_CONST", "V7RS4JM6:$TIME_CONST")
    override var params: String = "<Characters>:<isConstantTime>"
    override fun encoder(isConstantTime: Boolean): EncoderDecoder<*> {
        return Base32Hex { this.isConstantTime = isConstantTime }
    }
}

@State(Scope.Benchmark)
@BenchmarkMode(Mode.AverageTime)
@OutputTimeUnit(BenchmarkTimeUnit.NANOSECONDS)
@Warmup(iterations = ENC_ITERATIONS_WARMUP, time = ENC_TIME_WARMUP)
@Measurement(iterations = ENC_ITERATIONS_MEASURE, time = ENC_TIME_MEASURE)
open class Base64Benchmark: EncoderDecoderBenchmarkBase() {
    // CHARS: ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz0123456789
    @Param("0CaI:$TIME_QUICK", "9tvw:$TIME_QUICK", "0CaI:$TIME_CONST", "9tvw:$TIME_CONST")
    override var params: String = "<Characters>:<isConstantTime>"
    override fun encoder(isConstantTime: Boolean): EncoderDecoder<*> {
        return Base64 { this.isConstantTime = isConstantTime }
    }
}
