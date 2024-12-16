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

import io.matthewnelson.encoding.core.util.CTCase
import kotlinx.benchmark.*

@State(Scope.Benchmark)
@BenchmarkMode(Mode.AverageTime)
@Warmup(iterations = 5, time = 1)
@Measurement(iterations = 5, time = 3)
@OutputTimeUnit(BenchmarkTimeUnit.NANOSECONDS)
open class CTCaseBenchmark {

    private val case = CTCase("ABCDEFGH")

    @Benchmark
    fun lowercaseFirst(): Char = case.lowercase('A')!!
    @Benchmark
    fun lowercaseLast(): Char = case.lowercase('H')!!

    @Benchmark
    fun uppercaseFirst(): Char = case.uppercase('a')!!
    @Benchmark
    fun uppercaseLast(): Char = case.uppercase('h')!!
}
