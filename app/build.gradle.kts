import org.jetbrains.kotlin.config.KotlinCompilerVersion

plugins {
    id("com.android.application")
    kotlin("android")
}

android {
    namespace = "com.ifarbod.oneui_detect"
    compileSdk = 33
    buildToolsVersion = "33.0.1"
    ndkVersion = "25.1.8937393"

    defaultConfig {
        applicationId = "com.ifarbod.oneui_detect"
        minSdk = 16
        targetSdk = 33
        versionCode = 1
        versionName = "1.0"
        testInstrumentationRunner = "android.support.test.runner.AndroidJUnitRunner"
    }

    buildTypes {
        getByName("debug") {
            applicationIdSuffix = ".debug"
            versionNameSuffix = "-dev"
        }

        getByName("release") {
            isMinifyEnabled = false
            proguardFiles(getDefaultProguardFile("proguard-android.txt"), "proguard-rules.pro")
        }
    }

    val javaVersion = JavaVersion.VERSION_11

    compileOptions {
        sourceCompatibility = javaVersion
        targetCompatibility = javaVersion
    }
    kotlinOptions {
        jvmTarget = javaVersion.toString()
        freeCompilerArgs += "-opt-in=kotlin.RequiresOptIn"
    }

}

dependencies {
    implementation(fileTree(mapOf("dir" to "libs", "include" to listOf("*.jar"))))
    implementation(kotlin("stdlib", KotlinCompilerVersion.VERSION))
    testImplementation("junit:junit:4.13.2")
    androidTestImplementation("androidx.test:runner:1.5.1")
    androidTestImplementation("androidx.test.espresso:espresso-core:3.5.0")
}
