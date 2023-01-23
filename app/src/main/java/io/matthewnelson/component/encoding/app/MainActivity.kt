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
package io.matthewnelson.component.encoding.app

import android.annotation.SuppressLint
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import by.kirich1409.viewbindingdelegate.viewBinding
import io.matthewnelson.component.encoding.app.databinding.ActivityMainBinding
import io.matthewnelson.encoding.builders.*
import io.matthewnelson.encoding.core.Encoder.Companion.encodeToString

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
    }
}
