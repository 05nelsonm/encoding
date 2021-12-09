rootProject.name = "component-encoding"

includeBuild("kotlin-components/includeBuild/dependencies")
includeBuild("kotlin-components/includeBuild/kmp")

include(":encoding-base16")
include(":encoding-base32")
include(":encoding-base64")
include(":encoding-test")

// if ANDROID is not being built, don't include the app as it relies
// on some android only kmp projects
@Suppress("PrivatePropertyName")
private val KMP_TARGETS: String? by settings
@Suppress("PrivatePropertyName")
private val KMP_TARGETS_ALL: String? by settings
if (KMP_TARGETS_ALL != null || KMP_TARGETS?.split(',')?.contains("JVM") != false) {
    include(":app")
}

@Suppress("PrivatePropertyName")
private val CHECK_PUBLICATION: String? by settings
if (CHECK_PUBLICATION != null) {
    include(":tools:check-publication")
}
