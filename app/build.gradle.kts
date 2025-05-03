import com.android.build.gradle.internal.cxx.configure.gradleLocalProperties

plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.kotlin.android)
    alias(libs.plugins.kotlin.compose)
    alias(libs.plugins.kotlin.ksp)
}


android {
    namespace = "com.apps.kunalfarmah.kpass"
    compileSdk = 35

    defaultConfig {
        applicationId = "com.apps.kunalfarmah.kpass"
        minSdk = 24
        targetSdk = 35
        versionCode = 6
        versionName = "1.0.6"
        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
        buildConfigField("String","KEY_MASTER", gradleLocalProperties(rootDir, providers).getProperty("KEY_MASTER"))
        buildConfigField("String","LOWERCASE_POOL", gradleLocalProperties(rootDir, providers).getProperty("LOWERCASE"))
        buildConfigField("String","UPPERCASE_POOL", gradleLocalProperties(rootDir, providers).getProperty("UPPERCASE"))
        buildConfigField("String","SPECIAL_POOL", gradleLocalProperties(rootDir, providers).getProperty("SPECIAL"))
        buildConfigField("String","DIGITS_POOL", gradleLocalProperties(rootDir, providers).getProperty("DIGITS"))
        buildConfigField("Integer","MIN_LOWER", gradleLocalProperties(rootDir, providers).getProperty("MIN_LOWER"))
        buildConfigField("Integer","MIN_UPPER", gradleLocalProperties(rootDir, providers).getProperty("MIN_UPPER"))
        buildConfigField("Integer","MIN_SPECIAL", gradleLocalProperties(rootDir, providers).getProperty("MIN_SPECIAL"))
        buildConfigField("Integer","MIN_DIGITS", gradleLocalProperties(rootDir, providers).getProperty("MIN_DIGITS"))
        buildConfigField("Integer","MANDATORY_CHARS", gradleLocalProperties(rootDir, providers).getProperty("MANDATORY_CHARS"))
    }

    sourceSets {
        getByName("main").res.srcDirs("src/main/res")
        getByName("debug").res.srcDirs("src/debug/res")
    }


    buildTypes {
        release {
            isMinifyEnabled = true
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
    buildFeatures {
        compose = true
        buildConfig = true
    }
}

dependencies {

    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.lifecycle.runtime.ktx)
    implementation(libs.androidx.activity.compose)
    implementation(platform(libs.androidx.compose.bom))
    implementation(libs.androidx.ui)
    implementation(libs.androidx.ui.graphics)
    implementation(libs.androidx.ui.tooling.preview)
    implementation(libs.androidx.material3)
    implementation(libs.biometric)
    implementation(libs.androidx.appcompat)
    implementation(libs.koin.android)
    implementation(libs.itext.pdf)
    implementation(libs.itext.bouncycastle.adapter)
    implementation(libs.datastore.preferences)
    implementation(libs.splashscreen)
    implementation(libs.slf4j)
    implementation(libs.room)
    implementation(libs.room.ktx)
    implementation(libs.lifecycle.runtime.compose)
    ksp(libs.room.compiler)

    testImplementation(libs.junit)
    androidTestImplementation(libs.androidx.junit)
    androidTestImplementation(libs.androidx.espresso.core)
    androidTestImplementation(platform(libs.androidx.compose.bom))
    androidTestImplementation(libs.androidx.ui.test.junit4)
    debugImplementation(libs.androidx.ui.tooling)
    debugImplementation(libs.androidx.ui.test.manifest)
}