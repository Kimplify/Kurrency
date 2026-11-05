plugins {
    alias(libs.plugins.androidApplication)
    alias(libs.plugins.composeCompiler)
}

android {
    namespace = "org.kimplify.sample"
    compileSdk = libs.versions.compileSdk.get().toInt()

    defaultConfig {
        minSdk = libs.versions.minSdk.get().toInt()
        targetSdk = libs.versions.targetSdk.get().toInt()
        applicationId = "org.kimplify.sample"
        versionCode = 1
        versionName = "1.0.0"
        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
    }

    buildFeatures {
        compose = true
    }
}

dependencies {
    implementation(project(":sample:shared"))
    implementation(libs.androidx.activity.compose)
    debugImplementation("androidx.compose.ui:ui-tooling:1.9.0-alpha04")
    implementation("androidx.compose.ui:ui-tooling-preview:1.9.0-alpha04")
    androidTestImplementation("androidx.compose.ui:ui-test-junit4:1.11.2")
    androidTestImplementation(libs.androidx.runner)
    androidTestImplementation(libs.androidx.testExt.junit)
}
