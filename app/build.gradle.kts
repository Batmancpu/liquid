plugins {
    id("com.android.application")
    id("org.jetbrains.kotlin.android")
}

android {
    namespace = "mangoloads.liquid.com"
    compileSdk = 34

    defaultConfig {
        applicationId = "mangoloads.liquid.com"
        minSdk = 33
        targetSdk = 34
        versionCode = 7
        versionName = "0.7-liquid-glass-optical-debug"
    }

    signingConfigs {
        create("lab_signing") {
            storeFile = file("lab_key.jks")
            storePassword = "lab_password"
            keyAlias = "lab_alias"
            keyPassword = "lab_password"
        }
    }

    buildTypes {
        release {
            isMinifyEnabled = false
            signingConfig = signingConfigs.getByName("lab_signing")
        }
        debug {
            isMinifyEnabled = false
            signingConfig = signingConfigs.getByName("lab_signing")
        }
    }
}

kotlin {
    jvmToolchain(21)
}

dependencies {
    implementation("com.github.QWEA0:liquidglass:v2.0.11")
}
