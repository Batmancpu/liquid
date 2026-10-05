plugins {
    id("com.android.application")
    id("org.jetbrains.kotlin.android")
}

android {
    namespace = "mangoloads.liquid.com"
    compileSdk = 36

    defaultConfig {
        applicationId = "mangoloads.liquid.com"
        minSdk = 33
        targetSdk = 36
        versionCode = 1
        versionName = "0.1-liquid-lab"
    }

    buildTypes {
        release {
            isMinifyEnabled = false
        }
        debug {
            applicationIdSuffix = ".debug"
            versionNameSuffix = "-debug"
        }
    }
}

kotlin {
    jvmToolchain(17)
}

dependencies {
    implementation("com.github.QWEA0:liquidglass:v2.0.11")
}
