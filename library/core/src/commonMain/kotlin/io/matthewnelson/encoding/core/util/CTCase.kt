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

import io.matthewnelson.immutable.collections.toImmutableSet
import kotlin.jvm.JvmField

/**
 * Utility for converting letters to upper/lower case in a constant-time
 * manner (in terms of work performed).
 *
 * Both [CTCase.uppercase] and [CTCase.lowercase] perform the same number
 * of operations no matter the input (i.e. they do not return early).
 *
 * **NOTE:** Only characters where [Char.isLetter] is true are taken from
 * the provided table.
 *
 * @param [table] The decoding table (e.g. `ABCDEFGHIJKLMNOPQRSTUVWXYZ234567`)
 * @throws [IllegalArgumentException] if table contains a letter that has no
 *   corresponding lowercase value.
 * */
public class CTCase
@Throws(IllegalArgumentException::class)
public constructor(table: CharSequence) {

    /**
     * Uppercase letters
     * */
    @JvmField
    public val uppers: Set<Char>

    /**
     * Lowercase letters corresponding to [uppers]
     * */
    @JvmField
    public val lowers: Set<Char>

    /**
     * If the provided [char] exists within [lowers], its corresponding
     * uppercase value is returned. If nothing is found, `null` is returned.
     * */
    public fun uppercase(char: Char): Char? {
        val iLower = lowers.iterator()
        val iUpper = uppers.iterator()

        var result: Char? = null

        while (iLower.hasNext() && iUpper.hasNext()) {
            val cLower = iLower.next()
            val cUpper = iUpper.next()
            result = if (char == cLower) cUpper else result
        }

        return result
    }

    /**
     * If the provided [char] exists within [uppers], its corresponding
     * lowercase value is returned. If nothing is found, `null` is returned.
     * */
    public fun lowercase(char: Char): Char? {
        val iLower = lowers.iterator()
        val iUpper = uppers.iterator()

        var result: Char? = null

        while (iLower.hasNext() && iUpper.hasNext()) {
            val cLower = iLower.next()
            val cUpper = iUpper.next()
            result = if (char == cUpper) cLower else result
        }

        return result
    }

    init {
        val u = LinkedHashSet<Char>(table.length, 1.0f)
        val l = LinkedHashSet<Char>(table.length, 1.0f)

        table.forEach { char ->
            if (!char.isLetter()) return@forEach
            val cUpper = char.uppercaseChar()
            val cLower = cUpper.lowercaseChar()
            require(cUpper != cLower) { "Invalid character[$cUpper] (no corresponding lowercase value)" }
            u.add(cUpper)
            l.add(cLower)
        }

        require(u.size == l.size) { "uppers.size[${u.size}] != lowers.size[${l.size}] (invalid character input)." }

        uppers = u.toImmutableSet()
        lowers = l.toImmutableSet()
    }
}
