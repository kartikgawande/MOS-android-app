plugins {
    alias(libs.plugins.androidApplication)
}

android {
    namespace = "com.example.mos"
    compileSdk = 34

    defaultConfig {
        applicationId = "com.example.mos"
        minSdk = 29
        targetSdk = 34
        versionCode = 1
        versionName = "1.0"

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
    }

    buildTypes {
        release {
            isMinifyEnabled = false
            proguardFiles(getDefaultProguardFile("proguard-android-optimize.txt"), "proguard-rules.pro")
        }
    }
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_1_8
        targetCompatibility = JavaVersion.VERSION_1_8
    }

    // I added this to resolve "2 files found with path 'META-INF/DEPENDENCIES'." error while building apk
    packaging {
        resources.excludes.add("META-INF/DEPENDENCIES")
    }
    buildFeatures {
        viewBinding = true
    }
}

dependencies {
    implementation(libs.appcompat)
    implementation(libs.material)
    implementation(libs.activity)
    implementation(libs.constraintlayout)
    implementation(libs.lifecycle.livedata.ktx)
    implementation(libs.lifecycle.viewmodel.ktx)
    implementation(libs.navigation.fragment)
    implementation(libs.navigation.ui)
    testImplementation(libs.junit)
    androidTestImplementation(libs.ext.junit)
    androidTestImplementation(libs.espresso.core)
    implementation(libs.googlesignin)
    implementation(libs.googledriveapi)
    implementation(libs.googlegson)
    implementation(libs.googledriveservices)
}