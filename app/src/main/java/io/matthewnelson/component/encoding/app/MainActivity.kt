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
import io.matthewnelson.component.base64.Base64
import io.matthewnelson.component.base64.encodeBase64
import io.matthewnelson.component.encoding.app.databinding.ActivityMainBinding
import io.matthewnelson.component.encoding.base16.encodeBase16
import io.matthewnelson.component.encoding.base32.Base32
import io.matthewnelson.component.encoding.base32.encodeBase32

class MainActivity: AppCompatActivity(R.layout.activity_main) {

    companion object {
        const val HELLO_WORLD = "Hello World!"
    }

    private val binding: ActivityMainBinding by viewBinding(ActivityMainBinding::bind)

    @SuppressLint("SetTextI18n")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val bytes = HELLO_WORLD.encodeToByteArray()
        val base16 = bytes.encodeBase16()
        val crockford = bytes.encodeBase32(Base32.Crockford('*'))
        val default = bytes.encodeBase32(Base32.Default)
        val hex = bytes.encodeBase32(Base32.Hex)
        val base64 = bytes.encodeBase64(Base64.Default)
        val base64UrlSafe = bytes.encodeBase64(Base64.UrlSafe(pad = true))

        binding.textViewBase16.text = "Base16 (hex):\n$base16"
        binding.textViewCrockford.text = "Base32 Crockford(checkSymbol = *):\n$crockford"
        binding.textViewDefault.text = "Base32 Default:\n$default"
        binding.textViewHex.text = "Base32 Hex:\n$hex"
        binding.textViewBase64.text = "Base64 Default:\n$base64"
        binding.textViewBase64UrlSafe.text = "Base64 UrlSafe:\n$base64UrlSafe"
    }
}
