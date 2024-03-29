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
@file:Suppress("SpellCheckingInspection")

package io.matthewnelson.encoding.core

import kotlin.contracts.ExperimentalContracts
import kotlin.contracts.InvocationKind
import kotlin.contracts.contract
import io.matthewnelson.encoding.core.EncoderDecoder.Feed

/**
 * Executes the given [block] function and then closes
 * the [Feed] by either:
 *  - Calling [Feed.doFinal] if [block] **DID NOT** throw an
 *    exception and the feed is still open.
 *  - Calling [Feed.close] if [block] **DID** throw an
 *    exception.
 * */
@OptIn(ExperimentalContracts::class)
public inline fun <C: EncoderDecoder.Config, T: Feed<C>?, V> T.use(block: (T) -> V): V {
    contract {
        callsInPlace(block, InvocationKind.EXACTLY_ONCE)
    }

    var threw = false
    return try {
        block.invoke(this)
    } catch (t: Throwable) {
        threw = true
        throw t
    } finally {
        when {
            this == null -> {}
            !threw && !isClosed() -> doFinal()
            else -> close()
        }
    }
}
