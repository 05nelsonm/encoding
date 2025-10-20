/*
 * Copyright (c) 2025 Matthew Nelson
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
import io.matthewnelson.encoding.core.util.DecoderInput
import kotlinx.benchmark.Benchmark
import kotlinx.benchmark.BenchmarkMode
import kotlinx.benchmark.BenchmarkTimeUnit
import kotlinx.benchmark.Measurement
import kotlinx.benchmark.Mode
import kotlinx.benchmark.OutputTimeUnit
import kotlinx.benchmark.Scope
import kotlinx.benchmark.State
import kotlinx.benchmark.Warmup
import kotlin.random.Random

@State(Scope.Benchmark)
@BenchmarkMode(Mode.AverageTime)
@OutputTimeUnit(BenchmarkTimeUnit.NANOSECONDS)
@Warmup(iterations = ITERATIONS_WARMUP, time = TIME_WARMUP)
@Measurement(iterations = ITERATIONS_MEASURE, time = TIME_MEASURE)
open class DecoderInputBenchmark {

    private val array = CharArray(50) {
        val i = Random.nextInt(0, Base16.CHARS_UPPER.length)
        Base16.CHARS_UPPER[i]
    }
    private val i = Random.nextInt(0, array.size)
    private val input = DecoderInput(array)

    @Benchmark
    fun get() { input[i] }
}
