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
package io.matthewnelson.encoding.core.util

import kotlin.jvm.JvmField

/**
 * DEPRECATED
 * @suppress
 * */
@Deprecated(
    message = "Implementation is incredibly slow. Do not use.",
    level = DeprecationLevel.ERROR,
)
public fun interface DecoderAction {

    public fun convert(input: Char): Int

    /**
     * DEPRECATED
     * @suppress
     * */
    @Deprecated(
        message = "Implementation is incredibly slow. Do not use.",
        level = DeprecationLevel.ERROR,
    )
    @Suppress("DEPRECATION_ERROR")
    public class Parser(vararg action: Pair<Iterable<Char>, DecoderAction>) {

        @JvmField
        public val actions: List<Pair<Set<Char>, DecoderAction>>

        public fun parse(input: Char, isConstantTime: Boolean): Int? {
            var da: DecoderAction? = null

            for ((chars, action) in actions) {
                for (c in chars) {
                    da = if (input == c) action else da
                    if (!isConstantTime && da != null) break
                }

                if (isConstantTime) continue
                if (da != null) break
            }

            return da?.convert(input)
        }

        init {
            val converted = action.map { (iterable, action) ->
                @Suppress("IfThenToElvis")
                val set = if (iterable is Collection<Char>) {
                    iterable
                } else {
                    iterable.mapTo(LinkedHashSet(1, 1.0f)) { it }
                }.toSet()

                set to action
            }.toList()

            this.actions = converted
        }
    }
}
