plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.kotlin.android)
}

android {
    namespace = "com.example.anotacao"
    compileSdk = 36

    defaultConfig {
        applicationId = "com.example.anotacao"
        minSdk = 28
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

    buildFeatures {
        viewBinding = true
    }

}

dependencies {
    implementation("androidx.compose.material3:material3:1.3.2")
    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.appcompat)
    implementation(libs.material)
    implementation(libs.androidx.activity)
    implementation(libs.androidx.constraintlayout)
    testImplementation(libs.junit)
    androidTestImplementation(libs.androidx.junit)
    androidTestImplementation(libs.androidx.espresso.core)
    // Retrofit para fazer as chamadas à API
    implementation("com.squareup.retrofit2:retrofit:2.9.0")
    // Gson para converter a resposta JSON em objetos Kotlin/Java
    implementation("com.squareup.retrofit2:converter-gson:2.9.0")

    // Coroutines para gerenciar as chamadas de rede sem travar a tela
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-android:1.7.1")
    implementation("androidx.lifecycle:lifecycle-runtime-ktx:2.6.2")    // Retrofit para fazer as chamadas à API
    implementation("com.squareup.retrofit2:retrofit:2.9.0")
    // Gson para converter a resposta JSON em objetos Kotlin/Java
    implementation("com.squareup.retrofit2:converter-gson:2.9.0")

    // Coroutines para gerenciar as chamadas de rede sem travar a tela
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-android:1.7.1")
    implementation("androidx.lifecycle:lifecycle-runtime-ktx:2.6.2")

    // Adicione esta linha para o inspetor de rede
    implementation("com.squareup.okhttp3:logging-interceptor:4.11.0")
}