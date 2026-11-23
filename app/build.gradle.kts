plugins {
    id("com.android.application")
    id("org.jetbrains.kotlin.android")
}

val ciVersionCode = (project.findProperty("appVersionCode") as String?)?.toIntOrNull() ?: 1
val ciVersionName = (project.findProperty("appVersionName") as String?) ?: "1.0.1"

android {
    namespace = "com.xcluice.minimilitia3d"
    compileSdk = 34

    defaultConfig {
        applicationId = "com.xcluice.minimilitia3d"
        minSdk = 26
        targetSdk = 34
        versionCode = ciVersionCode
        versionName = ciVersionName
    }

    signingConfigs {
        create("release") {
            val ks = file("release.jks")
            if (ks.exists()) {
                storeFile = ks
                storePassword = "xcluice2026"
                keyAlias = "xcluice"
                keyPassword = "xcluice2026"
            }
        }
    }

    buildTypes {
        release {
            isMinifyEnabled = false
            isShrinkResources = false
            signingConfig = signingConfigs.getByName("release")
        }
    }

    lint {
        abortOnError = false
        checkReleaseBuilds = false
    }

    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
    }
    kotlinOptions {
        jvmTarget = "17"
    }
}
