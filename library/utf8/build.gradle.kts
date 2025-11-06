/*
 * Copyright (c) 2025 Matthew Nelson
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *         https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 **/
plugins {
    id("configuration")
//    id("bom-include")
}

kmpConfiguration {
    // TODO:
    //  - Set publish = true
    //  - Uncomment bom-include plugin
    //  - Remove explicitApi definition
    //  - Update tools/check-publication/build.gradle.kts
    //  - Update root README.md
    configureShared(java9ModuleName = "io.matthewnelson.encoding.utf8", publish = false) {
        common {
            sourceSetMain {
                dependencies {
                    api(project(":library:core"))
                }
            }
            sourceSetTest {
                dependencies {
                    implementation(project(":library:base16"))
                    implementation(project(":library:test"))
                }
            }
        }

        kotlin { explicitApi() }
    }
}
