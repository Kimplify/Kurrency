@file:OptIn(ExperimentalWasmDsl::class)

import org.jetbrains.kotlin.gradle.ExperimentalWasmDsl
import org.jetbrains.kotlin.gradle.dsl.JvmTarget

plugins {
    alias(libs.plugins.androidKotlinMultiplatformLibrary)
    alias(libs.plugins.kotlinMultiplatform)
    alias(libs.plugins.maven.publish)
}

kotlin {
    androidLibrary {
        namespace = "org.kimplify.kurrency.deci"
        compileSdk = libs.versions.compileSdk.get().toInt()
        minSdk = libs.versions.minSdk.get().toInt()

        compilations.configureEach {
            compileTaskProvider.get().compilerOptions {
                jvmTarget.set(JvmTarget.valueOf(libs.versions.jvmVersion.get()))
            }
        }
    }

    jvm()

    wasmJs {
        browser()
        outputModuleName.set("Kurrency-Deci")
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
            baseName = "KurrencyDeci"
            isStatic = true
        }
    }

    sourceSets {
        val commonMain by getting {
            dependencies {
                api(project(":kurrency-core"))
                api(libs.deci)
            }
        }

        val commonTest by getting {
            dependencies {
                implementation(libs.kotlin.test)
            }
        }
    }
}

mavenPublishing {
    publishToMavenCentral()
    signAllPublications()
    coordinates("org.kimplify", "kurrency-deci", libs.versions.appVersionName.get())

    pom {
        name = "Kurrency Deci"
        description = "Deci decimal arithmetic extensions for Kurrency currency formatting library"
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
