# component-encoding

**Base32**
 - [Crockford](https://www.crockford.com/base32.html)
 - [Default (Rfc 4648 section 6)](https://www.ietf.org/rfc/rfc4648.html#section-6)
 - [Hex (Rfc 4648 section 7)](https://www.ietf.org/rfc/rfc4648.html#section-7)

**Base64**
 - [Default (Rfc 4648 section 4)](https://www.ietf.org/rfc/rfc4648.html#section-4)
 - [Url Safe (Rfc 4648 section 5)](https://www.ietf.org/rfc/rfc4648.html#section-5)

A full list of `kotlin-components` projects can be found [HERE](https://kotlin-components.matthewnelson.io)

### Get Started

```kotlin
// build.gradle.kts

dependencies {
    val encoding = "1.0.0"
    implementation("io.matthewnelson.kotlin-components:encoding-base32:$encoding")
    implementation("io.matthewnelson.kotlin-components:encoding-base64:$encoding")
}
```

```groovy
// build.gradle

dependencies {
    def encoding = "1.0.0"
    implementation "io.matthewnelson.kotlin-components:encoding-base32:$encoding"
    implementation "io.matthewnelson.kotlin-components:encoding-base64:$encoding"
}
```

### Git

This project utilizes git submodules. You will need to initialize them when
cloning the repository via:

```bash
$ git clone --recursive https://github.com/05nelsonm/component-request.git
```

If you've already cloned the repository, run:
```bash
$ git checkout master
$ git pull
$ git submodule update --init
```

In order to keep submodules updated when pulling the latest code, run:
```bash
$ git pull --recurse-submodules
```
