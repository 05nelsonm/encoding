# benchmarks

Benchmarks for tracking performance of `encoding` implementation.

**NOTE:** Benchmarking is run on every Pull Request. Results can be viewed for each 
workflow run on the [GitHub Actions][url-actions] tab of the repository.

- Run All platforms:
  ```shell
  ./gradlew benchmark
  ```

- Run Jvm:
  ```shell
  ./gradlew jvmBenchmark
  ```

- Run WasmJs:
  ```shell
  ./gradlew wasmJsBenchmark
  ```

- Run Native:
  ```shell
  ./gradlew nativeHostBenchmark
  ```

[url-actions]: https://github.com/05nelsonm/encoding/actions/
