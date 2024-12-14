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

package io.matthewnelson.encoding.base32.internal

private const val CHECK_SYMBOLS = "*~$=Uu"

@Suppress("NOTHING_TO_INLINE")
internal inline fun Char.isCheckSymbol(isConstantTime: Boolean = false): Boolean {
    var isSymbol = false
    for (c in CHECK_SYMBOLS) {
        if (this != c) continue
        if (!isConstantTime) return true
        isSymbol = true
    }
    return isSymbol
}
