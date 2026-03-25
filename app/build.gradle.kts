plugins {
    id("com.android.application")
    id("org.jetbrains.kotlin.android")
    id("org.jetbrains.kotlin.plugin.compose")
    id("com.google.gms.google-services")
}

android {
    namespace = "com.example.breedclassifier"
<<<<<<< HEAD
    compileSdk = 35
=======
    compileSdk = 36
>>>>>>> 3c4abcf825878a9490a84235823a39ad000342a7

    defaultConfig {
        applicationId = "com.example.breedclassifier"
        minSdk = 24
        targetSdk = 36
        versionCode = 1
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

<<<<<<< HEAD
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
=======
    composeOptions {
        kotlinCompilerExtensionVersion = "1.5.14"
>>>>>>> 3c4abcf825878a9490a84235823a39ad000342a7
    }
}

dependencies {

    // ✅ Core
    implementation("androidx.core:core-ktx:1.15.0")
    implementation("androidx.lifecycle:lifecycle-runtime-ktx:2.8.7")

    implementation("androidx.compose.material3:material3")
    implementation("com.google.android.material:material:1.12.0")

    // ✅ FIXED (REQUIRED)
    implementation("androidx.activity:activity-compose:1.9.3")

    // ✅ Compose BOM
    implementation(platform("androidx.compose:compose-bom:2024.06.00"))

    // Compose UI
    implementation("androidx.compose.ui:ui")
    implementation("androidx.compose.ui:ui-tooling-preview")
    implementation("androidx.compose.ui:ui-graphics")

    // Material 3
    implementation("androidx.compose.material3:material3")
    implementation("androidx.compose.material:material-icons-extended")

<<<<<<< HEAD
    // TensorFlow Lite
    // Using 2.16.1 consistently as 2.17.0 is not available for all components (e.g. select-tf-ops)
    //implementation("org.tensorflow:tensorflow-lite:2.16.1")
    // Using 0.4.4 to avoid duplicate class conflicts with LiteRT which are present in 0.5.0
   // implementation("org.tensorflow:tensorflow-lite-support:0.4.4")
    //implementation("org.tensorflow:tensorflow-lite-select-tf-ops:2.16.1")
    //implementation("org.tensorflow:tensorflow-lite-gpu-delegate-plugin:2.16.1")
    implementation("org.tensorflow:tensorflow-lite:2.16.1")
    implementation("org.tensorflow:tensorflow-lite-select-tf-ops:2.16.1")
    implementation("org.tensorflow:tensorflow-lite-gpu:2.16.1")


    // Material and UI
    implementation("androidx.appcompat:appcompat:1.7.0")
    implementation("com.google.android.material:material:1.12.0")
    implementation("androidx.constraintlayout:constraintlayout:2.1.4")
    implementation("com.google.code.gson:gson:2.10.1")

    // Testing
    testImplementation("junit:junit:4.13.2")
    androidTestImplementation("androidx.test.ext:junit:1.2.1")
    androidTestImplementation("androidx.test.espresso:espresso-core:3.6.1")
    androidTestImplementation(platform("androidx.compose:compose-bom:2024.06.00"))
    androidTestImplementation("androidx.compose.ui:ui-test-junit4")
    debugImplementation("androidx.compose.ui:ui-tooling")
    debugImplementation("androidx.compose.ui:ui-test-manifest")
}
configurations.all {
    exclude(group = "com.google.ai.edge.litert")
=======
    // ✅ FIX ICONS
    implementation("androidx.compose.material:material-icons-extended")

    // Firebase
    implementation("com.google.firebase:firebase-auth:22.3.0")

    // TensorFlow
    implementation("org.tensorflow:tensorflow-lite:2.14.0")
    implementation("org.tensorflow:tensorflow-lite-task-vision:0.4.4")

    // Gson
    implementation("com.google.code.gson:gson:2.10.1")
    implementation(libs.androidx.credentials)
    implementation(libs.androidx.credentials.play.services.auth)
    implementation(libs.googleid)
    implementation(libs.firebase.inappmessaging.display)
>>>>>>> 3c4abcf825878a9490a84235823a39ad000342a7
}