plugins {
    id("com.android.application")
    id("org.jetbrains.kotlin.android")
    id("org.jetbrains.kotlin.plugin.serialization")
    id("com.google.devtools.ksp")
    id("org.jetbrains.kotlin.plugin.compose")
    id("com.google.dagger.hilt.android")
}


android {
    namespace = "com.datavite.distrivite"
    compileSdk = 36

    defaultConfig {
        applicationId = "com.datavite.distrivite"
        minSdk = 26
        targetSdk = 35
        versionCode = 1
        versionName = "1.0.0"

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
        vectorDrawables {
            useSupportLibrary = true
        }
    }

    buildTypes {

        val production = "\"https://distrivite-b3825671fa5a.herokuapp.com/\""
        val development = "\"http://192.168.43.107:8001/\""

        val currentEnv = production

        getByName("debug") {
            buildConfigField(
                "String",
                "BASE_URL",
                currentEnv   // your debug backend
            )
        }
        getByName("release") {
            isMinifyEnabled = true
            isShrinkResources = true
            buildConfigField(
                "String",
                "BASE_URL",
                currentEnv      // your production backend
            )
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro"
            )
            ndk {
                debugSymbolLevel = "FULL"
            }
        }


    }
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_1_8
        targetCompatibility = JavaVersion.VERSION_1_8
    }
    kotlinOptions {
        jvmTarget = "1.8"
    }
    buildFeatures {
        compose = true
        buildConfig = true // Add this line
    }
    composeOptions {
        kotlinCompilerExtensionVersion = "1.8"
    }
    packaging {
        resources {
            excludes += "/META-INF/{AL2.0,LGPL2.1}"
        }
    }
    ndkVersion = "29.0.14033849 rc4"

}





dependencies {

    val navVersion = "2.9.0"
    val roomVersion = "2.7.2"
    val destinationVersion = "2.2.0"
    val hiltVersion = "2.56.2"
    val workVersion = "2.10.2"
    val lifecycle_version = "2.9.1"

    implementation("androidx.core:core-ktx:1.16.0")
    implementation("androidx.lifecycle:lifecycle-runtime-ktx:${lifecycle_version}")
    implementation("androidx.lifecycle:lifecycle-process:${lifecycle_version}")
    implementation("androidx.lifecycle:lifecycle-viewmodel-compose:${lifecycle_version}")

    implementation("androidx.activity:activity-compose:1.10.1")
    implementation(platform("androidx.compose:compose-bom:2025.06.01"))
    implementation("androidx.compose.animation:animation:1.8.3")
    implementation("androidx.compose.runtime:runtime-livedata")
    implementation("androidx.compose.ui:ui")
    implementation("androidx.compose.ui:ui-graphics")
    implementation("androidx.compose.ui:ui-tooling-preview")
    implementation("androidx.compose.material3:material3")
    implementation("androidx.compose.material3:material3-window-size-class")
    implementation("androidx.compose.material:material-icons-extended")
    //implementation("androidx.compose.material:material:1.7.3")
    implementation("io.coil-kt:coil-compose:2.7.0")

    // permissions for compose
    implementation("com.google.accompanist:accompanist-permissions:0.37.3")

    // The new compose webview
    implementation("io.github.kevinnzou:compose-webview:0.33.6")

    // compose navigation
    implementation("androidx.navigation:navigation-compose:$navVersion")

    // compose destination
    implementation("io.github.raamcosta.compose-destinations:core:$destinationVersion")
    ksp("io.github.raamcosta.compose-destinations:ksp:$destinationVersion")

    // datastore
    implementation("androidx.datastore:datastore-preferences:1.1.7")

    // Retrofit
    implementation("com.squareup.retrofit2:retrofit:3.0.0")
    implementation("org.jetbrains.kotlinx:kotlinx-serialization-json:1.9.0")
    implementation("com.jakewharton.retrofit:retrofit2-kotlinx-serialization-converter:1.0.0")

    // Ok Http
    implementation("com.squareup.okhttp3:okhttp:4.12.0")
    implementation("com.squareup.okhttp3:logging-interceptor:4.12.0")

    //Dagger - Hilt
    implementation("com.google.dagger:hilt-android:$hiltVersion")
    ksp("com.google.dagger:hilt-android-compiler:$hiltVersion") // ✅ Required for Hilt DI
    implementation("androidx.hilt:hilt-navigation-compose:1.2.0") // ✅ For @HiltViewModel with Compose Navigation
    implementation("androidx.hilt:hilt-work:1.2.0") // ✅ Needed for Hilt & WorkManager interop (for WorkerFactory binding)
    ksp("androidx.hilt:hilt-compiler:1.2.0")        // ✅ Annotation processor for androidx.hilt.* modules

    // WorkManager with Kotlin Coroutines support
    implementation("androidx.work:work-runtime-ktx:$workVersion")

    // MLKit Face Detection
    implementation("com.google.mlkit:face-detection:16.1.7")

    // DocumentFile and ExitInterface
    implementation("androidx.exifinterface:exifinterface:1.4.1")

    // TensorFlow Lite dependencies
    implementation("org.tensorflow:tensorflow-lite:2.14.0")
    implementation("org.tensorflow:tensorflow-lite-gpu:2.14.0")
    implementation("org.tensorflow:tensorflow-lite-task-vision:0.4.4")
    implementation("org.tensorflow:tensorflow-lite-gpu-delegate-plugin:0.4.4")
    implementation("org.tensorflow:tensorflow-lite-gpu-api:2.11.0")
    implementation("org.tensorflow:tensorflow-lite-support:0.4.4")

    // Gps Location dependency
    implementation("com.google.android.gms:play-services-location:21.3.0")

    // CameraX dependencies
    val cameraX = "1.4.0-beta02"
    implementation("androidx.camera:camera-camera2:$cameraX")
    implementation("androidx.camera:camera-core:$cameraX")
    implementation("androidx.camera:camera-lifecycle:$cameraX")
    implementation("androidx.camera:camera-view:$cameraX")
    implementation("androidx.camera:camera-mlkit-vision:${cameraX}")

    // room dependencies
    implementation("androidx.room:room-runtime:$roomVersion")
    annotationProcessor("androidx.room:room-compiler:$roomVersion")
    ksp("androidx.room:room-compiler:$roomVersion")
    implementation("androidx.room:room-ktx:$roomVersion")

    implementation("com.juul.kable:core:0.32.0") // or latest version
    implementation("com.juul.kable:kable-default-permissions:0.39.0")

    // application testing
    testImplementation("junit:junit:4.13.2")
    androidTestImplementation("androidx.test.ext:junit:1.2.1")
    androidTestImplementation("androidx.test.espresso:espresso-core:3.6.1")
    androidTestImplementation(platform("androidx.compose:compose-bom:2025.06.01"))
    androidTestImplementation("androidx.compose.ui:ui-test-junit4")
    debugImplementation("com.squareup.leakcanary:leakcanary-android:2.14")
    debugImplementation("androidx.compose.ui:ui-tooling")
    debugImplementation("androidx.compose.ui:ui-test-manifest")
}

