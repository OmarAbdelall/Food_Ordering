plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.google.gms.google.services)
    alias(libs.plugins.kotlin.android)

}

android {
    namespace = "com.example.foodbot"
    compileSdk = 35

    defaultConfig {
        applicationId = "com.example.foodbot"
        minSdk = 28
        targetSdk = 34
        versionCode = 1
        versionName = "1.0"

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
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
}

dependencies {
    // those dependcenice for the Authentication
        implementation(platform("com.google.firebase:firebase-bom:32.7.1"))
        implementation("com.google.firebase:firebase-auth")
        implementation("com.google.firebase:firebase-database:20.3.0")
        implementation("com.google.android.material:material:1.11.0")
        // the end of them

    implementation("com.github.bumptech.glide:glide:4.16.0")

   // for flask
    implementation("com.android.volley:volley:1.2.1")

    // gif

    implementation("pl.droidsonroids.gif:android-gif-drawable:1.2.25")

    implementation(libs.appcompat)
    implementation(libs.material)
    implementation(libs.activity)
    implementation(libs.constraintlayout)
    implementation(libs.firebase.auth)
    implementation(libs.core.ktx)
    testImplementation(libs.junit)
    androidTestImplementation(libs.ext.junit)
    androidTestImplementation(libs.espresso.core)
}