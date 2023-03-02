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
import org.gradle.api.tasks.testing.logging.TestExceptionFormat
import org.gradle.api.tasks.testing.logging.TestLogEvent.FAILED
import org.gradle.api.tasks.testing.logging.TestLogEvent.PASSED
import org.gradle.api.tasks.testing.logging.TestLogEvent.SKIPPED
import org.gradle.api.tasks.testing.logging.TestLogEvent.STARTED
import org.jetbrains.kotlin.gradle.targets.js.yarn.YarnPlugin
import org.jetbrains.kotlin.gradle.targets.js.yarn.YarnRootExtension

buildscript {

    repositories {
        mavenCentral()
        gradlePluginPortal()
    }

    dependencies {
        classpath(libs.gradle.kotlin)
        classpath(libs.gradle.maven.publish)

        // NOTE: Do not place your application dependencies here; they belong
        // in the individual module build.gradle.kts files
    }
}

allprojects {

    repositories {
        mavenCentral()
    }

    tasks.withType<Test> {
        testLogging {
            exceptionFormat = TestExceptionFormat.FULL
            events(STARTED, PASSED, SKIPPED, FAILED)
            showStandardStreams = true
        }
    }

}

plugins.withType<YarnPlugin> {
    the<YarnRootExtension>().lockFileDirectory = rootDir.resolve(".kotlin-js-store")
}

plugins {
    id(pluginId.kmp.publish)
    @Suppress("DSL_SCOPE_VIOLATION")
    alias(libs.plugins.binaryCompat)
}

kmpPublish {
    setupRootProject(
        versionName = "1.2.2-SNAPSHOT",
        // 1.0.0-alpha1 == 01_00_00_11
        // 1.0.0-alpha2 == 01_00_00_12
        // 1.0.0-beta1  == 01_00_00_21
        // 1.0.0-rc1    == 01_00_00_31
        // 1.0.0        == 01_00_00_99
        // 1.0.1        == 01_00_01_99
        // 1.1.1        == 01_01_01_99
        // 1.15.1       == 01_15_01_99
        versionCode = /*0 */1_02_02_99,
        pomInceptionYear = 2021,
    )
}

@Suppress("LocalVariableName")
apiValidation {
    val CHECK_PUBLICATION = findProperty("CHECK_PUBLICATION") as? String

    if (CHECK_PUBLICATION != null) {
        ignoredProjects.add("check-publication")
    } else {
        nonPublicMarkers.add("io.matthewnelson.encoding.core.internal.InternalEncodingApi")

        ignoredProjects.add("encoding-test")
    }
}
