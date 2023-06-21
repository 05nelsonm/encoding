# MIGRATION

## Preamble

`2.0.0` release contained a breaking API change which broke binary compatibility. 
This was attributed to an issue with Java 9 `Module`s, for which JPMS disallows 
split packages. The `encoding-base16`, `encoding-base32` and `encoding-base64` 
dependencies were the cause of the issue because their builder classes and functions 
were located in the same package named `io.matthewnelson.encoding.builders`. Those 
classes and functions were subsequently deprecated in release `1.2.3` so consumers 
could gracefully update before upgrading to `2.0.0` where they have been removed.

For more details, see [[#124]][124].

## Migration guide for 1.x.x -> 2.0.0

 - Update dependency to `1.2.3`
     - Migration method 1:
         - Use your IDE or editor to search your project for the following
           ```
           import io.matthewnelson.encoding.builders
           ```
         - Replace `.builders` with the new package location (either `base16`, `base32`, or `base64`)
     - Migration method 2:
         - Use the provided `ReplaceWith` functionality of the `@Deprecated` notice 
           to update to the new builder class/function package locations.
 - Update dependency to `2.0.0`

[124]: https://github.com/05nelsonm/encoding/issues/124