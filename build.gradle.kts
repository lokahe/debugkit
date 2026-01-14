// Top-level build file where you can add configuration options common to all sub-projects/modules.
buildscript {
    // In Kotlin DSL, use 'val' or 'extra' to define properties
    val compose_version by extra("2.0.21")
    val kotlin_version by extra("2.0.21")

    repositories {
        mavenCentral()
        google()
    }
    dependencies {
        classpath("com.android.tools.build:gradle:8.10.1")
        classpath("org.jetbrains.kotlin:kotlin-gradle-plugin:$kotlin_version")
    }
}
plugins {
    // Check for the latest version in 2026
    id("com.vanniktech.maven.publish") version "0.30.0" apply false
}