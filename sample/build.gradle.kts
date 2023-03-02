import org.jetbrains.kotlin.gradle.plugin.mpp.KotlinNativeTarget

/*
 * Copyright (c) 2023 Matthew Nelson
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 **/
@Suppress("DSL_SCOPE_VIOLATION")
plugins {
    alias(libs.plugins.configuration)
}

kmpConfiguration {
    configure {
        jvm {
            pluginIds("application")

            target {
                withJava()
            }

            kotlinJvmTarget = JavaVersion.VERSION_1_8
            compileSourceCompatibility = JavaVersion.VERSION_1_8
            compileTargetCompatibility = JavaVersion.VERSION_1_8
        }

        fun KotlinNativeTarget.setup() {
            binaries {
                executable {
                    entryPoint = "main"
                }
            }
        }

        val osName = System.getProperty("os.name")
        when {
            osName.startsWith("Windows", true) -> {
                mingwX64("nativeSample") { target { setup() } }
            }
            osName == "Mac OS X" -> {
                macosX64("nativeSample") { target { setup() } }
            }
            osName.contains("Mac", true) -> {
                macosArm64("nativeSample") { target { setup() } }
            }
            osName == "Linux" -> {
                linuxX64("nativeSample") { target { setup() } }
            }
        }

        common {
            sourceSetMain {
                dependencies {
                    implementation(project(":library:encoding-base16"))
                    implementation(project(":library:encoding-base32"))
                    implementation(project(":library:encoding-base64"))
                }
            }
        }

        kotlin {
            sourceSets {
                findByName("jvmMain")?.let {
                    extensions.configure<JavaApplication>("application") {
                        mainClass.set("MainKt")
                    }
                }
            }
        }
    }
}
