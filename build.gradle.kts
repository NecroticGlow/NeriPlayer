plugins {
    alias(libs.plugins.android.application) apply false
    alias(libs.plugins.kotlin.android) apply false
}

buildscript {
    repositories {
        mavenCentral() // 去除 Google 仓库配置
    }
    dependencies {
        classpath("org.jetbrains.kotlin:kotlin-gradle-plugin:1.5.21")
        classpath("com.android.tools.build:gradle:7.0.2")
    }
}

allprojects {
    repositories {
        mavenCentral() // 去除 Google 仓库配置
    }
}

task("clean", type = Delete::class) {
    delete(rootProject.buildDir)
}
