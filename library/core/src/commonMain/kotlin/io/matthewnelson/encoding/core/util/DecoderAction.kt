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

import io.matthewnelson.immutable.collections.toImmutableList
import io.matthewnelson.immutable.collections.toImmutableSet
import kotlin.jvm.JvmField

/**
 * An action for decoding
 *
 * @see [Parser]
 * */
public fun interface DecoderAction {

    /**
     * Convert decoder input character to its bitwise integer
     * */
    public fun convert(input: Char): Int

    /**
     * Holder for ranges of characters and their associated [DecoderAction]
     * with support for constant-time operations (in terms of work performed
     * for provided input).
     *
     * e.g. (Base16)
     *
     *     val parser = DecoderAction.Parser(
     *         '0'..'9' to DecoderAction { char ->
     *             char.code - 48
     *         },
     *         'A'..'Z' to DecoderAction { char ->
     *             char.code - 55
     *         }
     *     )
     *
     *     val bits = "48656c6c6f20576f726c6421".map { char ->
     *         parser.parse(char, isConstantTime = true)
     *             ?: error("Invalid Char[$char])
     *     }
     *
     * @param [action] Pairs of character ranges and their associated [DecoderAction]
     * */
    public class Parser(vararg action: Pair<Iterable<Char>, DecoderAction>) {

        @JvmField
        public val actions: List<Pair<Set<Char>, DecoderAction>>

        /**
         * Parses the [actions] for containing [input], invoking the
         * [DecoderAction.convert] for that associated range of characters.
         *
         * @param [input] the character to parse [actions] with
         * @param [isConstantTime] If `true`, will **not** jump out of
         *   loops early in the event a hit occurs.
         * @return The result of [DecoderAction.convert], or `null` if no
         *   match was found.
         * */
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
                val set = if (iterable is Collection<Char>) {
                    iterable
                } else {
                    iterable.mapTo(LinkedHashSet(1, 1.0f)) { it }
                }.toImmutableSet()

                set to action
            }.toImmutableList()

            this.actions = converted
        }
    }
}
