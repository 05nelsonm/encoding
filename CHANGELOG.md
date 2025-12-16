# CHANGELOG

## Version 2.6.0 (2025-12-16)
 - Update dependencies [[#206]][206]:
     - Kotlin `2.2.20` -> `2.2.21`
 - Fixes `Base32.Crockford` check symbol validation when `Feed.doFinal` is called [[#179]][179]
 - Fixes `EncoderDecoder` and `EncoderDecoder.Config` equals/hashCode overrides [[#180]][180]
 - Fixes 64-bit integer overflow for `Base32` and `Base64` implementations of 
   `Config.decodeOutMaxSizeProtected` [[#183]][183]
 - Fixes `LineBreakOutFeed` not being reset as intended when `Encoder.Feed.flush` is 
   called [[#193]][193]
 - Fixes `Encoder.Feed` and `Decoder.Feed` implementations holding onto their respective 
   `OutFeed` callback references after `close` has been called [[#194]][194]
 - Fixes `Encoder.Companion.encodeToString` back-fill implementation of its temporary 
   `StringBuilder` buffer [[#220]][220]
     - Adds the `StringBuilder.wipe` extension functions
 - Aligns `Base16`, `Base32`, and `Base64` builder implementations' syntax/layout [[#176]][176]
     - Deprecates all old builders and replaces them with:
         - `Base16.Builder`
         - `Base32.Crockford.Builder`
         - `Base32.Default.Builder`
         - `Base32.Hex.Builder`
         - `Base64.Builder`
 - Deprecates `Base16`, `Base32.Crockford`, `Base32.Default`, `Base32.Hex`, and `Base64` 
   public constructors [[#177]][177]
 - Increases all `DeprecationLevel.WARNING` to `DeprecationLevel.ERROR` [[#178]][178]
 - Moves `Feed.use` extension function's `finally` logic to internal function [[#189]][189]
 - Renames `EncoderDecoder.Config.encodeOutSize` to `encodeOutMaxSize` [[#196]][196]
     - Deprecates `encodeOutSize`
 - Adds ability to configure temporary buffer back-fill behavior when using `:core` 
   module encoding/decoding extension functions [[#190]][190]
 - Adds `EncoderDecoder.Config.maxDecodeEmit` constructor parameter [[#208]][208] [[#215]][215]
 - Adds `Decoder.Companion.decodeBuffered` and `Decoder.Companion.decodeBufferedAsync` extension 
   functions [[#210]][210] [[#226]][226] [[#228]][228] [[#235]][235]
 - Adds ability to configure `LineBreakOutFeed` reset behavior when `Encoder.Feed.flush`
   is called [[#216]][216]
     - Deprecates `LineBreakOutFeed` constructor and adds new `resetOnFlush` parameter
     - Adds `EncoderDecoder.Config.lineBreakResetOnFlush` constructor parameter
     - Adds to `EncoderDecoder` implementations' `Builder` the `lineBreakReset` function to configure
 - Adds `UTF-8` encoding/decoding [[#170]][170] [[#185]][185] [[#200]][200] [[#201]][201] [[#202]][202] [[#203]][203]
     - Module `:utf8` contains the `UTF8` implementation of `EncoderDecoder`
 - Adds the `MalformedEncodingException` type [[#217]][217]
 - Removes checked exception `@Throws` annotation for `EncodingException` from 
   `Encoder.Companion.encodeToString` and `Encoder.Companion.encodeToCharArray` extension 
   functions [[#218]][218]
 - Adds `EncoderDecoder.Config.maxEncodeEmit` constructor parameter [[#223]][223]
 - Adds `Encoder.Companion.encodeBuffered` and `Encoder.Companion.encodeBufferedAsync` extension
   functions [[#232]][232]
 - Adds partial array encoding [[#233]][233]
     - All `Encoder.Companion` extension functions can now take additional `offset` and `len` arguments
 - Adds partial text decoding [[#234]][234]
     - All `Decoder.Companion` extension functions can now take additional `offset` and `len` arguments
 - Performance improvements for `DecoderInput` [[#169]][169]
 - Performance improvements for `Base16` [[#186]][186]
 - Performance improvements for `Base32` [[#188]][188] [[#236]][236]
 - Performance improvements for `Base64` [[#187]][187] [[#236]][236] [[#237]][237]

## Version 2.5.0 (2025-09-19)
 - Update dependencies [[#167]][167]:
     - Kotlin `2.1.10` -> `2.2.20`
 - Lower supported `KotlinVersion` to `1.8` [[#168]][168]

## Version 2.4.0 (2025-02-14)
 - Update dependencies [[#166]][166]:
     - Kotlin `1.9.24` -> `2.1.10`

## Version 2.3.1 (2024-12-18)
 - Fixes performance issues introduced in `2.3.0` [[#162]][162]:
     - Deprecates the following which were introduced in `2.3.0`:
         - `CTCase` class
         - `DecoderAction` functional interface
         - `DecoderAction.Parser` class
         - `Base16ConfigBuilder.isconstantTime` variable
         - `Base32CrockfordConfigBuilder.isconstantTime` variable
         - `Base32HexConfigBuilder.isconstantTime` variable
         - `Base64ConfigBuilder.isconstantTime` variable
     - All encoders (Base16/32/64) are now written in a constant-time 
       manner. Performance impact when compared to `2.2.2` was "negligible" (in 
       the neighborhood of ~4% depending on platform), given the implications.
 - Adds benchmarking to repository [[#160]][160]

## Version 2.3.0 (2024-12-15)
 - Fixes `Encoder.Companion` extension function `ByteArray.encodeToString` not zeroing
   out its local `StringBuilder` before returning encoded data. [[#155]][155]
 - Deprecates the following [[#156]][156] [[#158]][158]:
     - `Decoder.Companion` extension function `ByteArray.decodeToByteArray`
     - `Decoder.Companion` extension function `ByteArray.decodeToByteArrayOrNull`
     - `Encoder.Companion` extension function `ByteArray.encodeToByteArray`
     - `DecoderInput` constructor for `ByteArray` input
 - Adds support for constant-time operations when encoding/decoding (off by 
   default). [[#154]][154]
 - Adds `dokka` documentation at `https://encoding.matthewnelson.io` [[#157]][157]

## Version 2.2.2 (2024-08-30)
 - Updates dependencies
     - Kotlin `1.9.23` -> `1.9.24`
 - Fixes multiplatform metadata manifest `unique_name` parameter for
   all source sets to be truly unique. [[#147]][147]
 - Updates jvm `.kotlin_module` with truly unique file name. [[#147]][147]
 - Fixes Java9 JPMS `module-info` to use `transitive` instead of just
   `requires`. [[#150]][150]

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
[147]: https://github.com/05nelsonm/encoding/pull/147
[150]: https://github.com/05nelsonm/encoding/pull/150
[154]: https://github.com/05nelsonm/encoding/pull/154
[155]: https://github.com/05nelsonm/encoding/pull/155
[156]: https://github.com/05nelsonm/encoding/pull/156
[157]: https://github.com/05nelsonm/encoding/pull/157
[158]: https://github.com/05nelsonm/encoding/pull/158
[160]: https://github.com/05nelsonm/encoding/pull/160
[162]: https://github.com/05nelsonm/encoding/pull/162
[166]: https://github.com/05nelsonm/encoding/pull/166
[167]: https://github.com/05nelsonm/encoding/pull/167
[168]: https://github.com/05nelsonm/encoding/pull/168
[169]: https://github.com/05nelsonm/encoding/pull/169
[170]: https://github.com/05nelsonm/encoding/pull/170
[176]: https://github.com/05nelsonm/encoding/pull/176
[177]: https://github.com/05nelsonm/encoding/pull/177
[178]: https://github.com/05nelsonm/encoding/pull/178
[179]: https://github.com/05nelsonm/encoding/pull/179
[180]: https://github.com/05nelsonm/encoding/pull/180
[183]: https://github.com/05nelsonm/encoding/pull/183
[185]: https://github.com/05nelsonm/encoding/pull/185
[186]: https://github.com/05nelsonm/encoding/pull/186
[187]: https://github.com/05nelsonm/encoding/pull/187
[188]: https://github.com/05nelsonm/encoding/pull/188
[189]: https://github.com/05nelsonm/encoding/pull/189
[190]: https://github.com/05nelsonm/encoding/pull/190
[193]: https://github.com/05nelsonm/encoding/pull/193
[194]: https://github.com/05nelsonm/encoding/pull/194
[196]: https://github.com/05nelsonm/encoding/pull/196
[200]: https://github.com/05nelsonm/encoding/pull/200
[201]: https://github.com/05nelsonm/encoding/pull/201
[202]: https://github.com/05nelsonm/encoding/pull/202
[203]: https://github.com/05nelsonm/encoding/pull/203
[206]: https://github.com/05nelsonm/encoding/pull/206
[208]: https://github.com/05nelsonm/encoding/pull/208
[210]: https://github.com/05nelsonm/encoding/pull/210
[215]: https://github.com/05nelsonm/encoding/pull/215
[216]: https://github.com/05nelsonm/encoding/pull/216
[217]: https://github.com/05nelsonm/encoding/pull/217
[218]: https://github.com/05nelsonm/encoding/pull/218
[220]: https://github.com/05nelsonm/encoding/pull/220
[223]: https://github.com/05nelsonm/encoding/pull/223
[226]: https://github.com/05nelsonm/encoding/pull/226
[228]: https://github.com/05nelsonm/encoding/pull/228
[232]: https://github.com/05nelsonm/encoding/pull/232
[233]: https://github.com/05nelsonm/encoding/pull/233
[234]: https://github.com/05nelsonm/encoding/pull/234
[235]: https://github.com/05nelsonm/encoding/pull/235
[236]: https://github.com/05nelsonm/encoding/pull/236
[237]: https://github.com/05nelsonm/encoding/pull/237
[MIGRATION]: https://github.com/05nelsonm/encoding/blob/master/MIGRATION.md
