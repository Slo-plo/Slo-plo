import java.io.FileInputStream
import java.util.Properties

plugins {
//    alias(libs.plugins.android.application)
//    alias(libs.plugins.kotlin.android)
//    id("com.android.application")
    id("com.android.application")
    alias(libs.plugins.kotlin.android)
    id("com.google.gms.google-services")
}

var properties = Properties()
properties.load(FileInputStream("local.properties"))

android {
    namespace = "com.example.slo_plo"
    compileSdk = 35

    defaultConfig {
        applicationId = "com.example.slo_plo"
        minSdk = 24
        targetSdk = 34
        versionCode = 1
        versionName = "1.0"

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"

        buildConfigField("String", "NAVER_CLIENT_ID", "\"${properties.getProperty("NAVER_CLIENT_ID")}\"")
        buildConfigField("String", "NAVER_GEO_CLIENT_ID", "\"${properties.getProperty("NAVER_GEO_CLIENT_ID")}\"")
        buildConfigField("String", "NAVER_GEO_CLIENT_SECRET", "\"${properties.getProperty("NAVER_GEO_CLIENT_SECRET")}\"")
//        buildConfigField("String", "NAVER_CLIENT_ID", "\"${properties.getProperty("NAER_CLIENT_ID") ?: ""}\"")

        val imgbbKey: String = project.findProperty("imgbb.api.key") as String? ?: ""
        buildConfigField("String", "IMGBB_API_KEY", "\"$imgbbKey\"")
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
        sourceCompatibility = JavaVersion.VERSION_1_8
        targetCompatibility = JavaVersion.VERSION_1_8
        isCoreLibraryDesugaringEnabled = true
    }
    kotlinOptions {
        jvmTarget = "1.8"
    }
    buildFeatures {
        viewBinding = true
        buildConfig = true
    }
}

dependencies {

    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.appcompat)
    implementation(libs.material)
    implementation(libs.androidx.activity)
    implementation(libs.androidx.constraintlayout)
    implementation(libs.androidx.navigation.fragment)
    implementation(libs.androidx.navigation.ui.ktx)
    implementation(libs.androidx.fragment)
    implementation(libs.map.sdk)
    implementation(libs.play.services.location)
    implementation(libs.places)
    implementation(libs.androidx.core.i18n)
    implementation(libs.androidx.ui.text.android)
    implementation(libs.androidx.navigation.fragment.ktx)
    testImplementation(libs.junit)
    androidTestImplementation(libs.androidx.junit)
    androidTestImplementation(libs.androidx.espresso.core)
    coreLibraryDesugaring(libs.core.desugaring)
    implementation(libs.kotlin.stdlib)
    implementation ("com.kizitonwose.calendar:view:2.6.2")
    implementation("com.google.firebase:firebase-firestore-ktx")
    implementation(platform("com.google.firebase:firebase-bom:33.13.0"))
    implementation("com.google.firebase:firebase-auth")
    implementation("com.squareup.okhttp3:okhttp:4.12.0")



}
