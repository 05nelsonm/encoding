# Module base64

Base64 encoding/decoding in accordance with [RFC 4648 section 4][url-rfc-4], [RFC 4648 section 5][url-rfc-5]

```kotlin
val base64 = Base64.Builder {
    isLenient(enable = true)
    lineBreak(interval = 64)
    encodeUrlSafe(enable = false)
    padEncoded(enable = true)
}

val text = "Hello World!"
val bytes = text.encodeToByteArray()
val encoded = bytes.encodeToString(base64)
println(encoded) // SGVsbG8gV29ybGQh

// Alternatively, use the static implementation containing
// pre-configured settings, instead of creating your own.
var decoded = encoded.decodeToByteArray(Base64.Default).decodeToString()
assertEquals(text, decoded)
decoded = encoded.decodeToByteArray(Base64.UrlSafe).decodeToString()
assertEquals(text, decoded)
```

[url-rfc-4]: https://www.ietf.org/rfc/rfc4648.html#section-4
[url-rfc-5]: https://www.ietf.org/rfc/rfc4648.html#section-5
