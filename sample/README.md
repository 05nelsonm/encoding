# Encoding Sample App

Simple Application that will output encoded values of `Hello World!`

### Running

Java:
```shell
./gradlew :sample:jvmRun -PKMP_TARGETS="JVM"
```

Native:
```shell
./gradlew :sample:runDebugExecutableNativeSample -PKMP_TARGETS="LINUX_ARM64,LINUX_X64,MACOS_ARM64,MACOS_X64,MINGW_X64,MINGW_X86"
```
