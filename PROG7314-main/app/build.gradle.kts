plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.kotlin.android)
    id("androidx.navigation.safeargs")
    id("kotlin-kapt")
    id("com.google.devtools.ksp") version "2.0.21-1.0.25"
    alias(libs.plugins.google.gms.google.services)
}

android {
    namespace = "com.example.fitme"
    compileSdk = 36

    defaultConfig {
        applicationId = "com.example.fitme"
        minSdk = 24
        targetSdk = 36
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
        sourceCompatibility = JavaVersion.VERSION_11
        targetCompatibility = JavaVersion.VERSION_11
    }
    kotlinOptions {
        jvmTarget = "11"
    }

    buildFeatures{
        viewBinding = true
    }
}

dependencies {

    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.appcompat)
    implementation(libs.material)
    implementation(libs.androidx.activity)
    implementation(libs.androidx.constraintlayout)
    implementation(libs.androidx.lifecycle.viewmodel.ktx)
    testImplementation(libs.junit)
    androidTestImplementation(libs.androidx.junit)
    androidTestImplementation(libs.androidx.espresso.core)

    implementation(libs.androidx.navigation.fragment.ktx)
    implementation(libs.androidx.navigation.ui.ktx)
    implementation(libs.mpandroidchart)
    implementation(libs.androidx.recyclerview)
    implementation(libs.androidx.cardview)

    // Room runtime
    implementation(libs.androidx.room.runtime)

    // Kotlin extensions + Coroutines support
    implementation(libs.androidx.room.ktx)

    // ksp for Kotlin (Room compiler)
    ksp(libs.androidx.room.compiler)

    // (Optional) Testing Room
    testImplementation(libs.androidx.room.testing)

    // Kotlin Coroutines
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-android:1.7.3")
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-core:1.7.3")

    //Retrofit:
    implementation("com.squareup.retrofit2:retrofit:2.9.0")
    implementation("com.squareup.retrofit2:converter-gson:2.9.0")

    //Biometric Authentication:
    implementation("androidx.biometric:biometric-ktx:1.4.0-alpha02")

    //Firebase Database:
    implementation(libs.firebase.database)
    implementation(platform("com.google.firebase:firebase-bom:34.5.0"))
    implementation("com.google.firebase:firebase-auth")
    implementation(libs.google.firebase.firestore)
}