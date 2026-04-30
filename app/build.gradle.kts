import org.jetbrains.kotlin.gradle.dsl.JvmTarget

plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.kotlin.compose)
    alias(libs.plugins.kotlin.serialization)
}

kotlin {
    compilerOptions {
        jvmTarget = JvmTarget.JVM_17
    }
}

android {
    namespace = "com.kaii.trainspotter"
    compileSdk = 36

    defaultConfig {
        applicationId = "com.kaii.trainspotter"
        minSdk = 30
        targetSdk = 36
        versionCode = 180
        versionName = "1.8.0"

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
    }

    buildTypes {
        release {
            isShrinkResources = true
            isMinifyEnabled = true
            proguardFiles(getDefaultProguardFile("proguard-android-optimize.txt"), "proguard-rules.pro")
        }
    }

    splits {
        abi {
            isUniversalApk = false
            isEnable = true

            reset()
            include(includes = arrayOf("armeabi-v7a", "arm64-v8a"))
        }
    }

    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
    }

    buildFeatures {
        compose = true
    }
}

dependencies {

    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.lifecycle.runtime.ktx)
    implementation(libs.androidx.activity.compose)
    implementation(platform(libs.androidx.compose.bom))
    implementation(libs.androidx.compose.ui)
    implementation(libs.androidx.compose.ui.graphics)
    implementation(libs.androidx.compose.ui.tooling.preview)
    implementation(libs.androidx.compose.material3)
    implementation(libs.androidx.appcompat)
    implementation(libs.androidx.navigation)
    implementation(libs.androidx.datastore)
    implementation(libs.androidx.lifecycle)
    implementation(libs.androidx.splashscreen)

    implementation(libs.org.jetbrains.kotlinx.serialization)
    implementation(libs.org.jetbrains.kotlinx.datetime)

    implementation(libs.com.squareup.okhttp3)
    implementation(libs.com.squareup.okhttp3.coroutines)
    implementation(libs.com.squareup.okhttp3.sse)
    implementation(libs.io.github.pushpalroy.jetlime)
    implementation(libs.org.maplibre.compose)
    implementation(libs.org.maplibre.turf)

    implementation(libs.com.github.kaii.lb.lavender.snackbars)

    implementation(libs.mil.nga.geopackage.android)

    testImplementation(libs.junit)
    androidTestImplementation(libs.androidx.junit)
    androidTestImplementation(libs.androidx.espresso.core)
    androidTestImplementation(platform(libs.androidx.compose.bom))
    androidTestImplementation(libs.androidx.compose.ui.test.junit4)
    debugImplementation(libs.androidx.compose.ui.tooling)
    debugImplementation(libs.androidx.compose.ui.test.manifest)
}