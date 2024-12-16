# benchmarks

Benchmarks for tracking performance of `encoding` implementation.

- Run All platforms:
  ```shell
  ./gradlew benchmark
  ```

- Run Jvm:
  ```shell
  ./gradlew benchmark -PKMP_TARGETS="JVM"
  ```

- Run Js:
  ```shell
  ./gradlew benchmark -PKMP_TARGETS="JS"
  ```

- Run WasmJs:
  ```shell
  ./gradlew benchmark -PKMP_TARGETS="WASM_JS"
  ```

- Run Native:
  ```shell
  ./gradlew benchmark -PKMP_TARGETS="LINUX_ARM64,LINUX_X64,MACOS_ARM64,MACOS_X64,MINGW_X64"
  ```
