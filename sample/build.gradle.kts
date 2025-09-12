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
import io.matthewnelson.kmp.configuration.extension.container.target.KmpTarget
import org.jetbrains.kotlin.gradle.ExperimentalKotlinGradlePluginApi
import org.jetbrains.kotlin.gradle.plugin.mpp.KotlinNativeTarget
import org.jetbrains.kotlin.konan.target.HostManager
import org.jetbrains.kotlin.konan.target.KonanTarget

plugins {
    id("configuration")
}

kmpConfiguration {
    configure {
        jvm {
            target {
                @OptIn(ExperimentalKotlinGradlePluginApi::class)
                mainRun {
                    mainClass.set("MainKt")
                }
            }
        }

        fun <T: KotlinNativeTarget> KmpTarget<T>.setup() {
            target { binaries { executable { entryPoint = "main" } } }
        }

        val targetName = "nativeHost"

        when (HostManager.host) {
            is KonanTarget.LINUX_X64 -> linuxX64(targetName) { setup() }
            is KonanTarget.LINUX_ARM64 -> linuxArm64(targetName) { setup() }
            is KonanTarget.MACOS_X64 -> macosX64(targetName) { setup() }
            is KonanTarget.MACOS_ARM64 -> macosArm64(targetName) { setup() }
            is KonanTarget.MINGW_X64 -> mingwX64(targetName) { setup() }
            else -> {}
        }

        common {
            sourceSetMain {
                dependencies {
                    implementation(project(":library:base16"))
                    implementation(project(":library:base32"))
                    implementation(project(":library:base64"))
                }
            }
        }
    }
}
