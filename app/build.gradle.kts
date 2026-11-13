plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.kotlin.compose)
}

android {
    namespace = "com.example.induzteckapp"
    compileSdk {
        version = release(37)
    }

    defaultConfig {
        applicationId = "com.example.induzteckapp"
        minSdk = 24
        targetSdk = 37
        versionCode = 1
        versionName = "1.0"

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
    }

    buildTypes {
        release {
            optimization {
                enable = true
                packageScope = setOf("androidx.**", "kotlin.**", "kotlinx.**")
            }
        }
    }
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_11
        targetCompatibility = JavaVersion.VERSION_11
    }
    buildFeatures {
        compose = true
    }
}

dependencies {
    implementation(platform(libs.androidx.compose.bom))
    implementation(libs.androidx.activity.compose)
    implementation(libs.androidx.compose.material3)
    implementation(libs.androidx.compose.ui)
    implementation(libs.androidx.compose.ui.graphics)
    implementation(libs.androidx.compose.ui.tooling.preview)
    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.lifecycle.runtime.ktx)
    testImplementation(libs.junit)
    androidTestImplementation(platform(libs.androidx.compose.bom))
    androidTestImplementation(libs.androidx.compose.ui.test.junit4)
    androidTestImplementation(libs.androidx.espresso.core)
    androidTestImplementation(libs.androidx.junit)
    debugImplementation(libs.androidx.compose.ui.test.manifest)
    debugImplementation(libs.androidx.compose.ui.tooling)

    //Librerias para el ciclo de vida del viewmodel
    implementation("androidx.lifecycle:lifecycle-viewmodel-compose:2.6.1")
    implementation("androidx.lifecycle:lifecycle-runtime-compose:2.6.1")

    // Comunicación con el microservicio de Usuarios.
    implementation("com.squareup.retrofit2:retrofit:3.0.0")
    implementation("com.squareup.retrofit2:converter-gson:3.0.0")
    implementation("com.squareup.okhttp3:okhttp:4.12.0")

    // Corrutinas y ViewModel para llamadas de red.
    implementation("androidx.lifecycle:lifecycle-viewmodel-ktx:2.6.1")

    // Carga de imágenes remotas (cuando el backend exponga avatarUrl).
    implementation("io.coil-kt.coil3:coil-compose:3.4.0")
    implementation("io.coil-kt.coil3:coil-network-okhttp:3.4.0")

    // NUEVO: Para la navegación
    implementation("androidx.navigation:navigation-compose:2.10.2")
}