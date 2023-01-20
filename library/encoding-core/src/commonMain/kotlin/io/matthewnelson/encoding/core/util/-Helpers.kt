/*
 * Copyright (c) 2023 Matthew Nelson
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *         https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 **/
@file:Suppress("KotlinRedundantDiagnosticSuppress")

package io.matthewnelson.encoding.core.util

/**
 * Helper for checking if a character is a space or
 * new line.
 *
 * @return true if the character matches '\n', '\r', ' ', or '\t'
 * */
@Suppress("NOTHING_TO_INLINE")
public inline fun Char.isSpaceOrNewLine(): Boolean {
    return when(this) {
        '\n', '\r', ' ', '\t' -> true
        else -> false
    }
}
