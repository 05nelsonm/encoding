/*
 * Copyright (c) 2023 Matthew Nelson
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
package io.matthewnelson.encoding.core.util

import io.matthewnelson.encoding.core.Encoder
import kotlin.jvm.JvmField
import kotlin.jvm.JvmName

/**
 * A Wrapper around another [Encoder.OutFeed] to hijack the output and insert
 * new line characters at every expressed [interval].
 *
 * @param [interval] The interval at which new lines are inserted
 * @param [out] The other [Encoder.OutFeed]
 *
 * @throws [IllegalArgumentException] if [interval] is less than 0
 * */
public class LineBreakOutFeed
@Throws(IllegalArgumentException::class)
public constructor(
    @JvmField
    public val interval: Byte,
    private val out: Encoder.OutFeed,
): Encoder.OutFeed {

    init {
        require(interval > 0) { "interval must be greater than 0" }
    }

    @get:JvmName("count")
    public var count: Byte = 0
        private set

    /**
     * Resets the [count] to 0
     * */
    public fun reset() { count = 0 }

    override fun output(encoded: Char) {
        if (count == interval) {
            out.output('\n')
            count = 0
        }

        out.output(encoded)
        count++
    }
}
