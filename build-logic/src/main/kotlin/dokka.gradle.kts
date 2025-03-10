/*
 * Copyright (c) 2024 Matthew Nelson
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
import org.jetbrains.dokka.gradle.DokkaExtension
import org.jetbrains.dokka.gradle.engine.parameters.VisibilityModifier
import java.net.URI

plugins {
    id("org.jetbrains.dokka")
}

rootProject.dependencies { dokka(project(project.path)) }

extensions.configure<DokkaExtension> {
    dokkaPublications.configureEach {
        suppressInheritedMembers.set(true)
    }

    dokkaSourceSets.configureEach {
        includes.from("README.md")
        enableKotlinStdLibDocumentationLink.set(false)

        sourceLink {
            localDirectory.set(rootDir)
            remoteUrl.set(URI("https://github.com/05nelsonm/encoding/tree/master"))
            remoteLineSuffix.set("#L")
        }

        documentedVisibilities(
            VisibilityModifier.Public,
            VisibilityModifier.Protected,
        )
    }
}
