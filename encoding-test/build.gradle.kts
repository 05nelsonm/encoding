import io.matthewnelson.kotlin.components.kmp.KmpTarget
import org.jetbrains.kotlin.gradle.plugin.KotlinJsCompilerType

plugins {
    id("kmp-configuration")
}

kmpConfiguration {
    setupMultiplatform(
        setOf(
            KmpTarget.Jvm.Jvm(kotlinJvmTarget = JavaVersion.VERSION_1_8),

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

            KmpTarget.NonJvm.Native.Unix.Darwin.Ios.All(enableSimulator = {}),
            KmpTarget.NonJvm.Native.Unix.Darwin.Macos.Arm64.DEFAULT,
            KmpTarget.NonJvm.Native.Unix.Darwin.Macos.X64.DEFAULT,
            KmpTarget.NonJvm.Native.Unix.Darwin.Tvos.All(enableSimulator = {}),
            KmpTarget.NonJvm.Native.Unix.Darwin.Watchos.All(enableSimulator = {}),

            KmpTarget.NonJvm.Native.Unix.Linux.Arm32Hfp.DEFAULT,
            KmpTarget.NonJvm.Native.Unix.Linux.Mips32.DEFAULT,
            KmpTarget.NonJvm.Native.Unix.Linux.Mipsel32.DEFAULT,
            KmpTarget.NonJvm.Native.Unix.Linux.X64.DEFAULT,

            KmpTarget.NonJvm.Native.Mingw.X64.DEFAULT,
            KmpTarget.NonJvm.Native.Mingw.X86.DEFAULT,
        ),

        commonMainSourceSet = {
            dependencies {
                implementation(kotlin("test"))
            }
        }
    )
}
