plugins {
    `kotlin-dsl`
}

repositories {
    gradlePluginPortal()
}

dependencies {
    implementation(files("../kotlin-components/includeBuild/dependencies/build/libs/dependencies-SNAPSHOT.jar"))
}
