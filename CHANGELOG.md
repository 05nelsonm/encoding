# CHANGELOG

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
