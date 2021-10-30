import io.matthewnelson.kotlin.components.dependencies.versions
import io.matthewnelson.kotlin.components.kmp.KmpTarget
import io.matthewnelson.kotlin.components.kmp.publish.kmpPublishRootProjectConfiguration
import org.jetbrains.kotlin.gradle.plugin.KotlinJsCompilerType

plugins {
    id("kmp-configuration")
    id("kmp-publish")
}

kmpPublishRootProjectConfiguration?.let { config ->
    repositories {
        if (config.versionName.endsWith("-SNAPSHOT")) {
            maven("https://oss.sonatype.org/content/repositories/snapshots/")
        } else {
            maven("https://oss.sonatype.org/content/groups/staging") {
                credentials {
                    username = ext.get("mavenCentralUsername").toString()
                    password = ext.get("mavenCentralPassword").toString()
                }
            }
        }
    }
}

kmpConfiguration {
    setupMultiplatform(
        setOf(

            KmpTarget.Jvm.Jvm.DEFAULT,

            KmpTarget.Jvm.Android(
                buildTools = versions.android.buildTools,
                compileSdk = versions.android.sdkCompile,
                minSdk = versions.android.sdkMin16,
            ),

            KmpTarget.NonJvm.JS(
                compilerType = KotlinJsCompilerType.BOTH,
                browser = KmpTarget.NonJvm.JS.Browser(
                    jsBrowserDsl = null
                ),
                node = KmpTarget.NonJvm.JS.Node(
                    jsNodeDsl = null
                ),
                mainSourceSet = null,
                testSourceSet = null,
            ),

            KmpTarget.NonJvm.Native.Unix.Darwin.Ios.Arm32.DEFAULT,
            KmpTarget.NonJvm.Native.Unix.Darwin.Ios.Arm64.DEFAULT,
            KmpTarget.NonJvm.Native.Unix.Darwin.Ios.X64.DEFAULT,
            KmpTarget.NonJvm.Native.Unix.Darwin.Ios.SimulatorArm64.DEFAULT,
            KmpTarget.NonJvm.Native.Unix.Darwin.Macos.Arm64.DEFAULT,
            KmpTarget.NonJvm.Native.Unix.Darwin.Macos.X64.DEFAULT,
            KmpTarget.NonJvm.Native.Unix.Darwin.Tvos.Arm64.DEFAULT,
            KmpTarget.NonJvm.Native.Unix.Darwin.Tvos.X64.DEFAULT,
            KmpTarget.NonJvm.Native.Unix.Darwin.Tvos.SimulatorArm64.DEFAULT,
            KmpTarget.NonJvm.Native.Unix.Darwin.Watchos.Arm32.DEFAULT,
            KmpTarget.NonJvm.Native.Unix.Darwin.Watchos.Arm64.DEFAULT,
            KmpTarget.NonJvm.Native.Unix.Darwin.Watchos.X64.DEFAULT,
            KmpTarget.NonJvm.Native.Unix.Darwin.Watchos.X86.DEFAULT,
            KmpTarget.NonJvm.Native.Unix.Darwin.Watchos.SimulatorArm64.DEFAULT,
            KmpTarget.NonJvm.Native.Unix.Linux.Arm32Hfp.DEFAULT,
            KmpTarget.NonJvm.Native.Unix.Linux.Mips32.DEFAULT,
            KmpTarget.NonJvm.Native.Unix.Linux.Mipsel32.DEFAULT,
            KmpTarget.NonJvm.Native.Unix.Linux.X64.DEFAULT,
            KmpTarget.NonJvm.Native.Mingw.X64.DEFAULT,
            KmpTarget.NonJvm.Native.Mingw.X86.DEFAULT,
        ),
        commonMainSourceSet = {
            project.kmpPublishRootProjectConfiguration?.let { config ->
                dependencies {
                    implementation("${config.group}:encoding-base32:${config.versionName}")
                    implementation("${config.group}:encoding-base64:${config.versionName}")
                }
            }
        },
    )
}
