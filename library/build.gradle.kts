plugins {
    id("com.android.library")
    kotlin("android")
    id("com.vanniktech.maven.publish")
}

android {
    namespace = "com.lokahe.debugkit"
    compileSdk = 36

    defaultConfig {
        minSdk = 21

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
        consumerProguardFiles("consumer-rules.pro")
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
}

dependencies {
    implementation("androidx.core:core-ktx:1.17.0")
    implementation("androidx.appcompat:appcompat:1.7.1")
    implementation("com.google.android.material:material:1.13.0")
    testImplementation("junit:junit:4.13.2")
    androidTestImplementation("androidx.test.ext:junit:1.3.0")
    androidTestImplementation("androidx.test.espresso:espresso-core:3.7.0")
}

version = "1.0.1"
// Plugin configuration
mavenPublishing {
    // Define coordinates
    coordinates("io.github.lokahe", "debugkit", version.toString())

    // Configure POM metadata
    pom {
        name.set("debugkit")
        description.set("A concise description of what the library does.")
        url.set("https://github.com/lokahe/debugkit")
        licenses {
            license {
                name.set("The Apache License, Version 2.0")
                url.set("www.apache.org")
            }
        }
        developers {
            developer {
                id.set("lokahe")
                name.set("lokahe")
                email.set("lokahe619@gmail.com")
            }
        }
        scm {
            connection.set("scm:git:https://github.com/lokahe/debugkit.git")
            developerConnection.set("scm:git:ssh://git@github.com/lokahe/debugkit.git")
            url.set("https://github.com/lokahe/debugkit/")
        }
    }

    // This handles the Maven Central Portal (Sonatype S01/S10) integration
    publishToMavenCentral(automaticRelease = true)

    // Enable GPG signing
    signAllPublications()
}
