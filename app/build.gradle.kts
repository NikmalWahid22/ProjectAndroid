plugins {
    id("com.android.application")
    id("org.jetbrains.kotlin.android")
    id("org.jetbrains.kotlin.kapt")
    id("com.google.gms.google-services")
}

android {
    namespace = "com.campeat.app"
    compileSdk = 34

    defaultConfig {
        applicationId = "com.campeat.app"
        minSdk = 24
        targetSdk = 34
        versionCode = 1
        versionName = "1.0"
    }

    buildTypes {
        release {
            isMinifyEnabled = false
        }
    }

    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_11
        targetCompatibility = JavaVersion.VERSION_11
    }

    kotlinOptions {
        jvmTarget = "11"
    }
}

dependencies {
    implementation("androidx.appcompat:appcompat:1.6.1")
    implementation("com.google.android.material:material:1.11.0")
    implementation("androidx.activity:activity:1.8.2")
    implementation("androidx.constraintlayout:constraintlayout:2.1.4")

    // ROOM DATABASE
    implementation("androidx.room:room-runtime:2.6.1")
    implementation(libs.firebase.database)
    kapt("androidx.room:room-compiler:2.6.1")
    implementation("com.google.firebase:firebase-auth")

    implementation("org.jetbrains.kotlin:kotlin-stdlib:1.9.24")

    // LOCATION
    implementation("com.google.android.gms:play-services-location:21.0.1")

    // FIREBASE
    implementation(platform("com.google.firebase:firebase-bom:32.7.0"))
    implementation("com.google.firebase:firebase-database")

    // lottiflies
    implementation("com.airbnb.android:lottie:6.6.7")

    // SSO
    implementation("com.google.android.gms:play-services-auth:21.0.0")

    // AI assistant
    implementation("com.google.ai.client.generativeai:generativeai:0.9.0")

    implementation("com.google.guava:guava:31.1-android")
}