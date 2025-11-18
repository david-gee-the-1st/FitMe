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
        addManifestPlaceholders(mapOf(
            "auth0Domain" to "@string/com_auth0_domain",
            "auth0Scheme" to "@string/com_auth0_scheme"
        ))
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
    implementation(libs.firebase.storage)
    implementation(libs.firebase.database)
    implementation(libs.firebase.firestore)
    testImplementation(libs.junit)
    androidTestImplementation(libs.androidx.junit)
    androidTestImplementation(libs.androidx.espresso.core)

    implementation(libs.androidx.navigation.fragment.ktx)
    implementation(libs.androidx.navigation.ui.ktx)
    implementation(libs.mpandroidchart)
    implementation(libs.androidx.recyclerview)
    implementation(libs.androidx.cardview)

    implementation("androidx.navigation:navigation-fragment-ktx:2.5.0")
    implementation("androidx.navigation:navigation-ui-ktx:2.5.0")
    implementation("com.github.PhilJay:MPAndroidChart:v3.1.0")

    // Room runtime
    implementation(libs.androidx.room.runtime)

    // Kotlin extensions + Coroutines support
    implementation(libs.androidx.room.ktx)

    // Annotation processor (Java)
    annotationProcessor(libs.androidx.room.compiler)

    // ksp for Kotlin
    ksp(libs.androidx.room.compiler.v281)

    // (Optional) Testing Room
    testImplementation(libs.androidx.room.testing)

    //Retrofit:
    implementation("com.squareup.retrofit2:retrofit:3.0.0")
    implementation("com.squareup.retrofit2:converter-gson:3.0.0")

    //Biometric Authentication:
    implementation("androidx.biometric:biometric-ktx:1.4.0-alpha02")

    //Single Sign On authentication
    // Add the Auth0 Android SDK
    implementation("com.auth0.android:auth0:3.10.0")
    implementation("com.auth0.android:jwtdecode:2.0.2")

    implementation("com.google.androidbrowserhelper:androidbrowserhelper:2.6.2")

    implementation(platform("com.google.firebase:firebase-bom:34.6.0"))

    implementation("com.google.firebase:firebase-analytics")
}