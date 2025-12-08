# encoding
[![badge-license]][url-license]
[![badge-latest]][url-latest]

[![badge-kotlin]][url-kotlin]

![badge-platform-android]
![badge-platform-jvm]
![badge-platform-js]
![badge-platform-js-node]
![badge-platform-wasm]
![badge-platform-wasi]
![badge-platform-linux]
![badge-platform-macos]
![badge-platform-ios]
![badge-platform-tvos]
![badge-platform-watchos]
![badge-platform-windows]
![badge-platform-android-native]

Configurable, streamable, efficient and extensible encoding/decoding for Kotlin Multiplatform.

### Modules

 - [core](library/core/README.md)
 - [base16](library/base16/README.md)
 - [base32](library/base32/README.md)
 - [base64](library/base64/README.md)
 - [utf8](library/utf8/README.md)

### API Docs

 - [https://encoding.matthewnelson.io][url-docs]

### Sample

 - [README](sample/README.md)

### Get Started

<!-- TAG_VERSION -->
<!-- TODO: Add utf8 -->

```kotlin
// build.gradle.kts
dependencies {
    val encoding = "2.5.0"
    implementation("io.matthewnelson.encoding:base16:$encoding")
    implementation("io.matthewnelson.encoding:base32:$encoding")
    implementation("io.matthewnelson.encoding:base64:$encoding")

    // Only necessary if you just want the abstractions to create your own EncoderDecoder(s)
    implementation("io.matthewnelson.encoding:core:$encoding")
}
```

<!-- TAG_VERSION -->
<!-- TODO: Add utf8 -->
Alternatively, you can use the BOM.

```kotlin
// build.gradle.kts
dependencies {
    // define the BOM and its version
    implementation(project.dependencies.platform("io.matthewnelson.encoding:bom:2.5.0"))

    // define artifacts without version
    implementation("io.matthewnelson.encoding:base16")
    implementation("io.matthewnelson.encoding:base32")
    implementation("io.matthewnelson.encoding:base64")

    // Only necessary if you just want the abstractions to create your own EncoderDecoder(s)
    implementation("io.matthewnelson.encoding:core")
}
```

<!-- TAG_VERSION -->
[badge-latest]: https://img.shields.io/badge/latest--release-2.5.0-blue.svg?style=flat
[badge-license]: https://img.shields.io/badge/license-Apache%20License%202.0-blue.svg?style=flat

<!-- TAG_DEPENDENCIES -->
[badge-kotlin]: https://img.shields.io/badge/kotlin-2.2.20-blue.svg?logo=kotlin

<!-- TAG_PLATFORMS -->
[badge-platform-android]: http://img.shields.io/badge/-android-6EDB8D.svg?style=flat
[badge-platform-jvm]: http://img.shields.io/badge/-jvm-DB413D.svg?style=flat
[badge-platform-js]: http://img.shields.io/badge/-js-F8DB5D.svg?style=flat
[badge-platform-js-node]: https://img.shields.io/badge/-nodejs-68a063.svg?style=flat
[badge-platform-wasm]: https://img.shields.io/badge/-wasm-624FE8.svg?style=flat
[badge-platform-wasi]: https://img.shields.io/badge/-wasi-18a033.svg?style=flat
[badge-platform-android-native]: http://img.shields.io/badge/-android--native-6EDB8D.svg?style=flat
[badge-platform-linux]: http://img.shields.io/badge/-linux-2D3F6C.svg?style=flat
[badge-platform-macos]: http://img.shields.io/badge/-macos-111111.svg?style=flat
[badge-platform-ios]: http://img.shields.io/badge/-ios-CDCDCD.svg?style=flat
[badge-platform-tvos]: http://img.shields.io/badge/-tvos-808080.svg?style=flat
[badge-platform-watchos]: http://img.shields.io/badge/-watchos-C0C0C0.svg?style=flat
[badge-platform-windows]: http://img.shields.io/badge/-windows-4D76CD.svg?style=flat

[url-docs]: https://encoding.matthewnelson.io
[url-kotlin]: https://kotlinlang.org
[url-latest]: https://github.com/05nelsonm/encoding/releases/latest
[url-license]: https://www.apache.org/licenses/LICENSE-2.0.txt
