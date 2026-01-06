@file:OptIn(ExperimentalWasmDsl::class)

import com.android.build.api.dsl.androidLibrary
import org.jetbrains.kotlin.gradle.ExperimentalWasmDsl
import org.jetbrains.kotlin.gradle.dsl.JvmTarget

plugins {
    alias(libs.plugins.androidKotlinMultiplatformLibrary)
    alias(libs.plugins.kotlinMultiplatform)
    alias(libs.plugins.composeMultiplatform)
    alias(libs.plugins.composeCompiler)
    alias(libs.plugins.maven.publish)
}

kotlin {
    androidLibrary {
        namespace = "org.kimplify.kurrency.core"
        compileSdk = libs.versions.compileSdk.get().toInt()
        minSdk = libs.versions.minSdk.get().toInt()

        withDeviceTest {
            instrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
            execution = "ANDROIDX_TEST_ORCHESTRATOR"
        }
        withHostTest {
            isIncludeAndroidResources = true
        }

        compilations.configureEach {
            compileTaskProvider.get().compilerOptions {
                jvmTarget.set(JvmTarget.valueOf(libs.versions.jvmVersion.get()))
            }
        }
    }

    jvm()

    wasmJs {
        browser()
        outputModuleName.set("Kurrency")
    }

    js(IR) {
        browser()
        nodejs()
    }

    listOf(
        iosX64(),
        iosArm64(),
        iosSimulatorArm64()
    ).forEach { iosTarget ->
        iosTarget.binaries.framework {
            baseName = "Kurrency"
            isStatic = true
        }
    }

    sourceSets {
        commonMain.dependencies {
            implementation(compose.runtime)
            implementation(compose.foundation)
            implementation(libs.cedar.logging)
        }

        val commonTest by getting {
            dependencies {
                implementation(libs.kotlin.test)
            }
        }

        val androidMain by getting {
            dependencies {
                implementation(libs.androidx.core.ktx)
            }
        }

        androidInstrumentedTest.dependencies {
            implementation(libs.androidx.testExt.junit)
            implementation(libs.androidx.runner)
            implementation(libs.androidx.rules)
            implementation(libs.kotlin.test)
        }

        iosMain.dependencies {}
    }
}

//Publishing your Kotlin Multiplatform library to Maven Central
//https://www.jetbrains.com/help/kotlin-multiplatform-dev/multiplatform-publish-libraries.html
mavenPublishing {
    publishToMavenCentral()
    signAllPublications()
    coordinates("org.kimplify", "kurrency-core", libs.versions.appVersionName.get())

    pom {
        name = "Kurrency"
        description =
            "A Kotlin Multiplatform library for currency formatting and handling across Android, iOS, JVM, and Web platforms"
        url = "https://github.com/ChiliNoodles/Kurrency"

        licenses {
            license {
                name = "Apache License, Version 2.0"
                url = "https://www.apache.org/licenses/LICENSE-2.0"
            }
        }

        developers {
            developer {
                id = "merkost"
                name = "Konstantin Merenkov"
                email = "merkostdev@gmail.com"
            }

            developer {
                id = "diogocavaiar"
                name = "Diogo Cavaiar"
                email = "cavaiarconsulting@gmail.com"
            }
        }

        scm {
            url = "https://github.com/ChiliNoodles/Kurrency"
        }
    }
}
