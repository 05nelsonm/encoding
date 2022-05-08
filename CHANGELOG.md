# CHANGELOG

## Version 1.1.1 (2022-05-08)
 - Updates Kotlin-Components
     - Bumps Kotlin `1.6.10` -> `1.6.21`

## Version 1.1.0 (2022-01-14)
 - Bumps dependencies
 - Enables Kotlin 1.6's new memory model

## Version 1.0.3 (2021-12-09)
 - Removes unnecessary Android target
 - JVM target compile to JavaVersion 1.8 in stead of 11

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
