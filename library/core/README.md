# Module core

Core primitives and abstractions for encoding/decoding operations, serving as the foundation for 
higher level implementations.

```kotlin
// Using modules :base64 and :utf8 for example purposes
// Using io.matthewnelson.kmp-file:{file/async} for example purposes

fun main() {
    //// Encoder.Feed example ////

    // The medium to put encoded data (could be a buffer, file, etc.)
    val sb = StringBuilder()

    // Define the callback for where to dump encoded characters as they
    // come out of the Encoder.Feed
    //
    // Alternatively, use Encoder.OutFeed(sb::append)
    val out = Encoder.OutFeed { char -> sb.append(char) }

    // Wrap it in helper LineBreakOutFeed which will output `\n` every `interval`
    // characters of output.
    val outN = LineBreakOutFeed(interval = 64, resetOnFlush = false, out)

    Base64.Default.newEncoderFeed(outN).use { feed ->
        // Encode UTF-8 bytes to base64
        "Hello World 1!".decodeToByteArray(UTF8).forEach(feed::consume)
        feed.flush() // Finalize first encoding to reuse the Feed
        outN.output('.') // Add a separator or something.
        "Hello World 2!".decodeToByteArray(UTF8).forEach(feed::consume)
    } // << `Feed.use` extension function will call Feed.doFinal automatically

    val encoded = sb.toString()
    println(encoded) // SGVsbG8gV29ybGQgMSE=.SGVsbG8gV29ybGQgMiE=

    //// Decoder.Feed example ////
    val decoded = StringBuilder()

    UTF8.newEncoderFeed(decoded::append).use { feedUTF8 ->

        // As the base64 decoder outputs decoded bytes, pipe them through
        // the UTF8 "encoder" feed (i.e. UTF-8 byte to text transform),
        // which will then pipe each "encoded" character of output to
        // the StringBuilder.
        Base64.Default.newDecoderFeed(feedUTF8::consume).use { feedB64 ->
            encoded.substringBefore('.').forEach(feedB64::consume)
            feedB64.flush() // Finalize first decoding to reuse the Feed
            feedUTF8.flush() // Prepare UTF8 feed for second decoding
            encoded.substringAfter('.').forEach(feedB64::consume)
        } // << `Feed.use` extension function will call Feed.doFinal automatically
    } // << `Feed.use` extension function will call Feed.doFinal automatically

    println(decoded.toString()) // Hello World 1!Hello World 2!

    //// Decoder decodeBuffered/decodeBufferedAsync examples ///

    // Write UTF-8 encoded bytes to a FileStream (kmp-file:file)
    val file = "/path/to/file.txt".toFile()
    file.openWrite(excl = null).use { stream ->
        decoded.decodeBuffered(
            decoder = UTF8,
            throwOnOverflow = false,
            action = stream::write,
        )
    }

    // Now do it asynchronously (kmp-file:async)
    GlobalScope.launch {
        AsyncFs.Default.with {
            file.openAppendAsync(excl = OpenExcl.MustExist)
                .useAsync { stream ->
                    decoded.decodeBufferedAsync(
                        decoder = UTF8.ThrowOnInvalid,
                        throwOnOverflow = false,
                        maxBufSize = 1024,
                        action = stream::writeAsync,
                    )
                }
        }
    }
}
```
