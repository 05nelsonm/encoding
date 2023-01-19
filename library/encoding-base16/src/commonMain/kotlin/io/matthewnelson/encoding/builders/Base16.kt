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
package io.matthewnelson.encoding.builders

import io.matthewnelson.encoding.base16.Base16
import io.matthewnelson.encoding.base16.Base16.Configuration
import kotlin.jvm.JvmField

public fun Base16(block: Base16Builder.() -> Unit): Base16 {
    val builder = Base16Builder()
    block.invoke(builder)
    return builder.build()
}

public class Base16Builder internal constructor() {

    /**
     * See [Configuration.isLenient]
     * */
    @JvmField
    public var isLenient: Boolean = false

    /**
     * See [Configuration.decodeLowercase]
     * */
    @JvmField
    public var decodeLowercase: Boolean = false

    /**
     * See [Configuration.encodeToLowercase]
     * */
    @JvmField
    public var encodeToLowercase: Boolean = false

    internal fun build(): Base16 {
        return Base16(Configuration(
            isLenient = isLenient,
            decodeLowercase = decodeLowercase,
            encodeToLowercase = encodeToLowercase,
        ))
    }
}
