rootProject.name = "encoding"

pluginManagement {
    repositories {
        mavenCentral()
        gradlePluginPortal()
    }
}

includeBuild("build-logic")

@Suppress("PrivatePropertyName")
private val CHECK_PUBLICATION: String? by settings

if (CHECK_PUBLICATION != null) {
    include(":tools:check-publication")
} else {
    listOf(
        "base16",
        "base32",
        "base64",
        "core",
        "test",
    ).forEach { name ->
        include(":library:$name")
    }

//    include(":benchmarks")
    include(":bom")
    include(":sample")
}
