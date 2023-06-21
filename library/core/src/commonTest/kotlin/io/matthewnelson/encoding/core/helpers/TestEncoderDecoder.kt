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
package io.matthewnelson.encoding.core.helpers

import io.matthewnelson.encoding.core.*

class TestEncoderDecoder(
    config: TestConfig,
    private val encoderDoFinal: ((Encoder<TestConfig>.Feed) -> Unit)? = null,
    private val decoderDoFinal: ((Decoder<TestConfig>.Feed) -> Unit)? = null,
): EncoderDecoder<TestConfig>(config) {
    override fun name(): String = "Test"

    override fun newEncoderFeedProtected(out: Encoder.OutFeed): Encoder<TestConfig>.Feed {
        return object : Encoder<TestConfig>.Feed() {
            override fun consumeProtected(input: Byte) { out.output(Char.MAX_VALUE) }
            override fun doFinalProtected() {
                encoderDoFinal?.invoke(this) ?: out.output(Char.MIN_VALUE)
            }
        }
    }

    override fun newDecoderFeedProtected(out: Decoder.OutFeed): Decoder<TestConfig>.Feed {
        return object : Decoder<TestConfig>.Feed() {
            override fun consumeProtected(input: Char) { out.output(Byte.MAX_VALUE) }
            override fun doFinalProtected() {
                decoderDoFinal?.invoke(this) ?: out.output(Byte.MIN_VALUE)
            }
        }
    }
}
