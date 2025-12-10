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
@file:JvmName("TextUtil")
@file:Suppress("NOTHING_TO_INLINE")

package io.matthewnelson.encoding.core.util

import io.matthewnelson.encoding.core.internal.commonWipe
import kotlin.jvm.JvmName

/**
 * Wipes the [StringBuilder] backing array from index `0` to [StringBuilder.length]
 * (exclusive) with the null character `\u0000`, and then sets it's length back to `0`.
 * If [StringBuilder.length] is `0`, then [StringBuilder.capacity] will be used.
 *
 * On Kotlin/Js there is no backing array, so after setting length to `0` will return
 * early.
 *
 * @return [StringBuilder]
 * */
public inline fun StringBuilder.wipe(): StringBuilder = wipe(length)

/**
 * Wipes the [StringBuilder] backing array from index `0` to [len] (exclusive)
 * with the null character `\u0000`, and then sets it's length back to `0`.
 * If [len] is less than `1` or greater than [StringBuilder.capacity], then
 * [StringBuilder.capacity] is used in place of [len].
 *
 * On Kotlin/Js there is no backing array, so after setting length to `0` will return
 * early.
 *
 * @param [len] The length (exclusive), starting from index `0`, to wipe. If less
 *   than `1` or greater than [StringBuilder.capacity], then [StringBuilder.capacity]
 *   is used instead.
 *
 * @return [StringBuilder]
 * */
public fun StringBuilder.wipe(len: Int): StringBuilder = commonWipe(len, finalLen = 0)
