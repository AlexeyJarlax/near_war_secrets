// Top-level build file where you can add configuration options common to all sub-projects/modules.
plugins {
    id("com.android.application") version "8.5.0" apply false
    id("org.jetbrains.kotlin.android") version "1.9.0" apply false
    id("com.google.devtools.ksp") version "1.8.10-1.0.9" apply false

}


allprojects {
    repositories {
//        maven { setUrl("https://www.jitpack.io") }
        // Другие репозитории, если есть
    }
}

buildscript {
    repositories {
        maven { setUrl("https://www.jitpack.io") }
        // Другие репозитории, если нужно
    }
}