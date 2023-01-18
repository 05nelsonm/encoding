rootProject.name = "component-encoding"

includeBuild("kotlin-components/includeBuild/dependencies")
includeBuild("kotlin-components/includeBuild/kmp")

@Suppress("PrivatePropertyName")
private val KMP_TARGETS: String? by settings
@Suppress("PrivatePropertyName")
private val CHECK_PUBLICATION: String? by settings
@Suppress("PrivatePropertyName")
private val KMP_TARGETS_ALL = System.getProperty("KMP_TARGETS_ALL") != null
@Suppress("PrivatePropertyName")
private val TARGETS = KMP_TARGETS?.split(',')

if (CHECK_PUBLICATION != null) {
    include(":tools:check-publication")
} else {
    listOf(
        "encoding-base16",
        "encoding-base32",
        "encoding-base64",
        "encoding-test",
    ).forEach { name ->
        include(":library:$name")
    }

    if (KMP_TARGETS_ALL || (TARGETS?.contains("ANDROID") != false && TARGETS?.contains("JVM") != false)) {
        include(":app")
    }
}
