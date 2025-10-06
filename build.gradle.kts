// Top-level build file where you can add configuration options common to all sub-projects/modules.
plugins {
    // Android application plugin
    alias(libs.plugins.android.application) apply false
    // Kotlin plugin for Android
    alias(libs.plugins.kotlin.android) apply false
}

buildscript {
    repositories {
        google()
        mavenCentral()
    }
    dependencies {
        // Kotlin Gradle plugin version
        classpath("org.jetbrains.kotlin:kotlin-gradle-plugin:1.5.21")
        // Android Gradle plugin version
        classpath("com.android.tools.build:gradle:7.0.2")
    }
}

allprojects {
    repositories {
        google()
        mavenCentral()
    }
}

task("clean", type = Delete::class) {
    delete(rootProject.buildDir)
}

