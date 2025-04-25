plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.google.gms.google.services)
}

android {
    namespace = "com.example.goceryforproj"
    compileSdk = 34

    defaultConfig {
        applicationId = "com.example.goceryforproj"
        minSdk = 24
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
}

dependencies {
    androidTestImplementation("androidx.test.espresso:espresso-core:3.5.1")
    androidTestImplementation("androidx.test.espresso:espresso-intents:3.5.1")
    androidTestImplementation("androidx.test.ext:junit:1.1.5")
    androidTestImplementation("androidx.test:rules:1.5.0")
    // Firebase BoM
    implementation(platform("com.google.firebase:firebase-bom:32.7.4"))

    // Firebase dependencies without versions (managed by BoM)
    implementation("com.google.firebase:firebase-common")
    implementation("com.google.firebase:firebase-auth")
    implementation("com.google.firebase:firebase-firestore")
    implementation("com.google.firebase:firebase-analytics")
    implementation("com.google.firebase:firebase-database")

    // Other dependencies
    implementation("com.google.android.gms:play-services-auth:20.7.0")
    implementation("com.github.bumptech.glide:glide:4.12.0")
    implementation(libs.appcompat)
    implementation(libs.material)
    // Testing dependencies
    testImplementation(libs.junit)
    testImplementation("org.mockito:mockito-android:5.7.0")
    testImplementation("org.mockito:mockito-core:5.7.0")
    androidTestImplementation(libs.ext.junit)
    androidTestImplementation(libs.espresso.core)

    implementation("com.google.mlkit:barcode-scanning:17.2.0")
    implementation("androidx.camera:camera-core:1.3.2")
    implementation("androidx.camera:camera-camera2:1.3.2")
    implementation("androidx.camera:camera-lifecycle:1.3.2")
    implementation("androidx.camera:camera-view:1.3.2")
//added dependencies
}
