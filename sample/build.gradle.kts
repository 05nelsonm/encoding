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
import org.jetbrains.kotlin.gradle.plugin.mpp.KotlinNativeTarget

plugins {
    id("configuration")
}

kmpConfiguration {
    this.configure {
        jvm {
            pluginIds("application")

            target { withJava() }

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

        val X86 = "x86"
        val X64 = "x64"
        val ARM64 = "arm64"

        val arch = when (System.getProperty("os.arch")) {
            X86 -> X86
            "i386" -> X86
            "i486" -> X86
            "i586" -> X86
            "i686" -> X86
            "pentium" -> X86

            X64 -> X64
            "x86_64" -> X64
            "amd64" -> X64
            "em64t" -> X64
            "universal" -> X64

            ARM64 -> ARM64
            "aarch64" -> ARM64
            else -> null
        }

        val os = org.gradle.internal.os.OperatingSystem.current()
        val targetName = "nativeSample"

        when {
            os.isLinux -> {
                when (arch) {
                    ARM64 -> linuxArm64(targetName) { target { setup() } }
                    X64 -> linuxX64(targetName) { target { setup() } }
                }
            }
            os.isMacOsX -> {
                when (arch) {
                    ARM64 -> macosArm64(targetName) { target { setup() } }
                    X64 -> macosX64(targetName) { target { setup() } }
                }
            }
            os.isWindows -> {
                @Suppress("DEPRECATION")
                when (arch) {
                    X64 -> mingwX64(targetName) { target { setup() } }
                    X86 -> mingwX86(targetName) { target { setup() } }
                }
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
                findByName("jvmMain")?.run {
                    extensions.configure<JavaApplication>("application") {
                        mainClass.set("MainKt")
                    }
                }
            }
        }
    }
}
