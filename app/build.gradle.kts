import java.util.Properties
import java.io.FileInputStream

plugins {
    id("com.android.application")
    id("org.jetbrains.kotlin.android")
    id("com.google.devtools.ksp")
}

// Load values from local.properties, which is NOT committed to GitHub
// (it's listed in .gitignore). This keeps the AdMob App ID and ad unit
// IDs out of your public source history, so anyone browsing the repo
// on GitHub can't see them directly.
val localProperties = Properties()
val localPropertiesFile = rootProject.file("local.properties")
if (localPropertiesFile.exists()) {
    localProperties.load(FileInputStream(localPropertiesFile))
}

fun readLocalProp(key: String, default: String): String =
    localProperties.getProperty(key) ?: default

android {
    namespace = "com.fitflow.app"
    compileSdk = 34

    defaultConfig {
        applicationId = "com.fitflow.app"
        minSdk = 26
        targetSdk = 34
        versionCode = 1
        versionName = "1.0"

        vectorDrawables {
            useSupportLibrary = true
        }

        manifestPlaceholders["admobAppId"] =
            readLocalProp("ADMOB_APP_ID", "ca-app-pub-3940256099942544~3347511713")

        buildConfigField(
            "String", "ADMOB_BANNER_ID",
            "\"${readLocalProp("ADMOB_BANNER_ID", "ca-app-pub-3940256099942544/9214589741")}\""
        )
        buildConfigField(
            "String", "ADMOB_INTERSTITIAL_ID",
            "\"${readLocalProp("ADMOB_INTERSTITIAL_ID", "ca-app-pub-3940256099942544/1033173712")}\""
        )
        buildConfigField(
            "String", "ADMOB_NATIVE_ID",
            "\"${readLocalProp("ADMOB_NATIVE_ID", "ca-app-pub-3940256099942544/2247696110")}\""
        )
    }

    buildTypes {
        release {
            isMinifyEnabled = false
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro"
            )
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
        compose = true
        buildConfig = true
    }

    composeOptions {
        kotlinCompilerExtensionVersion = "1.5.14"
    }

    packaging {
        resources {
            excludes += "/META-INF/{AL2.0,LGPL2.1}"
        }
    }
}

dependencies {
    // Core
    implementation("androidx.core:core-ktx:1.13.1")
    implementation("androidx.lifecycle:lifecycle-runtime-ktx:2.8.3")
    implementation("androidx.activity:activity-compose:1.9.0")

    // Compose BOM
    implementation(platform("androidx.compose:compose-bom:2024.06.00"))
    implementation("androidx.compose.ui:ui")
    implementation("androidx.compose.ui:ui-graphics")
    implementation("androidx.compose.ui:ui-tooling-preview")
    implementation("androidx.compose.material3:material3")
    implementation("androidx.compose.material:material-icons-extended")

    // Navigation
    implementation("androidx.navigation:navigation-compose:2.7.7")

    // ViewModel for Compose
    implementation("androidx.lifecycle:lifecycle-viewmodel-compose:2.8.3")
    implementation("androidx.lifecycle:lifecycle-runtime-compose:2.8.3")

    // Room (local database)
    implementation("androidx.room:room-runtime:2.6.1")
    implementation("androidx.room:room-ktx:2.6.1")
    ksp("androidx.room:room-compiler:2.6.1")

    // WorkManager (reminders)
    implementation("androidx.work:work-runtime-ktx:2.9.1")

    // DataStore (small key-value settings - used for guest ID + onboarding flag)
    implementation("androidx.datastore:datastore-preferences:1.1.1")

    // AdMob (Google Mobile Ads SDK)
    implementation("com.google.android.gms:play-services-ads:23.3.0")

    // Testing
    testImplementation("junit:junit:4.13.2")
    androidTestImplementation("androidx.test.ext:junit:1.2.1")
    androidTestImplementation("androidx.test.espresso:espresso-core:3.6.1")
    androidTestImplementation(platform("androidx.compose:compose-bom:2024.06.00"))
    androidTestImplementation("androidx.compose.ui:ui-test-junit4")
    debugImplementation("androidx.compose.ui:ui-tooling")
    debugImplementation("androidx.compose.ui:ui-test-manifest")
}
