# component-encoding
[![badge-license]][url-license]
[![badge-latest-release]][url-latest-release]

[![badge-kotlin]][url-kotlin]

![badge-platform-android]
![badge-platform-jvm]
![badge-platform-js]
![badge-platform-js-node]
![badge-platform-linux]
![badge-platform-macos]
![badge-platform-ios]
![badge-platform-tvos]
![badge-platform-watchos]
![badge-platform-wasm]
![badge-platform-windows]
![badge-support-android-native]
![badge-support-apple-silicon]
![badge-support-js-ir]

**Base16 (Hex)**
 - [Default (Rfc 4648 section 8)](https://www.ietf.org/rfc/rfc4648.html#section-8)

**Base32**
 - [Crockford](https://www.crockford.com/base32.html)
 - [Default (Rfc 4648 section 6)](https://www.ietf.org/rfc/rfc4648.html#section-6)
 - [Hex (Rfc 4648 section 7)](https://www.ietf.org/rfc/rfc4648.html#section-7)

**Base64**
 - [Default (Rfc 4648 section 4)](https://www.ietf.org/rfc/rfc4648.html#section-4)
 - [Url Safe (Rfc 4648 section 5)](https://www.ietf.org/rfc/rfc4648.html#section-5)

A full list of `kotlin-components` projects can be found [HERE](https://kotlin-components.matthewnelson.io)

### Get Started

<!-- TAG_VERSION -->

```kotlin
// build.gradle.kts
dependencies {
    val encoding = "1.1.4"
    implementation("io.matthewnelson.kotlin-components:encoding-base16:$encoding")
    implementation("io.matthewnelson.kotlin-components:encoding-base32:$encoding")
    implementation("io.matthewnelson.kotlin-components:encoding-base64:$encoding")
}
```

<!-- TAG_VERSION -->

```groovy
// build.gradle
dependencies {
    def encoding = "1.1.4"
    implementation "io.matthewnelson.kotlin-components:encoding-base16:$encoding"
    implementation "io.matthewnelson.kotlin-components:encoding-base32:$encoding"
    implementation "io.matthewnelson.kotlin-components:encoding-base64:$encoding"
}
```

### Kotlin Version Compatibility

**Note:** as of `1.1.0`, the experimental memory model for KotlinNative is enabled.

<!-- TAG_VERSION -->

| encoding | kotlin |
|:--------:|:------:|
|  1.1.4   | 1.7.20 |
|  1.1.3   | 1.6.21 |
|  1.1.2   | 1.6.21 |
|  1.1.1   | 1.6.21 |
|  1.1.0   | 1.6.10 |
|  1.0.3   | 1.5.31 |

### Usage

Encoding/Decoding are extension functions, but below are example
classes for demonstration purposes.

```kotlin
import io.matthewnelson.component.encoding.base16.*

class Base16EncodeDecodeExample {
    
    fun base16Encode(bytes: ByteArray): String =
        bytes.encodeBase16()
    
    fun base16EncodeToCharArray(bytes: ByteArray): CharArray =
        bytes.encodeBase16ToCharArray()
        
    fun base16EncodeToByteArray(bytes: ByteArray): ByteArray =
        bytes.encodeBase16ToByteArray()
    
    fun base16Decode(string: String): ByteArray? =
        string.decodeBase16ToArray()
    
    fun base16Decode(chars: CharArray): ByteArray? =
        chars.decodeBase16ToArray()
}
```


```kotlin
import io.matthewnelson.component.encoding.base32.*

class Base32EncodeDecodeExample {

    // enable whichever for decoding/encoding
    private val base32: Base32 = Base32.Default
    // private val base32: Base32 = Base32.Hex
    // private val base32: Base32 = Base32.Crockford(checkSymbol = null)
    
    fun base32Encode(bytes: ByteArray): String =
        bytes.encodeBase32(base32 = base32)
    
    fun base32EncodeToCharArray(bytes: ByteArray): CharArray =
        bytes.encodeBase32ToCharArray(base32 = base32)
        
    fun base32EncodeToByteArray(bytes: ByteArray): ByteArray =
        bytes.encodeBase32ToByteArray(base32 = base32)
    
    fun base32Decode(string: String): ByteArray? =
        string.decodeBase32ToArray(base32 = base32)
    
    fun base32Decode(chars: CharArray): ByteArray? =
        chars.decodeBase32ToArray(base32 = base32)
}
```

```kotlin
import io.matthewnelson.component.encoding.base64.*

class Base64EncodeDecodeExample {

    // enable whichever for decoding/encoding
    private val base64: Base64 = Base64.Default
    // private val base64: Base64 = Base64.UrlSafe(pad = true)
    
    fun base64Encode(bytes: ByteArray): String =
        bytes.encodeBase64(base64 = base64)
    
    fun base64EncodeToCharArray(bytes: ByteArray): CharArray =
        bytes.encodeBase64ToCharArray(base64 = base64)
        
    fun base64EncodeToByteArray(bytes: ByteArray): ByteArray =
        bytes.encodeBase64ToByteArray(base64 = base64)
    
    fun base64Decode(string: String): ByteArray? =
        string.decodeBase64ToArray()
    
    fun base64Decode(chars: CharArray): ByteArray? =
        chars.decodeBase64ToArray()
}
```

### Git

This project utilizes git submodules. You will need to initialize them when
cloning the repository via:

```bash
$ git clone --recursive https://github.com/05nelsonm/component-encoding.git
```

If you've already cloned the repository, run:
```bash
$ git checkout master
$ git pull
$ git submodule update --init
```

In order to keep submodules updated when pulling the latest code, run:
```bash
$ git pull --recurse-submodules
```

<!-- TAG_VERSION -->
[badge-latest-release]: https://img.shields.io/badge/latest--release-1.1.4-blue.svg?style=flat
[badge-license]: https://img.shields.io/badge/license-Apache%20License%202.0-blue.svg?style=flat

<!-- TAG_DEPENDENCIES -->
[badge-kotlin]: https://img.shields.io/badge/kotlin-1.7.20-blue.svg?logo=kotlin

<!-- TAG_PLATFORMS -->
[badge-platform-android]: http://img.shields.io/badge/-android-6EDB8D.svg?style=flat
[badge-platform-jvm]: http://img.shields.io/badge/-jvm-DB413D.svg?style=flat
[badge-platform-js]: http://img.shields.io/badge/-js-F8DB5D.svg?style=flat
[badge-platform-js-node]: https://img.shields.io/badge/-nodejs-68a063.svg?style=flat
[badge-platform-linux]: http://img.shields.io/badge/-linux-2D3F6C.svg?style=flat
[badge-platform-macos]: http://img.shields.io/badge/-macos-111111.svg?style=flat
[badge-platform-ios]: http://img.shields.io/badge/-ios-CDCDCD.svg?style=flat
[badge-platform-tvos]: http://img.shields.io/badge/-tvos-808080.svg?style=flat
[badge-platform-watchos]: http://img.shields.io/badge/-watchos-C0C0C0.svg?style=flat
[badge-platform-wasm]: https://img.shields.io/badge/-wasm-624FE8.svg?style=flat
[badge-platform-windows]: http://img.shields.io/badge/-windows-4D76CD.svg?style=flat
[badge-support-android-native]: http://img.shields.io/badge/support-[AndroidNative]-6EDB8D.svg?style=flat
[badge-support-apple-silicon]: http://img.shields.io/badge/support-[AppleSilicon]-43BBFF.svg?style=flat
[badge-support-js-ir]: https://img.shields.io/badge/support-[js--IR]-AAC4E0.svg?style=flat

[url-latest-release]: https://github.com/05nelsonm/component-encoding/releases/latest
[url-license]: https://www.apache.org/licenses/LICENSE-2.0.txt
[url-kotlin]: https://kotlinlang.org
