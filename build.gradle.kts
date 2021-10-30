import org.gradle.api.tasks.testing.logging.TestExceptionFormat
import org.gradle.api.tasks.testing.logging.TestLogEvent.FAILED
import org.gradle.api.tasks.testing.logging.TestLogEvent.PASSED
import org.gradle.api.tasks.testing.logging.TestLogEvent.SKIPPED
import org.gradle.api.tasks.testing.logging.TestLogEvent.STARTED

// Top-level build file where you can add configuration options common to all sub-projects/modules.
buildscript {

    repositories {
        mavenCentral()
        google()
        gradlePluginPortal()
    }

    dependencies {
        classpath(io.matthewnelson.kotlin.components.dependencies.plugins.android.gradle)

        /* Replace version 1.5.31 with 1.6.0-RC due to native test errors with llvm */
        classpath(io.matthewnelson.kotlin.components.dependencies.plugins.kotlin.gradle
            .replaceAfterLast(':', "1.6.0-RC")
        )
        classpath(io.matthewnelson.kotlin.components.dependencies.plugins.intellij)
        classpath(io.matthewnelson.kotlin.components.dependencies.plugins.mavenPublish)

        // NOTE: Do not place your application dependencies here; they belong
        // in the individual module build.gradle.kts files
    }
}

allprojects {

    repositories {
        mavenCentral()
        google()
        gradlePluginPortal()
    }

    tasks.withType<Test> {
        testLogging {
            exceptionFormat = TestExceptionFormat.FULL
            events(STARTED, PASSED, SKIPPED, FAILED)
            showStandardStreams = true
        }
    }

}

plugins {
    id("kmp-publish")
}

kmpPublish {
    setupRootProject(
        versionName = "1.0.0",
        versionCode = 100000,
        pomInceptionYear = 2021,
    )
}
