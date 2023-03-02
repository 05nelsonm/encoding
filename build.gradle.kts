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

    findProperty("GROUP")?.let { group = it }
    findProperty("VERSION_NAME")?.let { version = it }
    findProperty("POM_DESCRIPTION")?.let { description = it.toString() }

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
    @Suppress("DSL_SCOPE_VIOLATION")
    alias(libs.plugins.binaryCompat)
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
