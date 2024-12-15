# encoding
[![badge-license]][url-license]
[![badge-latest-release]][url-latest-release]

[![badge-kotlin]][url-kotlin]
[![badge-immutable]][url-immutable]

![badge-platform-android]
![badge-platform-jvm]
![badge-platform-js]
![badge-platform-js-node]
![badge-platform-wasm]
![badge-platform-linux]
![badge-platform-macos]
![badge-platform-ios]
![badge-platform-tvos]
![badge-platform-watchos]
![badge-platform-windows]
![badge-support-android-native]
![badge-support-apple-silicon]
![badge-support-js-ir]
![badge-support-linux-arm]

<!--
![badge-platform-wasm]
-->

Configurable, streamable, efficient and extensible Encoding/Decoding for Kotlin Multiplatform.

**Base16 (a.k.a. "hex")**
 - [RFC 4648 section 8](https://www.ietf.org/rfc/rfc4648.html#section-8)

**Base32**
 - [Crockford](https://www.crockford.com/base32.html)
 - Default [RFC 4648 section 6](https://www.ietf.org/rfc/rfc4648.html#section-6)
 - Hex [RFC 4648 section 7](https://www.ietf.org/rfc/rfc4648.html#section-7)

**Base64**
 - Default [RFC 4648 section 4](https://www.ietf.org/rfc/rfc4648.html#section-4)
 - UrlSafe [RFC 4648 section 5](https://www.ietf.org/rfc/rfc4648.html#section-5)

A full list of `kotlin-components` projects can be found [HERE](https://kotlin-components.matthewnelson.io)

### Usage

**Configure `EncoderDecoder`(s) to your needs**

```kotlin
val base16 = Base16 {
    // Ignore whitespace and new lines when decoding
    isLenient = true

    // Insert line breaks every X characters of encoded output
    lineBreakInterval = 10

    // Use lowercase instead of uppercase characters when encoding
    encodeToLowercase = true

    // Use constant-time operations for sensitive data
    isConstantTime = true
}

// Shortcuts
val base16StrictSettings = Base16(strict = true)
val base16DefaultSettings = Base16()

// Alternatively, use the static instance with its default settings
Base16
```

```kotlin
val base32Crockford = Base32Crockford {
    isLenient = true
    encodeToLowercase = false
    isConstantTime = true

    // Insert hyphens every X characters of encoded output
    hyphenInterval = 5

    // Optional data integrity check unique to the Crockford spec
    checkSymbol('*')

    // Only apply the checkSymbol & reset hyphen interval counter
    // when Encoder.Feed.doFinal is called (see builder docs for
    // more info) 
    finalizeWhenFlushed = false
}

// Alternatively, use the static instance with its default settings
Base32.Crockford

val base32Default = Base32Default {
    isLenient = true
    lineBreakInterval = 64
    encodeToLowercase = true
    isConstantTime = true
    
    // Skip padding of the encoded output
    padEncoded = false
}

// Alternatively, use the static instance with its default settings
Base32.Default

val base32Hex = Base32Hex {
    isLenient = true
    lineBreakInterval = 64
    encodeToLowercase = false
    padEncoded = true
    isConstantTime = true
}

// Alternatively, use the static instance with its default settings
Base32.Hex
```

```kotlin
// NOTE: Base64 can _decode_ both Default and UrlSafe, no matter what
// encodeToUrlSafe is set to.
val base64 = Base64 {
    isLenient = true
    lineBreakInterval = 64
    encodeToUrlSafe = false
    padEncoded = true
    isConstantTime = true
}

// Alternatively, use the static instance with its default settings
Base64.Default

// Inherit settings from another EncoderDecoder's Config
val base64UrlSafe = Base64(base64.config) {
    encodeToUrlSafe = true
    padEncoded = false
}

// Alternatively, use the static instance with its default settings
Base64.UrlSafe
```

**Encoding/Decoding Extension Functions**

```kotlin
val text = "Hello World!"
val bytes = text.encodeToByteArray()

// Choose the output type that suits your needs
// without having to perform unnecessary intermediate
// transformations (can be useful for security 
// purposes, too, as you are able to clear Arrays
// before they are de-referenced).
val encodedString = bytes.encodeToString(Base64.Default)
val encodedChars = bytes.encodeToCharArray(Base32.Default)
val encodedBytes = bytes.encodeToByteArray(Base16)

val decodedString = try {
    encodedString.decodeToByteArray(Base64.Default)
} catch (e: EncodingException) {
    Log.e("Something went terribly wrong", e)
    null
}
// Swallow `EncodingException`s by using the `*OrNull` variants
val decodedChars = encodedChars.decodeToByteArrayOrNull(Base32.Default)
val decodedBytes = encodedBytes.decodeToByteArrayOrNull(Base16)
```

**Encoding/Decoding `Feed`(s) (i.e. Streaming)**

`Feed`'s are a new concept which enable some pretty awesome things. They break 
the encoding/decoding process into its individual parts, such that the medium 
for which data is coming from or going to can be **anything**; `Feed`'s only 
care about `Byte`(s) and `Char`(s)!

```kotlin
// e.g. Concatenate multiple encodings
val sb = StringBuilder()

// Use our own line break out feed in order to add a delimiter between
// encodings and preserve the counter.
val out = LineBreakOutFeed(interval = 64) { char -> sb.append(char) }

Base64.Default.newEncoderFeed(out).use { feed ->
    "Hello World 1!".forEach { c -> feed.consume(c.code.toByte())  }
    feed.flush()
    out.output('.')
    "Hello World 2!".forEach { c -> feed.consume(c.code.toByte())  }
}

println(sb.toString())
// SGVsbG8gV29ybGQgMSE=.SGVsbG8gV29ybGQgMiE=
```

```kotlin
// e.g. Writing encoded data to a File in Java.
// NOTE: try/catch omitted for this example.

file.outputStream().use { oStream ->
    Base64.Default.newEncoderFeed { encodedChar ->
        // As encoded data comes out of the feed,
        // write it to the file.
        oStream.write(encodedChar.code)
    }.use { feed ->

        // Push data through the feed.
        //
        // There are NO size/length limitations with `Feed`s.
        // You are only limited by the medium you use to store
        // the output (e.g. the maximum size of a ByteArray is
        // Int.MAX_VALUE).
        //
        // The `Feed.use` extension function calls `doFinal`
        // automatically, which closes the `Encoder.Feed`
        // and performs finalization of the operation (such as
        // adding padding).
        "Hello World!".forEach { c ->
            feed.consume(c.code.toByte())
        }
    }
}
```

As `Feed`(s) is a new concept, they can be "bulky" to use (as you will see in 
the example below). This is due to a lack of extension functions for them, but 
it's something I hope can be built out over time with your help (PRs and 
FeatureRequests are **always** welcome)!

```kotlin
// e.g. Reading encoded data from a File in Java.
// NOTE: try/catch omitted for this example.

// Pre-calculate the output size for the given encoding
// spec; in this case, Base64.
val size = Base64.Default.config.decodeOutMaxSize(file.length())

// Since we will be storing the data in a StringBuilder,
// we need to check if the output size would exceed
// StringBuilder's maximum capacity.
if (size > Int.MAX_VALUE.toLong()) {
    // Alternatively, one could fall back to chunking, but that
    // is beyond the scope of this example.
    throw EncodingSizeException(
        "File contents would be too large after decoding to store in a StringBuilder"
    )
}

val sb = StringBuilder(size.toInt())

file.inputStream().reader().use { iStreamReader ->
    Base64.Default.newDecoderFeed { decodedByte ->
        // As decoded data comes out of the feed,
        // update the StringBuilder.
        sb.append(decodedByte.toInt().toChar())
    }.use { feed ->

        val buffer = CharArray(4096)
        while (true) {
            val read = iStreamReader.read(buffer)
            if (read == -1) break
            
            // Push encoded data from the file through the feed.
            //
            // The `Feed.use` extension function calls `doFinal`
            // automatically, which closes the `Decoder.Feed`
            // and performs finalization of the operation.
            for (i in 0 until read) {
                feed.consume(buffer[i])
            }
        }
    }
}

println(sb.toString())
```

**Alternatively, create your own `EncoderDecoder`(s) using the abstractions provided by `encoding-core`!**


### Sample

See [sample project](sample/README.md)

### Get Started

<!-- TAG_VERSION -->

```kotlin
// build.gradle.kts
dependencies {
    val encoding = "2.2.2"
    implementation("io.matthewnelson.encoding:base16:$encoding")
    implementation("io.matthewnelson.encoding:base32:$encoding")
    implementation("io.matthewnelson.encoding:base64:$encoding")

    // Only necessary if you just want the abstractions to create your own EncoderDecoder(s)
    implementation("io.matthewnelson.encoding:core:$encoding")
}
```

<!-- TAG_VERSION -->
Alternatively, you can use the BOM.

```kotlin
// build.gradle.kts
dependencies {
    // define the BOM and its version
    implementation(platform("io.matthewnelson.encoding:bom:2.2.2"))

    // define artifacts without version
    implementation("io.matthewnelson.encoding:base16")
    implementation("io.matthewnelson.encoding:base32")
    implementation("io.matthewnelson.encoding:base64")

    // Only necessary if you just want the abstractions to create your own EncoderDecoder(s)
    implementation("io.matthewnelson.encoding:core")
}
```

<!-- TAG_VERSION -->
[badge-latest-release]: https://img.shields.io/badge/latest--release-2.2.2-blue.svg?style=flat
[badge-license]: https://img.shields.io/badge/license-Apache%20License%202.0-blue.svg?style=flat

<!-- TAG_DEPENDENCIES -->
[badge-kotlin]: https://img.shields.io/badge/kotlin-1.9.24-blue.svg?logo=kotlin
[badge-immutable]: https://img.shields.io/badge/immutable-0.1.4-blue.svg?style=flat

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
[badge-support-linux-arm]: http://img.shields.io/badge/support-[LinuxArm]-2D3F6C.svg?style=flat

[url-latest-release]: https://github.com/05nelsonm/encoding/releases/latest
[url-license]: https://www.apache.org/licenses/LICENSE-2.0.txt
[url-kotlin]: https://kotlinlang.org
[url-immutable]: https://github.com/05nelsonm/immutable
