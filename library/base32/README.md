# Module base32

Base32 encoding/decoding in accordance with [Crockford][url-crockford], [RFC 4648 section 6][url-rfc-6], [RFC 4648 section 7][url-rfc-7]

```kotlin
val crockford = Base32.Crockford.Builder {
    isLenient(enable = true)
    encodeLowercase(enable = false)
    hyphen(interval = 5)
    check(symbol = '~')
}

val text = "Hello World!"
val bytes = text.encodeToByteArray()
val encoded = bytes.encodeToString(crockford)
println(encoded) // 91JPR-V3F41-BPYWK-CCGGG~

// Alternatively, use the static implementation containing
// pre-configured settings, instead of creating your own.
val decoded = encoded.decodeToByteArray(Base32.Crockford).decodeToString()
assertEquals(text, decoded)
```

```kotlin
val default = Base32.Default.Builder {
    isLenient(enable = true)
    lineBreak(interval = 64)
    encodeLowercase(enable = false)
    padEncoded(enable = true)
}

val text = "Hello World!"
val bytes = text.encodeToByteArray()
val encoded = bytes.encodeToString(default)
println(encoded) // JBSWY3DPEBLW64TMMQQQ====

// Alternatively, use the static implementation containing
// pre-configured settings, instead of creating your own.
val decoded = encoded.decodeToByteArray(Base32.Default).decodeToString()
assertEquals(text, decoded)
```

```kotlin
val hex = Base32.Hex.Builder {
    isLenient(enable = true)
    lineBreak(interval = 64)
    encodeLowercase(enable = false)
    padEncoded(enable = true)
}

val text = "Hello World!"
val bytes = text.encodeToByteArray()
val encoded = bytes.encodeToString(hex)
println(encoded) // 91IMOR3F41BMUSJCCGGG====

// Alternatively, use the static implementation containing
// pre-configured settings, instead of creating your own.
val decoded = encoded.decodeToByteArray(Base32.Hex).decodeToString()
assertEquals(text, decoded)
```

[url-crockford]: https://www.crockford.com/base32.html
[url-rfc-6]: https://www.ietf.org/rfc/rfc4648.html#section-6
[url-rfc-7]: https://www.ietf.org/rfc/rfc4648.html#section-7
