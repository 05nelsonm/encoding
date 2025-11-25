# Module utf8

UTF-8 encoding/decoding in accordance with [RFC 3629][url-rfc]

```kotlin
val utf8 = UTF8.Builder {
    replacement(strategy = UTF8.ReplacementStrategy.KOTLIN)
}

val text = "Hello World!"
val utf8Bytes = text.decodeToByteArray(utf8)
println(utf8Bytes.toList()) // [72, 101, 108, 108, 111, 32, 87, 111, 114, 108, 100, 33]

val text2 = utf8Bytes.encodeToString(utf8)
assertEquals(text, text2)
```

[url-rfc]: https://datatracker.ietf.org/doc/html/rfc3629
