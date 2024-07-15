import java.io.FileInputStream
import java.util.Properties

plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.jetbrains.kotlin.android)
}

val localProperties = Properties()
val localPropertiesFile = rootProject.file("local.properties")
if (localPropertiesFile.exists()) {
    localPropertiesFile.inputStream().use { stream ->
        localProperties.load(stream)
    }
}


val keystorePropertiesFile = rootProject.file("key.properties")
val keystoreProperties = Properties()

if (keystorePropertiesFile.exists()) {
    FileInputStream(keystorePropertiesFile).use { stream ->
        keystoreProperties.load(stream)
    }
}

android {
    namespace = "com.kaitokitaya.easytransfer"
    compileSdk = 34

    defaultConfig {
        applicationId = "com.kaitokitaya.easytransfer"
        minSdk = 29
        targetSdk = 34
        versionCode = 1
        versionName = "1.0.0"

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
        vectorDrawables {
            useSupportLibrary = true
        }
        val adsProjectId: String = localProperties.getProperty("adsProjectId") ?: ""
        manifestPlaceholders["ADS_PROJECT_ID"] = adsProjectId

        val bannerDevId = localProperties.getProperty("bannerDev") ?: ""
        val bannerPrdId = localProperties.getProperty("bannerPrd") ?: ""

        buildConfigField("String", "ADS_BANNER_ID_DEV", "\"${System.getenv("ADS_APPLICATION_ID_DEV") ?: bannerDevId}\"")
        buildConfigField("String", "ADS_BANNER_ID_PRD", "\"${System.getenv("ADS_APPLICATION_ID_PRD") ?: bannerPrdId}\"")
        buildConfigField("String", "ADS_APPLICATION_PROJECT_ID", "\"${System.getenv("ADS_APPLICATION_PROJECT_ID") ?: adsProjectId}\"")
    }

    signingConfigs {
        create("release") {
            keyAlias = keystoreProperties["keyAlias"] as String
            keyPassword = keystoreProperties["keyPassword"] as String
            storeFile = file(keystoreProperties["storeFile"] as String)
            storePassword = keystoreProperties["storePassword"] as String
        }
    }

    buildTypes {
        getByName("release") {
            isMinifyEnabled = false
            proguardFiles(getDefaultProguardFile("proguard-android-optimize.txt"), "proguard-rules.pro")
            signingConfig = signingConfigs.getByName("release")
            ndk {
                debugSymbolLevel = "SYMBOL_TABLE"
            }
        }
    }

    flavorDimensions += "version"

    productFlavors {
        create("development") {
            applicationIdSuffix = ".development"
            versionCode = 1
            versionName = "0.10.1"
            resValue("string", "app_name", "DEV EASY TRANSFER")
        }
        create("staging") {
            applicationIdSuffix = ".staging"
            versionCode = 1
            versionName = "0.20.1"
            resValue("string", "app_name", "STG EASY TRANSFER")
        }
        create("production") {
            versionCode = 3
            versionName = "1.0.0"
        }
    }

    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_1_8
        targetCompatibility = JavaVersion.VERSION_1_8
    }
    kotlinOptions {
        jvmTarget = "1.8"
    }
    buildFeatures {
        buildConfig = true
        compose = true
    }
    composeOptions {
        kotlinCompilerExtensionVersion = "1.5.1"
    }
    packaging {
        resources {
            excludes += "/META-INF/*"
        }
    }
}

dependencies {

    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.lifecycle.runtime.ktx)
    implementation(libs.androidx.activity.compose)
    implementation(platform(libs.androidx.compose.bom))
    implementation(libs.androidx.ui)
    implementation(libs.androidx.ui.graphics)
    implementation(libs.androidx.ui.tooling.preview)
    implementation(libs.androidx.material3)
    implementation(libs.androidx.navigation.compose)


    // Timber
    implementation(libs.timber)

    // HTTP Server
    implementation(libs.ktor.server.core)
    implementation(libs.ktor.server.netty)
    implementation(libs.kotlinx.coroutines.core)
    implementation(libs.ktor.ktor.server.call.logging)
    implementation(libs.ktor.server.html.builder)
    implementation(libs.kotlin.css)

    // HTTP Client
    implementation(libs.ktor.client.core)
    implementation(libs.ktor.client.cio)

    // Log module
    implementation(libs.slf4j.api)
    implementation(libs.logback.classic.v135)
    // Test Tool
    testImplementation(libs.junit)
    androidTestImplementation(libs.androidx.junit)
    androidTestImplementation(libs.androidx.espresso.core)
    androidTestImplementation(platform(libs.androidx.compose.bom))
    androidTestImplementation(libs.androidx.ui.test.junit4)
    debugImplementation(libs.androidx.ui.tooling)
    debugImplementation(libs.androidx.ui.test.manifest)

    // Admob
    implementation(libs.play.services.ads)
}