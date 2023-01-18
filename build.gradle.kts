/*
 * Copyright (c) 2021 Matthew Nelson
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
import io.matthewnelson.kotlin.components.kmp.util.configureYarn
import org.gradle.api.tasks.testing.logging.TestExceptionFormat
import org.gradle.api.tasks.testing.logging.TestLogEvent.FAILED
import org.gradle.api.tasks.testing.logging.TestLogEvent.PASSED
import org.gradle.api.tasks.testing.logging.TestLogEvent.SKIPPED
import org.gradle.api.tasks.testing.logging.TestLogEvent.STARTED
import org.jetbrains.kotlin.psi.addRemoveModifier.addAnnotationEntry

// Top-level build file where you can add configuration options common to all sub-projects/modules.
buildscript {

    repositories {
        mavenCentral()
        google()
        gradlePluginPortal()
    }

    dependencies {
        classpath(pluginDeps.android.gradle)
        classpath(pluginDeps.kotlin.gradle)
        classpath(pluginDeps.mavenPublish)

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

configureYarn { rootYarn, _ ->
    rootYarn.apply {
        lockFileDirectory = project.rootDir.resolve(".kotlin-js-store")
    }
}

plugins {
    id(pluginId.kmp.publish)
    id(pluginId.kotlin.binaryCompat) version(versions.gradle.binaryCompat)
}

kmpPublish {
    setupRootProject(
        versionName = "1.1.6-SNAPSHOT",
        // 1.0.0-alpha1 == 01_00_00_11
        // 1.0.0-alpha2 == 01_00_00_12
        // 1.0.0-beta1  == 01_00_00_21
        // 1.0.0-rc1    == 01_00_00_31
        // 1.0.0        == 01_00_00_99
        // 1.0.1        == 01_00_01_99
        // 1.1.1        == 01_01_01_99
        // 1.15.1       == 01_15_01_99
        versionCode = /*0 */1_01_06_99,
        pomInceptionYear = 2021,
    )
}

@Suppress("LocalVariableName")
apiValidation {
    val KMP_TARGETS = findProperty("KMP_TARGETS") as? String
    val CHECK_PUBLICATION = findProperty("CHECK_PUBLICATION") as? String
    val KMP_TARGETS_ALL = System.getProperty("KMP_TARGETS_ALL") != null
    val TARGETS = KMP_TARGETS?.split(',')

    if (CHECK_PUBLICATION != null) {
        ignoredProjects.add("check-publication")
    } else {
        nonPublicMarkers.add("io.matthewnelson.encoding.core.internal.InternalEncodingApi")

        val JVM = TARGETS?.contains("JVM") != false
        val ANDROID = TARGETS?.contains("ANDROID") != false

        ignoredProjects.add("encoding-test")

        if (KMP_TARGETS_ALL || (ANDROID && JVM)) {
            ignoredProjects.add("app")
        }
    }
}
