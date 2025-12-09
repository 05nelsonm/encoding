# Module base16

Base16 (a.k.a. "hex") encoding/decoding in accordance with [RFC 4648 section 8][url-rfc]

```kotlin
val base16 = Base16.Builder {
    isLenient(enable = true)
    lineBreak(interval = 64)
    lineBreakReset(onFlush = true)
    encodeLowercase(enable = true)
    backFillBuffers(enable = true)
}

val text = "Hello World!"
val bytes = text.encodeToByteArray()
val encoded = bytes.encodeToString(base16)
println(encoded) // 48656c6c6f20576f726c6421

// Alternatively, use the static implementation containing
// pre-configured settings, instead of creating your own.
val decoded = encoded.decodeToByteArray(Base16).decodeToString()
assertEquals(text, decoded)
```

[url-rfc]: https://www.ietf.org/rfc/rfc4648.html#section-8
