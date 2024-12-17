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
import io.matthewnelson.encoding.base32.Base32Crockford
import io.matthewnelson.encoding.base32.Base32Default
import io.matthewnelson.encoding.base32.Base32Hex
import io.matthewnelson.encoding.base64.Base64
import io.matthewnelson.encoding.core.Decoder
import io.matthewnelson.encoding.core.Encoder
import io.matthewnelson.encoding.core.EncoderDecoder
import kotlinx.benchmark.*

abstract class EncoderDecoderBenchmarkBase {

    // "<Char>:<Byte>:<isConstantTime>"
    abstract var params: String
    protected abstract fun encoder(isConstantTime: Boolean): EncoderDecoder<*>

    private var byte: Byte = 0
    private var char = '_'
    private var feedDecoder: Decoder<*>.Feed = Base16.newDecoderFeed {}.apply { close() }
    private var feedEncoder: Encoder<*>.Feed = Base16.newEncoderFeed {}.apply { close() }

    @Setup
    fun setup() {
        val encoder = params.split(':').let { params ->
            char = params[0][0]
            byte = params[1].toByte()
            encoder(params[2] == TIME_CONST)
        }

        feedDecoder = encoder.newDecoderFeed {}
        feedEncoder = encoder.newEncoderFeed {}
    }

    @Benchmark
    fun decode() { feedDecoder.consume(char) }

    @Benchmark
    fun encode() { feedEncoder.consume(byte) }
}

@State(Scope.Benchmark)
@BenchmarkMode(Mode.AverageTime)
@OutputTimeUnit(BenchmarkTimeUnit.NANOSECONDS)
@Warmup(iterations = ENC_ITERATIONS_WARMUP, time = ENC_TIME_WARMUP)
@Measurement(iterations = ENC_ITERATIONS_MEASURE, time = ENC_TIME_MEASURE)
open class Base16Benchmark: EncoderDecoderBenchmarkBase() {
    // CHARS: 0123456789ABCDEF
    @Param("3:0:$TIME_CONST", "d:122:$TIME_CONST")
    override var params: String = "<Char>:<Byte>:<isConstantTime>"
    override fun encoder(isConstantTime: Boolean): EncoderDecoder<*> {
        return Base16()
    }
}

@State(Scope.Benchmark)
@BenchmarkMode(Mode.AverageTime)
@OutputTimeUnit(BenchmarkTimeUnit.NANOSECONDS)
@Warmup(iterations = ENC_ITERATIONS_WARMUP, time = ENC_TIME_WARMUP)
@Measurement(iterations = ENC_ITERATIONS_MEASURE, time = ENC_TIME_MEASURE)
open class Base32CrockfordBenchmark: EncoderDecoderBenchmarkBase() {
    // CHARS: 0123456789ABCDEFGHJKMNPQRSTVWXYZ
    @Param("3:-6:$TIME_QUICK", "x:115:$TIME_QUICK", "3:-6:$TIME_CONST", "x:115:$TIME_CONST")
    override var params: String = "<Char>:<Byte>:<isConstantTime>"
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
    @Param("C:-123:$TIME_QUICK", "w:15:$TIME_QUICK", "C:-123:$TIME_CONST", "w:15:$TIME_CONST")
    override var params: String = "<Char>:<Byte>:<isConstantTime>"
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
    @Param("A:-12:$TIME_QUICK", "r:42:$TIME_QUICK", "A:-12:$TIME_CONST", "r:42:$TIME_CONST")
    override var params: String = "<Char>:<Byte>:<isConstantTime>"
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
    @Param("2:84:$TIME_CONST", "w:22:$TIME_CONST")
    override var params: String = "<Char>:<Byte>:<isConstantTime>"
    override fun encoder(isConstantTime: Boolean): EncoderDecoder<*> {
        return Base64()
    }
}
