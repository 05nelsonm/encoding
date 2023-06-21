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
package io.matthewnelson.encoding.core

public open class EncodingException: RuntimeException {

    final override val message: String

    public constructor(message: String): this(message, null)
    public constructor(message: String, cause: Throwable?): super(message, cause) {
        this.message = message
    }
}

public open class EncodingSizeException: EncodingException {
    public constructor(message: String): this(message, null)
    public constructor(message: String, cause: Throwable?): super(message, cause)
}
