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
@file:Suppress("unused")

package io.matthewnelson.encoding.benchmarks

import io.matthewnelson.encoding.core.util.FeedBuffer
import kotlinx.benchmark.*

@State(Scope.Benchmark)
@BenchmarkMode(Mode.AverageTime)
@OutputTimeUnit(BenchmarkTimeUnit.NANOSECONDS)
@Warmup(iterations = ITERATIONS_WARMUP, time = TIME_WARMUP)
@Measurement(iterations = ITERATIONS_MEASURE, time = TIME_MEASURE)
open class FeedBufferBenchmark {

    private val buffer = object : FeedBuffer(
        blockSize = 10,
        flush = Flush { _ -> },
        finalize = Finalize { _, _ -> }
    ) {}

    @Benchmark
    fun update() { buffer.update(42) }
}
