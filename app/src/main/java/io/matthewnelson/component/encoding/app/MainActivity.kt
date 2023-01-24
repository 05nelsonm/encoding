/*
*  Copyright 2021 Matthew Nelson
*
*  Licensed under the Apache License, Version 2.0 (the "License");
*  you may not use this file except in compliance with the License.
*  You may obtain a copy of the License at
*
*      http://www.apache.org/licenses/LICENSE-2.0
*
*  Unless required by applicable law or agreed to in writing, software
*  distributed under the License is distributed on an "AS IS" BASIS,
*  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
*  See the License for the specific language governing permissions and
*  limitations under the License.
* */
@file:Suppress("SpellCheckingInspection")

package io.matthewnelson.component.encoding.app

import android.annotation.SuppressLint
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import by.kirich1409.viewbindingdelegate.viewBinding
import io.matthewnelson.component.encoding.app.databinding.ActivityMainBinding
import io.matthewnelson.encoding.builders.*
import io.matthewnelson.encoding.core.Encoder.Companion.encodeToString
import io.matthewnelson.encoding.core.ExperimentalEncodingApi
import io.matthewnelson.encoding.core.use
import java.io.File

class MainActivity: AppCompatActivity(R.layout.activity_main) {

    companion object {
        const val HELLO_WORLD = "Hello World!"

        private val base16EncoderDecoder = Base16 {
            isLenient = false
            encodeToLowercase = true
        }

        private val base32CrockfordEncoderDecoder = Base32Crockford {
            isLenient = false
            encodeToLowercase = false
            hyphenInterval = 5
            checkSymbol('*')
        }

        private val base32DefaultEncoderDecoder = Base32Default {
            isLenient = false
            encodeToLowercase = true
            padEncoded = false
        }

        private val base32HexEncoderDecoder = Base32Hex {
            isLenient = false
            encodeToLowercase = true
            padEncoded = false
        }

        private val base64DefaultEncoderDecoder = Base64 {
            isLenient = true
            encodeToUrlSafe = false
            padEncoded = true
        }

        private val base64UrlSafeEncoderDecoder = Base64 {
            isLenient = false
            encodeToUrlSafe = true
            padEncoded = false
        }
    }

    private val binding: ActivityMainBinding by viewBinding(ActivityMainBinding::bind)

    @SuppressLint("SetTextI18n")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val bytes = HELLO_WORLD.encodeToByteArray()

        val base16 = bytes.encodeToString(base16EncoderDecoder)

        val crockford = bytes.encodeToString(base32CrockfordEncoderDecoder)
        val default = bytes.encodeToString(base32DefaultEncoderDecoder)
        val hex = bytes.encodeToString(base32HexEncoderDecoder)

        val base64 = bytes.encodeToString(base64DefaultEncoderDecoder)
        val base64UrlSafe = bytes.encodeToString(base64UrlSafeEncoderDecoder)

        binding.textViewBase16.text = "Base16 (hex):\n$base16"

        binding.textViewCrockford.text = "Base32 Crockford[checkSymbol = *, hyphenInterval = 5]:\n$crockford"
        binding.textViewDefault.text = "Base32 Default:\n$default"
        binding.textViewHex.text = "Base32 Hex:\n$hex"

        binding.textViewBase64.text = "Base64 Default:\n$base64"
        binding.textViewBase64UrlSafe.text = "Base64 UrlSafe:\n$base64UrlSafe"

        // NOTE: This should not be done on the Main thread or in onCreate...
        val file = File(applicationContext.applicationInfo.dataDir, "hello_world.txt")

        // Stream encoded data to a File
        file.outputStream().use { oStream ->

            @OptIn(ExperimentalEncodingApi::class)
            base64DefaultEncoderDecoder.newEncoderFeed { encodedChar ->
                // Write to the stream with every character
                // that is pushed out of the feed.
                oStream.write(encodedChar.code)
            }.use { feed ->

                HELLO_WORLD.forEach { c ->
                    // Update the feed with each character
                    // of Hello World!
                    feed.consume(c.code.toByte())
                }
            }
        }

        // Read the encoded data from the file
        val sb = StringBuilder()
        file.inputStream().reader().use { iStream ->

            @OptIn(ExperimentalEncodingApi::class)
            base64DefaultEncoderDecoder.newDecoderFeed { decodedByte ->
                // Update the StringBuilder with every decoded
                // byte that is pushed out of the feed.
                sb.append(decodedByte.toInt().toChar())
            }.use { feed ->

                // Acquire a perfectly sized buffer for the encoding config being used
                val size: Int = feed.config.decodeOutMaxSize(file.length()).let { size ->
                    if (size > 4096L) {
                        4096
                    } else {
                        size.toInt()
                    }
                }

                val buffer = CharArray(size)
                while (true) {
                    val read = iStream.read(buffer)
                    if (read == -1) break

                    for (i in 0 until read) {
                        // Update the feed with each character from the file
                        feed.consume(buffer[i])
                    }
                }
            }
        }

        println(sb.toString())
    }
}
