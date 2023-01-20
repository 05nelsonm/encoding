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

import io.matthewnelson.encoding.core.helpers.TestConfig
import kotlin.test.Test
import kotlin.test.fail

class EncoderDecoderConfigUnitTest {

    @Test
    fun givenConfig_whenNegativeValuesSent_thenThrowsEncodingSizeExceptionBeforePassingToProtected() {
        val config = TestConfig(
            encodeReturn = {
                fail("Should not make it here")
            },
            decodeReturn = {
                fail("Should not make it here")
            }
        )

        try {
            config.decodeOutMaxSizeOrFail(-1L, null)
            fail()
        } catch (_: EncodingSizeException) {
            // pass
        }

        try {
            config.encodeOutSize(-1L)
            fail()
        } catch (_: EncodingSizeException) {
            // pass
        }
    }

    @Test
    fun givenConfig_whenNegativeValuesReturned_thenThrowsEncodingSizeException() {
        val config = TestConfig(
            encodeReturn = { -1L },
            decodeReturn = { -1L }
        )

        try {
            config.decodeOutMaxSizeOrFail(5, null)
            fail()
        } catch (_: EncodingSizeException) {
            // pass
        }

        try {
            config.encodeOutSize(5)
            fail()
        } catch (_: EncodingSizeException) {
            // pass
        }
    }

    @Test
    fun givenConfig_whenPositiveValuesSentAndReturned_thenDoseNotThrowException() {
        val config = TestConfig(
            encodeReturn = { 1L },
            decodeReturn = { 1L },
        )

        config.decodeOutMaxSizeOrFail(1L, null)
        config.encodeOutSize(1L)
    }

    @Test
    fun givenConfig_whenZeroPassed_thenReturns0Immediately() {
        val config = TestConfig(
            encodeReturn = { fail("Should not make it here") },
            decodeReturn = { fail("Should not make it here") },
        )

        config.decodeOutMaxSizeOrFail(0L, null)
        config.encodeOutSize(0L)
    }

    @Test
    fun givenConfig_whenZeroReturned_thenDoesNotThrowException() {
        val config = TestConfig(
            encodeReturn = { 0L },
            decodeReturn = { 0L },
        )

        config.decodeOutMaxSizeOrFail(1L, null)
        config.encodeOutSize(1L)
    }
}
