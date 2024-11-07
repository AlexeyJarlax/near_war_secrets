plugins {
    id("com.android.application")
//    id("com.google.dagger.hilt.android")
    id("org.jetbrains.kotlin.android")
    id("kotlin-kapt")
    id("com.google.devtools.ksp")
    id("org.jetbrains.kotlin.plugin.compose")
    id("org.jetbrains.kotlin.plugin.serialization")
    id("com.autonomousapps.dependency-analysis")
}

android {
    namespace = "com.pavlov.MyShadowGallery"
    compileSdk = 35

    buildFeatures {
        viewBinding = true
    }

    bundle {
        language {
            enableSplit = false
        }
    }

    defaultConfig {
        applicationId = "com.pavlov.MyShadowGallery"
        resourceConfigurations += setOf("ru", "en", "zh", "es")
        minSdk = 28
        targetSdk = 35
        versionCode = 53
        versionName = "1.53"
        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
    }

    buildTypes {
        release {
            isMinifyEnabled = false  // Выключить ProGuard
            proguardFiles(
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
    // графическая обработка
    implementation(libs.coil.compose)

    // compose
    implementation ("androidx.compose.ui:ui:1.7.5")
    implementation ("androidx.compose.material:material:1.7.5")
    implementation ("androidx.compose.ui:ui-tooling-preview:1.7.5")
    implementation ("androidx.activity:activity-compose:1.9.3")
    implementation ("androidx.lifecycle:lifecycle-viewmodel-compose:2.8.7")
    implementation ("androidx.navigation:navigation-compose:2.8.3")
    implementation ("androidx.compose.material:material-icons-extended:1.7.5")

    implementation("androidx.lifecycle:lifecycle-viewmodel-ktx:2.8.7")
    implementation("androidx.lifecycle:lifecycle-livedata-ktx:2.8.7")
    implementation("androidx.lifecycle:lifecycle-runtime-ktx:2.8.7")
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-android:1.9.0")

//    implementation("com.github.christian:PhotoView:2.3.0")
    implementation("androidx.core:core-ktx:1.15.0")
    implementation("androidx.appcompat:appcompat:1.7.0")
    implementation("com.google.android.material:material:1.12.0")
    implementation("androidx.constraintlayout:constraintlayout:2.2.0")
    implementation("com.github.bumptech.glide:glide:4.15.1")
    implementation("androidx.camera:camera-camera2:1.4.0")
    implementation("androidx.camera:camera-lifecycle:1.4.0")
    implementation("androidx.camera:camera-view:1.4.0")
    implementation("androidx.compose.runtime:runtime-livedata:1.7.5")
    testImplementation("junit:junit:4.13.2")
    androidTestImplementation("androidx.test.ext:junit:1.2.1")
    androidTestImplementation("androidx.test.espresso:espresso-core:3.6.1")
    implementation("com.google.code.gson:gson:2.10.1") // эта строка и ниже: связь с сервером
    implementation("com.squareup.retrofit2:retrofit:2.11.0")
    implementation("com.squareup.retrofit2:converter-gson:2.11.0")
    implementation("com.squareup.okhttp3:okhttp:4.12.0")
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-core:1.9.0") // Корутины многопоточности
    implementation("androidx.security:security-crypto:1.1.0-alpha06")  // хэширование
}