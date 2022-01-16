# component-encoding

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

```kotlin
// build.gradle.kts

dependencies {
    val encoding = "1.1.0"
    implementation("io.matthewnelson.kotlin-components:encoding-base16:$encoding")
    implementation("io.matthewnelson.kotlin-components:encoding-base32:$encoding")
    implementation("io.matthewnelson.kotlin-components:encoding-base64:$encoding")
}
```

```groovy
// build.gradle

dependencies {
    def encoding = "1.1.0"
    implementation "io.matthewnelson.kotlin-components:encoding-base16:$encoding"
    implementation "io.matthewnelson.kotlin-components:encoding-base32:$encoding"
    implementation "io.matthewnelson.kotlin-components:encoding-base64:$encoding"
}
```

### Kotlin Version Compatibility

**Note:** as of `1.1.0`, the experimental memory model for KotlinNative is enabled.

|  encoding  |    kotlin    |
| :--------: | :----------: |
|    1.0.3   |    1.5.31    |
|    1.1.0   |    1.6.10    |

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
