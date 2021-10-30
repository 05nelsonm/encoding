import io.matthewnelson.kotlin.components.dependencies.deps
import io.matthewnelson.kotlin.components.dependencies.versions

plugins {
    id("com.android.application")
    id("kotlin-android")
}

android {
    compileSdk = versions.android.sdkCompile
    buildToolsVersion = versions.android.buildTools

    packagingOptions {
        resources.excludes.add("META-INF/gradle/incremental.annotation.processors")
    }

    buildFeatures.viewBinding = true
    defaultConfig {
        applicationId = "io.matthewnelson.app"
        minSdk = versions.android.sdkMin16
        targetSdk = versions.android.sdkTarget
        versionCode = 1
        versionName = "1.0.0"

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
        testInstrumentationRunnerArguments["disableAnalytics"] = "true"
    }

    // Gradle 4.0's introduction of Google analytics to Android App Developers.
    // https://developer.android.com/studio/releases/gradle-plugin
    dependenciesInfo {
        // Disables dependency metadata when building APKs.
        includeInApk = false
        // Disables dependency metadata when building Android App Bundles.
        includeInBundle = false
    }

    buildTypes {
        getByName("release") {
            isMinifyEnabled = false
            proguardFiles(getDefaultProguardFile("proguard-android-optimize.txt"), "proguard-rules.pro")
        }
    }

    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_1_8
        targetCompatibility = JavaVersion.VERSION_1_8
    }

    kotlinOptions {
        jvmTarget = JavaVersion.VERSION_1_8.toString()
    }
}

dependencies {
    implementation(project(":encoding-base32"))
    implementation(project(":encoding-base64"))

    implementation(deps.androidx.appCompat)
    implementation(deps.androidx.constraintLayout)
    implementation(deps.viewBindingDelegateNoReflect)
}
