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

import io.matthewnelson.encoding.core.util.DecoderAction
import kotlinx.benchmark.*

@State(Scope.Benchmark)
@BenchmarkMode(Mode.AverageTime)
@Warmup(iterations = 5, time = 1)
@Measurement(iterations = 5, time = 3)
@OutputTimeUnit(BenchmarkTimeUnit.NANOSECONDS)
open class DecoderActionBenchmark {

    @Param(TIME_QUICK, TIME_CONST)
    var params: String = "-"

    private var isConstantTime = false
    private val parser = DecoderAction.Parser(
        '0'..'9' to DecoderAction { 0 },
        'A'..'F' to DecoderAction { 0 },
    )

    @Setup
    fun setup() {
        isConstantTime = params == TIME_CONST
    }

    @Benchmark
    fun actionFirst() = parser.parse('0', isConstantTime)

    @Benchmark
    fun actionLast() = parser.parse('F', isConstantTime)
}
