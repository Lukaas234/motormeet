plugins {
    id("com.android.application")
    id("org.jetbrains.kotlin.android")
    id("org.jetbrains.kotlin.multiplatform")
    id("org.jetbrains.kotlin.plugin.serialization")
    id("com.google.devtools.ksp")
    id("org.jetbrains.compose")
}

kotlin {
    androidTarget()
    iosTarget()

    sourceSets {
        val commonMain by getting {
            dependencies {
                // Kotlin stdlib & serialization
                implementation("org.jetbrains.kotlinx:kotlinx-serialization-json:1.7.3")
                implementation("org.jetbrains.kotlinx:kotlinx-coroutines-core:1.9.0")

                // Compose Multiplatform
                implementation("org.jetbrains.compose.ui:ui:1.7.3")
                implementation("org.jetbrains.compose.material:material:1.7.3")
                implementation("org.jetbrains.compose.foundation:foundation:1.7.3")
                implementation("org.jetbrains.compose.material:material-icons-extended:1.7.3")

                // Ktor HTTP client (reemplaza Appwrite Android SDK)
                implementation("io.ktor:ktor-client-core:2.3.7")
                implementation("io.ktor:ktor-client-content-negotiation:2.3.7")
                implementation("io.ktor:ktor-serialization-kotlinx-json:2.3.7")

                // Multiplatform Settings (reemplaza SharedPreferences)
                implementation("com.russhwolf:multiplatform-settings:0.13.0")

                // Koin DI (reemplaza Hilt)
                implementation("io.insert-koin:koin-core:3.5.3")
                implementation("io.insert-koin:koin-androidx-compose:3.5.3")
            }
        }
        val commonTest by getting {
            dependencies {
                implementation("org.jetbrains.kotlinx:kotlinx-coroutines-test:1.9.0")
            }
        }

        val androidMain by getting {
            dependsOn(commonMain)
            // AndroidX
            implementation("androidx.core:core-ktx:1.13.1")
            implementation("androidx.lifecycle:lifecycle-runtime-ktx:2.8.7")
            implementation("androidx.activity:activity-compose:1.9.3")
            implementation("androidx.tracing:tracing:2.0.5")

            // Navigation Compose (Android)
            implementation("androidx.navigation:navigation-compose:2.8.3")

            // Ktor client for Android
            implementation("io.ktor:ktor-client-android:2.3.7")

            // Coil for images (Android)
            implementation("io.coil-kt:coil-compose:2.7.0")

            // Google Maps (Android only)
            implementation("com.google.android.gms:play-services-maps:19.0.0")
            implementation("com.google.android.gms:play-services-location:21.3.0")
            implementation("com.google.android.libraries.places:places:4.1.0")
            implementation("com.google.maps.android:maps-compose:6.1.0")

            // Koin Android
            implementation("io.insert-koin:koin-android:3.5.3")

            // Appwrite (Android only, hasta que se migre a Ktor)
            implementation("io.appwrite:sdk-for-android:6.0.0")

            // Datastore
            implementation("androidx.datastore:datastore-preferences:1.1.1")
        }
        val iosMain by getting {
            dependsOn(commonMain)
            // Ktor client for iOS
            implementation("io.ktor:ktor-client-darwin:2.3.7")
        }
        val androidUnitTest by getting {
            dependsOn(commonTest)
            implementation("junit:junit:4.13.2")
            implementation("org.jetbrains.kotlinx:kotlinx-coroutines-test:1.9.0")
        }
    }
}

android {
    namespace = "com.motormeet.uruguay"
    compileSdk = 35

    defaultConfig {
        applicationId = "com.motormeet.uruguay"
        minSdk = 26
        targetSdk = 35
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
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
    }

    kotlinOptions {
        jvmTarget = "17"
    }

    buildFeatures {
        buildConfig = false
        compose = true
    }

    packaging {
        resources {
            excludes += "/META-INF/{AL2.0,LGPL2.1}"
        }
    }

    lint {
        abortOnError = false
    }
}

dependencies {
    // Test
    testImplementation("junit:junit:4.13.2")
    androidTestImplementation("androidx.test.ext:junit:1.2.1")
    androidTestImplementation("androidx.test.espresso:espresso-core:3.6.1")
    androidTestImplementation(platform("androidx.compose:compose-bom:2024.10.00"))
    androidTestImplementation("androidx.compose.ui:ui-test-junit4")
    debugImplementation("androidx.compose.ui:ui-tooling")
    debugImplementation("androidx.compose.ui:ui-test-manifest")
}
