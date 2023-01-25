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

@OptIn(ExperimentalEncodingApi::class)
class TestEncoderDecoder(config: TestConfig): EncoderDecoder<TestConfig>(config) {
    override fun name(): String = "Test"

    @ExperimentalEncodingApi
    protected override fun newEncoderFeedProtected(out: Encoder.OutFeed): Encoder<TestConfig>.Feed {
        return object : Encoder<TestConfig>.Feed() {
            override fun consumeProtected(input: Byte) { out.output(Char.MAX_VALUE) }
            override fun doFinalProtected() { out.output(Char.MIN_VALUE) }
        }
    }

    @ExperimentalEncodingApi
    override fun newDecoderFeed(out: Decoder.OutFeed): Decoder<TestConfig>.Feed {
        return object : Decoder<TestConfig>.Feed() {
            override fun consumeProtected(input: Char) { out.output(Byte.MAX_VALUE) }
            override fun doFinalProtected() { out.output(Byte.MIN_VALUE) }
        }
    }
}
