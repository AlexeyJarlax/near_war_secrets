plugins {
    id("com.android.application")
    id("org.jetbrains.kotlin.android")
//    id("kotlin-kapt") //  плагин kapt
    id("com.google.devtools.ksp")

}

android {
    namespace = "com.pavlov.MyShadowGallery"
    compileSdk = 34

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
        resConfigs("ru", "en", "zh", "es")
        minSdk = 28
        targetSdk = 34
        versionCode = 41
        versionName = "1.41"
        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
    }

    android {
        buildTypes {
            release {
                isMinifyEnabled = true  // Включить ProGuard
                proguardFiles(
                    getDefaultProguardFile("proguard-android-optimize.txt"),
                    "proguard-rules.pro"
                )
            }
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
    implementation("com.github.chrisbanes:PhotoView:2.3.0")
    implementation("androidx.core:core-ktx:1.12.0")
    implementation("androidx.appcompat:appcompat:1.6.1")
    implementation("com.google.android.material:material:1.10.0")
    implementation("androidx.constraintlayout:constraintlayout:2.1.4")
    implementation("com.github.bumptech.glide:glide:4.14.2")
    implementation("androidx.camera:camera-camera2:1.3.0")
    implementation("androidx.camera:camera-lifecycle:1.3.0")
    implementation("androidx.camera:camera-view:1.4.0-alpha02")
    testImplementation("junit:junit:4.13.2")
    androidTestImplementation("androidx.test.ext:junit:1.1.5")
    androidTestImplementation("androidx.test.espresso:espresso-core:3.5.1")
    implementation("com.google.code.gson:gson:2.10") // эта строка и ниже: связь с сервером
    implementation("com.squareup.retrofit2:retrofit:2.9.0")
    implementation("com.squareup.retrofit2:converter-gson:2.9.0")
    implementation("com.squareup.okhttp3:okhttp:4.9.1")
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-core:1.5.2") // Корутины многопоточности
    implementation("androidx.security:security-crypto:1.1.0-alpha06")  // хэширование
}