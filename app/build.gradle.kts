//plugins {
//    id("com.android.application")
//    id("org.jetbrains.kotlin.android")
//    id("com.google.gms.google-services")
//}
//
//android {
//    namespace = "com.breeddetect.ai"
//    compileSdk = 36
//
//    defaultConfig {
//        applicationId = "com.breeddetect.ai"
//        minSdk = 24
//        targetSdk = 36
//        versionCode = 2
//        versionName = "1.0"
//    }
//
//    buildTypes {
//        release {
//            isMinifyEnabled = false
//        }
//    }
//
//    compileOptions {
//        sourceCompatibility = JavaVersion.VERSION_17
//        targetCompatibility = JavaVersion.VERSION_17
//    }
//
//    kotlinOptions {
//        jvmTarget = "17"
//    }
//
//    buildFeatures {
//        compose = true
//    }
//
//    // ✅ FIXED: moved outside packaging
//    composeOptions {
//        kotlinCompilerExtensionVersion = "1.5.14"
//    }
//
//    aaptOptions {
//        noCompress("tflite")
//    }
//
//    packaging {
//        jniLibs {
//            useLegacyPackaging = true
//            pickFirsts.add("**/libtensorflowlite_jni.so")
//            pickFirsts.add("**/libtensorflowlite_flex_jni.so")
//        }
//        resources {
//            excludes += "/META-INF/{AL2.0,LGPL2.1}"
//        }
//    }
//}
//
//dependencies {
//
//    // Core
//    implementation("androidx.core:core-ktx:1.15.0")
//    implementation("androidx.lifecycle:lifecycle-runtime-ktx:2.8.7")
//
//    // Compose
//    implementation(platform("androidx.compose:compose-bom:2024.06.00"))
//    implementation("androidx.activity:activity-compose:1.9.3")
//    implementation("androidx.compose.ui:ui")
//    implementation("androidx.compose.ui:ui-tooling-preview")
//    implementation("androidx.compose.ui:ui-graphics")
//    implementation("androidx.compose.material3:material3")
//    implementation("androidx.compose.material:material-icons-extended")
//
//    // Material UI
//    implementation("com.google.android.material:material:1.12.0")
//    implementation("androidx.appcompat:appcompat:1.7.0")
//    implementation("androidx.constraintlayout:constraintlayout:2.1.4")
//
//    // TensorFlow (clean & consistent)
//    implementation("org.tensorflow:tensorflow-lite:2.16.1")
//    implementation("org.tensorflow:tensorflow-lite-select-tf-ops:2.16.1")
//    implementation("org.tensorflow:tensorflow-lite-gpu:2.16.1")
//
//    // Firebase
//    implementation("com.google.firebase:firebase-auth:22.3.0")
//
//    // Gson
//    implementation("com.google.code.gson:gson:2.10.1")
//    implementation(libs.androidx.credentials)
//    implementation(libs.androidx.credentials.play.services.auth)
//    implementation(libs.googleid)
//
//    // Testing
//    testImplementation("junit:junit:4.13.2")
//    androidTestImplementation("androidx.test.ext:junit:1.2.1")
//    androidTestImplementation("androidx.test.espresso:espresso-core:3.6.1")
//    androidTestImplementation(platform("androidx.compose:compose-bom:2024.06.00"))
//    androidTestImplementation("androidx.compose.ui:ui-test-junit4")
//
//    debugImplementation("androidx.compose.ui:ui-tooling")
//    debugImplementation("androidx.compose.ui:ui-test-manifest")
//}
//
//configurations.all {
//    exclude(group = "com.google.ai.edge.litert")
//}

plugins {
    id("com.android.application")
    id("org.jetbrains.kotlin.android")
}

android {
    namespace = "com.breeddetect.ai"
    compileSdk = 36

    defaultConfig {
        applicationId = "com.breeddetect.ai"
        minSdk = 24
        targetSdk = 36
        versionCode = 2
        versionName = "1.0"
    }

    buildTypes {
        release {
            isMinifyEnabled = false
        }
    }

    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
    }

    kotlinOptions {
        jvmTarget = "17"
    }

    buildFeatures {
        compose = true
    }

    composeOptions {
        kotlinCompilerExtensionVersion = "1.5.14"
    }

    aaptOptions {
        noCompress("tflite")
    }

    packaging {
        jniLibs {
            useLegacyPackaging = true
            pickFirsts.add("**/libtensorflowlite_jni.so")
            pickFirsts.add("**/libtensorflowlite_flex_jni.so")
        }
        resources {
            excludes += "/META-INF/{AL2.0,LGPL2.1}"
        }
    }
}

dependencies {

    implementation("androidx.core:core-ktx:1.15.0")
    implementation("androidx.lifecycle:lifecycle-runtime-ktx:2.8.7")

    implementation(platform("androidx.compose:compose-bom:2024.06.00"))
    implementation("androidx.activity:activity-compose:1.9.3")
    implementation("androidx.compose.ui:ui")
    implementation("androidx.compose.ui:ui-tooling-preview")
    implementation("androidx.compose.ui:ui-graphics")
    implementation("androidx.compose.material3:material3")
    implementation("androidx.compose.material:material-icons-extended")

    implementation("com.google.android.material:material:1.12.0")
    implementation("androidx.appcompat:appcompat:1.7.0")
    implementation("androidx.constraintlayout:constraintlayout:2.1.4")

    implementation("org.tensorflow:tensorflow-lite:2.16.1")
    implementation("org.tensorflow:tensorflow-lite-select-tf-ops:2.16.1")
    implementation("org.tensorflow:tensorflow-lite-gpu:2.16.1")

    implementation("com.google.code.gson:gson:2.10.1")

    testImplementation("junit:junit:4.13.2")
    androidTestImplementation("androidx.test.ext:junit:1.2.1")
    androidTestImplementation("androidx.test.espresso:espresso-core:3.6.1")

    debugImplementation("androidx.compose.ui:ui-tooling")
}

configurations.all {
    exclude(group = "com.google.ai.edge.litert")
}