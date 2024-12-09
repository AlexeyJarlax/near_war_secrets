import org.gradle.kotlin.dsl.android
import org.gradle.kotlin.dsl.hilt

plugins {
    id("com.android.application")
    id("kotlin-android")
    id("com.google.dagger.hilt.android")
    id("org.jetbrains.kotlin.android")
    id("kotlin-kapt")
    id("com.google.devtools.ksp")
    id("org.jetbrains.kotlin.plugin.compose")
    id("org.jetbrains.kotlin.plugin.serialization")
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
        minSdk = 29
        targetSdk = 35
        versionCode = 62
        versionName = "1.62"
        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
    }

    buildTypes {
        release {
            isMinifyEnabled = true
            isShrinkResources = true
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro"
            )
            isDebuggable = false
            signingConfig = signingConfigs.getByName("debug")
        }
        debug {
            isMinifyEnabled = false
        }
    }


    bundle {
        abi {// Оптимизация для разных ABI (процессорных архитектур)
            enableSplit = true
        }
    }

    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
        isCoreLibraryDesugaringEnabled = true
        encoding = "UTF-8"
    }

    kotlinOptions {
        jvmTarget = "17"
        languageVersion = "1.9"
    }

    buildFeatures {
        buildConfig = true
        viewBinding = true
        dataBinding = true
        compose = true
    }

    composeOptions {
        kotlinCompilerExtensionVersion = "1.5.15"
    }

    packaging {
        resources {
            excludes += "META-INF/DEPENDENCIES"
        }
    }

    hilt {
        enableAggregatingTask = true
    }

    kapt {
        correctErrorTypes = true
        includeCompileClasspath = false
    }
}

composeCompiler {
    reportsDestination = layout.buildDirectory.dir("compose_compiler")
}

dependencies {
    implementation(libs.androidx.activity)
    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.appcompat)
    implementation(libs.androidx.foundation.android)
    implementation(libs.androidx.foundation.android)
    implementation(libs.androidx.foundation.android)
    implementation(libs.androidx.foundation.layout.android)
    coreLibraryDesugaring (libs.desugar.jdk.libs)

    // mailto: URI
    implementation(libs.email.intent.builder)

    // Dagger Hilt
    implementation(libs.hilt.android)
    kapt(libs.dagger.hilt.compiler)
    implementation(libs.androidx.lifecycle.viewmodel.compose)
    implementation(libs.hilt.navigation.compose.v100)

    // Jetpack Compose
    implementation(libs.androidx.ui)
    implementation(libs.androidx.activity.compose)
    implementation(libs.androidx.lifecycle.viewmodel.compose)
    implementation(libs.runtime.livedata)
    implementation(libs.android.maps.compose)
    implementation(libs.maps.compose.v272)
    implementation(libs.androidx.foundation)
    implementation(libs.google.accompanist.flowlayout)
    implementation (libs.androidx.ui.tooling.preview)
    debugImplementation (libs.androidx.ui.tooling)
    implementation (libs.androidx.runtime)

    // Compose навигация
    implementation (libs.androidx.navigation.compose)

    // графическая обработка (более современное решение по загрузке пикч вместо Glide или Picasso)
    implementation(libs.coil.compose)

    // визуал material
    implementation (libs.androidx.material3)
    implementation(libs.androidx.material)
    implementation(libs.androidx.material.icons.extended)
    implementation(libs.glide)
    implementation(libs.androidx.camera.camera2)
    implementation(libs.androidx.camera.lifecycle)
    implementation(libs.androidx.camera.view)

    // шифрование
    implementation (libs.androidx.security.crypto)

    // корутин
    implementation(libs.kotlinx.coroutines.android)
    implementation(libs.peko)

    //логи Тимбер
    implementation(libs.timber)

    // Требуемые обфускатором R8:
    implementation(libs.bcprov.jdk15on)
    implementation(libs.conscrypt.android)

    // тестирование
    testImplementation(libs.junit)
    androidTestImplementation(libs.androidx.junit)

    // запрос разрешений
    implementation (libs.accompanist.permissions)

    // Room
    implementation (libs.androidx.room.runtime)
    ksp (libs.androidx.room.compiler)
    implementation (libs.androidx.room.ktx)

    // работа со временем
    implementation (libs.androidx.datastore.preferences)

    //сдвоенный экран pager со смахиванием
    implementation (libs.accompanist.pager)
//    implementation("androidx.compose.foundation:foundation:1.4.3")

    // Google API Client
//    implementation (libs.google.api.client)

    // Joda-Time
    implementation (libs.joda.time)

    // Error Prone Annotations
    implementation (libs.error.prone.annotations)

    implementation (libs.tink.android)
}