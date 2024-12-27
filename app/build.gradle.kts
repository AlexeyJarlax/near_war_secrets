plugins {
    id("com.android.application")
    id("kotlin-android")
    id("dagger.hilt.android.plugin")
    id("org.jetbrains.kotlin.plugin.compose")
    id("org.jetbrains.kotlin.plugin.serialization")
    id("com.google.devtools.ksp")
}

android {
    namespace = "com.pavlov.MyShadowGallery"
    compileSdk = 35

    buildFeatures {
        viewBinding = true
        compose = true
        dataBinding = true
        buildConfig = true
    }

    bundle {
        language {
            enableSplit = false
        }
        abi {
            enableSplit = true
        }
    }

    defaultConfig {
        applicationId = "com.pavlov.MyShadowGallery"
        resourceConfigurations += setOf("ru", "en", "zh", "es")
        minSdk = 29
        targetSdk = 35
        versionCode = 68
        versionName = "1.68"
        testInstrumentationRunner = "com.pavlov.MyShadowGallery.testing.HiltTestRunner"
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

    composeOptions {
        kotlinCompilerExtensionVersion = "1.5.15"
    }

    packaging {
        resources {
            excludes += "META-INF/DEPENDENCIES"
        }
    }

    testOptions {
        unitTests {
            isIncludeAndroidResources = true
        }
    }

    hilt {
        enableAggregatingTask = true
    }
}

dependencies {
    implementation(libs.androidx.activity)
    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.appcompat)
    implementation(libs.androidx.foundation.layout.android)
    coreLibraryDesugaring(libs.desugar.jdk.libs)

    // Dagger Hilt
    implementation(libs.hilt.android)
    ksp(libs.dagger.hilt.compiler)
    implementation(libs.androidx.lifecycle.viewmodel.compose)
    implementation(libs.hilt.navigation.compose.v100)

    // mailto: URI
    implementation(libs.email.intent.builder)

    // Jetpack Compose
    implementation(libs.androidx.ui)
    implementation(libs.androidx.activity.compose)
    implementation(libs.androidx.lifecycle.viewmodel.compose)
    implementation(libs.runtime.livedata)
    implementation(libs.android.maps.compose)
    implementation(libs.maps.compose.v272)
    implementation(libs.androidx.foundation)
    implementation(libs.google.accompanist.flowlayout)
    implementation(libs.androidx.ui.tooling.preview)
    debugImplementation(libs.androidx.ui.tooling)
    implementation(libs.androidx.runtime)

    // Compose + навигация
    implementation(libs.androidx.navigation.compose)

    // Coil для загрузки изображений
    implementation(libs.coil.compose)

    // Material Design
    implementation(libs.androidx.material3)
    implementation(libs.androidx.material)
    implementation(libs.androidx.material.icons.extended)
    implementation(libs.glide)
    implementation(libs.androidx.camera.camera2)
    implementation(libs.androidx.camera.lifecycle)
    implementation(libs.androidx.camera.view)

    // Шифрование
    implementation(libs.androidx.security.crypto)

    // Корутины
    implementation(libs.kotlinx.coroutines.android)
    implementation(libs.peko)

    // Логирование с Timber
    implementation(libs.timber)

    // Обфускатор R8
    implementation(libs.bcprov.jdk15on)
    implementation(libs.conscrypt.android)

    // Test
    testImplementation(libs.kotlinx.coroutines.test)
    testImplementation(libs.junit)
    androidTestImplementation(libs.androidx.junit)
    androidTestImplementation(libs.androidx.runner)
    testImplementation(libs.mockk)
    testImplementation(libs.androidx.core.testing)
    testImplementation(libs.mockito.core.v520)
    testImplementation(libs.mockito.inline)

    // Разрешения
    implementation(libs.accompanist.permissions)

    // Room
    implementation(libs.androidx.room.runtime)
    ksp(libs.androidx.room.compiler)
    implementation(libs.androidx.room.ktx)

    // Работа со временем
    implementation(libs.androidx.datastore.preferences)

    // Pager
    implementation(libs.accompanist.pager)

    // Core Library Desugaring
    coreLibraryDesugaring(libs.desugar.jdk.libs)
}
