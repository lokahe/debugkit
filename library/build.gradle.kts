import com.android.build.api.instrumentation.AsmClassVisitorFactory
import com.android.build.api.instrumentation.ClassContext
import com.android.build.api.instrumentation.ClassData
import com.android.build.api.instrumentation.InstrumentationParameters
import com.android.build.api.instrumentation.InstrumentationScope
import org.objectweb.asm.ClassVisitor
import org.objectweb.asm.commons.ClassRemapper
import org.objectweb.asm.commons.Remapper

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

    buildFeatures {
        buildConfig = true
    }

    buildTypes {
        release {
            isMinifyEnabled = false
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro"
            )
            buildConfigField("Boolean", "DEBUG", "false")
        }
        debug {
            buildConfigField("Boolean", "DEBUG", "true")
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
    compileOnly(project(":stub"))
    implementation("androidx.core:core-ktx:1.17.0")
    implementation("androidx.appcompat:appcompat:1.7.1")
    implementation("com.google.android.material:material:1.13.0")
    testImplementation("junit:junit:4.13.2")
    androidTestImplementation("androidx.test.ext:junit:1.3.0")
    androidTestImplementation("androidx.test.espresso:espresso-core:3.7.0")
}

androidComponents.onVariants { variant ->
    variant.instrumentation.transformClassesWith(
        ClassVisitorFactory::class.java, InstrumentationScope.PROJECT
    ) {}
}

abstract class ClassVisitorFactory : AsmClassVisitorFactory<InstrumentationParameters.None> {
    override fun createClassVisitor(
        classContext: ClassContext,
        nextClassVisitor: ClassVisitor
    ): ClassVisitor {
        return ClassRemapper(nextClassVisitor, object : Remapper() {
            override fun map(name: String): String {
                if (name.startsWith("stub/")) {
                    return name.substring(name.indexOf('/') + 1)
                }
                return name
            }
        })
    }

    override fun isInstrumentable(classData: ClassData): Boolean {
        return classData.className.endsWith("ass")
    }
}

version = "1.0.2"
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
