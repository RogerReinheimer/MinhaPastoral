plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.kotlin.android)
    // Usando a versão correta e estável do KSP
    id("com.google.devtools.ksp") version "1.9.22-1.0.17"
    alias(libs.plugins.google.gms.google.services)
}
android {
    namespace = "com.example.anotacao"
    compileSdk = 34

    splits {
        abi.isEnable = true
        abi.reset()
        abi.include("armeabi-v7a", "arm64-v8a", "x86", "x86_64")
        abi.isUniversalApk = false
    }

    defaultConfig {
        applicationId = "com.example.anotacao"
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
        sourceCompatibility = JavaVersion.VERSION_11
        targetCompatibility = JavaVersion.VERSION_11
    }
    kotlinOptions {
        jvmTarget = "11"
    }

    buildFeatures {
        viewBinding = true
    }
}

dependencies {

    implementation ("com.github.bumptech.glide:glide:4.15.1")
    implementation ("com.google.firebase:firebase-storage-ktx:20.3.0")


    implementation("androidx.compose.material3:material3:1.3.2")
    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.appcompat)
    implementation(libs.material)
    implementation(libs.androidx.activity)
    implementation(libs.androidx.constraintlayout)

    implementation(platform("com.google.firebase:firebase-bom:33.1.2"))
    implementation("com.google.firebase:firebase-auth-ktx")
    implementation("com.google.firebase:firebase-firestore-ktx")
    implementation("com.google.firebase:firebase-database-ktx")

    testImplementation(libs.junit)
    androidTestImplementation(libs.androidx.junit)
    androidTestImplementation(libs.androidx.espresso.core)

    // Retrofit
    implementation("com.squareup.retrofit2:retrofit:2.9.0")
    implementation("com.squareup.retrofit2:converter-gson:2.9.0")
    implementation("com.squareup.okhttp3:logging-interceptor:4.11.0")

    // Coroutines
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-android:1.7.1")
    implementation("androidx.lifecycle:lifecycle-runtime-ktx:2.6.2")
}