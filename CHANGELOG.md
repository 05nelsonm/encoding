# CHANGELOG

## Version 2.2.1 (2024-03-13)
 - Adds support for `wasmJs.browser` [[#144]][144]

## Version 2.2.0 (2024-03-10)
 - Updates dependencies
     - Kotlin `1.9.21` -> `1.9.23`
 - Adds support for Java9 JPMS via Multi-Release jars [[#139]][139]
 - Adds experimental support for the following targets [[#140]][140]:
     - `wasmJs`
     - `wasmWasi`
 - Deprecates `InternalEncodingApi` annotation [[#142]][142]

## Version 2.1.0 (2023-11-30)
 - Updates dependencies
     - Kotlin `1.8.21` -> `1.9.21`
 - Drops support for the following deprecated targets:
     - `iosArm32`
     - `watchosX86`
     - `linuxArm32Hfp`
     - `linuxMips32`
     - `linuxMipsel32`
     - `mingwX86`
     - `wasm32`

## Version 2.0.0 (2023-06-21)
 - Fixes JPMS split package exception [[#126]][126] & [[#127]][127]
     - **API BREAKING CHANGES**
     - See the [MIGRATION][MIGRATION] guide for more details
 - Updates the `Maven Central` group & artifact ids
     - **NEW:**
         - `io.matthewnelson.encoding:bom:2.0.0`
         - `io.matthewnelson.encoding:core:2.0.0`
         - `io.matthewnelson.encoding:base16:2.0.0`
         - `io.matthewnelson.encoding:base32:2.0.0`
         - `io.matthewnelson.encoding:base64:2.0.0`
     - **OLD:**
         - `io.matthewnelson.kotlin-components:encoding-bom:1.2.3`
         - `io.matthewnelson.kotlin-components:encoding-core:1.2.3`
         - `io.matthewnelson.kotlin-components:encoding-base16:1.2.3`
         - `io.matthewnelson.kotlin-components:encoding-base32:1.2.3`
         - `io.matthewnelson.kotlin-components:encoding-base64:1.2.3`
 - Promotes `EncoderDecoder.Feed`s to stable by removing the
   `ExperimentalEncodingApi` annotation [[#130]][130]
 - In the event of a decoding failure, the underlying `ByteArray` is now
   cleared prior to throwing the exception [[#132]][132]

## Version 1.2.3 (2023-06-21)
 - Deprecates `...encoding.builders` package path classes/functions for
   `encoding-base16`, `encoding-base32`, `encoding-base64` modules
   and re-introduces them at new package locations
     - `...encoding.base16.Builders.kt`
     - `...encoding.base32.Builders.kt`
     - `...encoding.base64.Builders.kt`
 - This is attributed to issue [[#124]][124] whereby JPMS does not allowing
   split packages.
 - A follow-up release of `2.0.0` with the API breaking changes will be had.
   This release is primarily for consumers to migrate as gracefully as possible.
 - See the [MIGRATION][MIGRATION] guide for more details.

## Version 1.2.2 (2023-06-03)
 - Build improvements [[#106]][106]
     - Removes `kotln-components` submodule
     - Composite builds via `gradle-kmp-configuration-plugin`
     - Adds a Bill of Materials (BOM)
 - Updates dependencies
     - Kotlin `1.8.0` -> `1.8.21`
 - Fixes dropped decoded bytes not being back-filled on resize [[#112]][112]
 - Adds static instances with "reasonable" default configurations [[#122]][122]
 - Exposes `LineBreakOutFeed` utility class [[#113]][113] && [[#118]][118]
 - Adds ability to `flush` an `EncoderDecoder.Feed` [[#114]][114]

## Version 1.2.1 (2023-01-31)
 - Fixes `Base32` encoding
 - Updates `kotlin-components` submodule
     - Publication support updates

## Version 1.2.0 (2023-01-27)
 - Fixes `base16` and `base32` decoding case sensitivity.
     - Previously, encoded data had to be all `uppercase` for decoding to work. This 
       was **not** compliant with RFC 4648 which specifies those n-encodings should 
       be `case-insensitive` (should be able to decode upper &/or lowercase).
 - Adds `binary-compatibility-validator` gradle plugin to track API changes.
 - Adds CI build + caching to project.
 - Changes project & repository name from `component-encoding` -> `encoding`.
 - Downgrades the KotlinJvm & Java `compile`/`target` `sourceCompatibility` versions 
   from 11 to 8.
      - There was absolutely no need to require Java 11.
      - As this is a downgrade, inline functions "should" still work since 
        library consumers had to have been using Java 11+ to use them anyway.
 - Moves all library modules from the root project directory to the `library` directory.
 - Introduces the `encoding-core` module
     - Migrates all core/common functionality of `encoding-base16`, `encoding-base32`, 
       `encoding-base64` to `encoding-core`.
     - Abstractions for easily creating `EncoderDecoder`(s) not already implemented by
       this library.
         - Adds the `Encoder` & `Decoder` sealed classes.
         - Adds the `EncoderDecoder` abstract class (to expose `Encoder` and `Decoder` 
           to library consumers).
         - Adds the `EncoderDecoder.Config` abstract class to specify configuration 
           options for `EncoderDecoder` implementors.
         - Adds the `Encoder.Feed` & `Decoder.Feed` classes for "streaming" 
           encoded/decoded data.
         - Adds 2 new exceptions; `EncodingException` & `EncodingSizeException`.
         - Adds the `FeedBuffer` utility class for use in `EncoderDecoder` implementations.
      - Adds builder classes & functions for easily configuring `base16`, `base32` & 
        `base64` `EncoderDecoder`(s).
      - Refer to [README.md#usage](https://github.com/05nelsonm/encoding#usage) for a quick 
        rundown of new functionality.
 - Deprecates all old extension functions
     - `ReplaceWith` setup to easily switch over to use new `EncoderDecoder`(s) for
       `base16`, `base32` & `base64` library consumers.

## Version 1.1.5 (2023-01-09)
 - Updates `kotlin-components` submodule
     - Kotlin `1.7.20` -> `1.8.0`
     - Support new target:
         - `watchosDeviceArm64`

## Version 1.1.4 (2023-01-07)
 - Updates `kotlin-components` submodule
     - Kotlin `1.6.21` -> `1.7.20`
     - Support new targets:
         - `androidNativeArm32`
         - `androidNativeArm64`
         - `androidNativeX64`
         - `androidNativeX86`
         - `linuxArm64`
         - `wasm32`

## Version 1.1.3 (2022-06-24)
 - Re-enable compiler flag `enableCompatibilityMetadataVariant=true` to support
   non-hierarchical projects. (sorry...)

## Version 1.1.2 (2022-05-14)
 - Updates `kotlin-components` submodule
     - Support new targets:
         - `iosArm32`
         - `iosSimulatorArm64`
         - `tvosSimulatorArm64`
         - `watchosx86`
         - `watchosSimulatorArm64`

## Version 1.1.1 (2022-05-08)
 - Updates `kotlin-components` submodule
     - Kotlin `1.6.10` -> `1.6.21`

## Version 1.1.0 (2022-01-14)
 - Updates `kotlin-components` submodule
     - Kotlin `1.5.31` -> `1.6.10`
 - Enables Kotlin 1.6's new memory model

## Version 1.0.3 (2021-12-09)
 - Removes unnecessary Android target
 - JVM target compile to JavaVersion 1.8 instead of 11

## Version 1.0.2 (2021-11-15)
 - Drops kotlin gradle plugin version down to 1.5.31 for source
   compatibility.
     - Will update to 1.6.0 when atomicfu & coroutines are released for it.

## Version 1.0.1 (2021-11-13)
 - Add base 16 module
 - Fixes potential Kotlin/Native freezing issue
 - Fixes decoder behavior so that decoding an empty String now returns an
   empty ByteArray (instead of null).
 - Bumps kotlin gradle version to 1.6.0-RC2

## Version 1.0.0 (2021-10-30)
 - Initial Release

[106]: https://github.com/05nelsonm/encoding/pull/106
[112]: https://github.com/05nelsonm/encoding/pull/112
[113]: https://github.com/05nelsonm/encoding/pull/113
[114]: https://github.com/05nelsonm/encoding/pull/114
[118]: https://github.com/05nelsonm/encoding/pull/118
[122]: https://github.com/05nelsonm/encoding/pull/122
[124]: https://github.com/05nelsonm/encoding/issues/124
[126]: https://github.com/05nelsonm/encoding/pull/126
[127]: https://github.com/05nelsonm/encoding/pull/127
[130]: https://github.com/05nelsonm/encoding/pull/130
[132]: https://github.com/05nelsonm/encoding/pull/132
[139]: https://github.com/05nelsonm/encoding/pull/139
[140]: https://github.com/05nelsonm/encoding/pull/140
[142]: https://github.com/05nelsonm/encoding/pull/142
[144]: https://github.com/05nelsonm/encoding/pull/144
[MIGRATION]: https://github.com/05nelsonm/encoding/blob/master/MIGRATION.md
