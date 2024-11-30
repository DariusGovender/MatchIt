plugins {
    id("com.google.devtools.ksp") version "1.9.0-1.0.13"
    alias(libs.plugins.android.application)

    alias(libs.plugins.kotlin.android)
    id("com.google.gms.google-services")


}

android {
    namespace = "com.example.matchgame"
    compileSdk = 35

    defaultConfig {
        applicationId = "com.example.matchgame"
        minSdk = 31
        targetSdk = 34
        versionCode = 1
        versionName = "1.0"

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
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

    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.appcompat)
    implementation(libs.material)
    implementation(libs.androidx.activity)
    implementation(libs.androidx.constraintlayout)
    implementation(libs.androidx.runtime.saved.instance.state)
    implementation(libs.google.firebase.storage.ktx)
    implementation(libs.firebase.firestore.ktx)
    implementation(libs.firebase.messaging)
    implementation(libs.play.services.ads.lite)
    implementation(libs.androidx.room.common)
    implementation(libs.androidx.room.ktx)
    ksp(libs.room.compiler)
    testImplementation(libs.junit)
    androidTestImplementation(libs.androidx.junit)
    androidTestImplementation(libs.androidx.espresso.core)
    implementation(platform(libs.firebase.bom))
    implementation(libs.firebase.auth.ktx)
    implementation(libs.google.signin)
    implementation(libs.picasso)
    implementation(libs.facebook.android.sdk)
    implementation(libs.retrofit)
    implementation(libs.converter.gson)
    implementation (libs.confetti)
    implementation(libs.androidx.biometric)
    implementation(libs.play.services.ads)

}