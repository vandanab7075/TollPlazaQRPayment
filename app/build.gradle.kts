plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.kotlin.android)
    alias(libs.plugins.google.gms.google.services)

}



android {
    namespace = "com.example.tollplazaqrpayment"
    compileSdk = 34

    defaultConfig {
        applicationId = "com.example.tollplazaqrpayment"
        minSdk = 26
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
        sourceCompatibility = JavaVersion.VERSION_1_8
        targetCompatibility = JavaVersion.VERSION_1_8
    }
    kotlinOptions {
        jvmTarget = "1.8"
    }
}

dependencies {

    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.appcompat)
    implementation(libs.material)
    implementation(libs.androidx.activity)
    implementation(libs.androidx.constraintlayout)
    implementation(libs.firebase.messaging)
    implementation(libs.firebase.firestore)
    implementation(libs.transportation.consumer)
    testImplementation(libs.junit)
    androidTestImplementation(libs.androidx.junit)
    androidTestImplementation(libs.androidx.espresso.core)
    // Firebase
    implementation(libs.firebase.bom)
    implementation(libs.firebase.auth.ktx)
    implementation(libs.firebase.firestore.ktx)

//  for QR Code generation and scanning
    implementation(libs.core)
    implementation(libs.zxing.android.embedded)

// Payment Gateway (Example: Razorpay)
    implementation(libs.checkout)

// For image loading (optional, for QR code display)
    implementation(libs.glide)
    implementation(libs.firebase.auth)
    implementation(libs.androidx.appcompat.v131)
    implementation(libs.material.v140)
    implementation(libs.zxing.android.embedded)
    implementation(libs.core.v341)
    implementation(libs.checkout.v1619)
    implementation(libs.google.maps.services)

}



