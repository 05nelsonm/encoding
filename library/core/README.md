# Module core

Core primitives and abstractions for encoding/decoding operations, serving as the foundation for 
higher level implementations.

```kotlin
// Using modules :base64 and :utf8 for example purposes

fun main() {
    //// Encoder.Feed example ////

    // The medium to put encoded data (could be a buffer, file, etc.)
    val sb = StringBuilder()

    // Define the callback for where to dump encoded characters as they
    // come out of the Encoder.Feed
    val out = Encoder.OutFeed { char -> sb.append(char) }

    // Wrap it in helper LineBreakOutFeed which will output \n every `interval`
    // characters of output.
    val outN = LineBreakOutFeed(interval = 64, out)

    Base64.Default.newEncoderFeed(outN).use { feed ->
        "Hello World 1!".decodeToByteArray(UTF8).forEach { b -> feed.consume(b) }
        feed.flush() // Finalize first encoding to reuse the Feed
        outN.output('.')
        "Hello World 2!".decodeToByteArray(UTF8).forEach { b -> feed.consume(b) }
    } // << `Feed.use` extension function will call Feed.doFinal automatically for us here

    val encoded = sb.toString()
    println(encoded) // SGVsbG8gV29ybGQgMSE=.SGVsbG8gV29ybGQgMiE=

    //// Decoder.Feed example ////
    val decoded = StringBuilder()

    UTF8.newEncoderFeed { char -> decoded.append(char) }.use { feedUTF8 ->
        Base64.Default.newDecoderFeed { decodedByte -> feedUTF8.consume(decodedByte) }.use { feedB64 ->
            encoded.substringBefore('.').forEach { c -> feedB64.consume(c) }
            feedB64.flush() // Finalize first decoding to reuse the Feed
            feedUTF8.flush() // Prepare UTF8 feed for second decoding
            encoded.substringAfter('.').forEach { c -> feedB64.consume(c) }
        } // << `Feed.use` extension function will call Feed.doFinal automatically for us here
    } // << `Feed.use` extension function will call Feed.doFinal automatically for us here

    println(decoded.toString()) // Hello World 1!Hello World 2!
}
```
