rootProject.name = "encoding"

includeBuild("kotlin-components/includeBuild/dependencies")
includeBuild("kotlin-components/includeBuild/kmp")

@Suppress("PrivatePropertyName")
private val CHECK_PUBLICATION: String? by settings

if (CHECK_PUBLICATION != null) {
    include(":tools:check-publication")
} else {
    listOf(
        "encoding-base16",
        "encoding-base32",
        "encoding-base64",
        "encoding-core",
        "encoding-test",
    ).forEach { name ->
        include(":library:$name")
    }
}
