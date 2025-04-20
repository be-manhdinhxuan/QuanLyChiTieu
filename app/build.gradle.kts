plugins {
    id("com.android.application")
    id("com.google.gms.google-services")
    // Thêm plugin Kotlin nếu bạn muốn sử dụng Kotlin
    // id("org.jetbrains.kotlin.android")
}

android {
    namespace = "com.example.quanlychitieu"
    compileSdk = 34

    defaultConfig {
        applicationId = "com.example.quanlychitieu"
        minSdk = 24
        targetSdk = 34
        versionCode = 1
        versionName = "1.0"

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
    }

    buildTypes {
        release {
            isMinifyEnabled = false
            proguardFiles(getDefaultProguardFile("proguard-android-optimize.txt"), "proguard-rules.pro")
        }
    }

    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_1_8
        targetCompatibility = JavaVersion.VERSION_1_8
    }

    // Thêm cấu hình test
    testOptions {
        unitTests.all {
            it.useJUnitPlatform()
        }
    }

    buildFeatures {
        viewBinding = true
    }
}

dependencies {
    // Architecture Components
    implementation("androidx.lifecycle:lifecycle-viewmodel:2.7.0")
    implementation("androidx.lifecycle:lifecycle-livedata:2.7.0")
    implementation("androidx.lifecycle:lifecycle-runtime:2.7.0")

    // Firebase
    implementation(platform("com.google.firebase:firebase-bom:32.7.1"))
    implementation("com.google.firebase:firebase-auth")
    implementation("com.google.firebase:firebase-firestore")
    implementation("com.google.firebase:firebase-analytics")
    implementation("com.google.firebase:firebase-storage:20.3.0")

    // Google Sign In
    implementation("com.google.android.gms:play-services-auth:20.7.0")
    implementation("com.google.android.gms:play-services-base:18.3.0")

    // Facebook Login
    implementation("com.facebook.android:facebook-android-sdk:16.3.0")
    implementation("com.facebook.android:facebook-login:16.3.0")

    // AndroidX
    implementation("androidx.appcompat:appcompat:1.6.1")
    implementation("androidx.core:core-ktx:1.12.0")  // Thêm nếu dùng Kotlin
    implementation("com.google.android.material:material:1.11.0")
    implementation("androidx.constraintlayout:constraintlayout:2.1.4")

    // Add MPAndroidChart library
    implementation("com.github.PhilJay:MPAndroidChart:v3.1.0")

    // Network libraries
    implementation("com.squareup.okhttp3:okhttp:4.12.0")
    implementation("com.squareup.retrofit2:retrofit:2.9.0")  // Thêm Retrofit
    implementation("com.squareup.retrofit2:converter-gson:2.9.0")  // Thêm Retrofit Gson Converter
    implementation("com.google.code.gson:gson:2.10.1")

    // Dependency Injection
    implementation("io.insert-koin:koin-android:3.5.3")  // Thêm Koin cho DI

    // Testing
    testImplementation("junit:junit:4.13.2")
    testImplementation("org.mockito:mockito-core:5.10.0")  // Thêm Mockito
    testImplementation("io.mockk:mockk:1.13.9")  // Thêm MockK nếu dùng Kotlin
    androidTestImplementation("androidx.test.ext:junit:1.1.5")
    androidTestImplementation("androidx.test.espresso:espresso-core:3.5.1")

    // Add FlexboxLayout
    implementation("com.google.android.flexbox:flexbox:3.0.0")

    // Alternatively, you can use this library for calendar functionality
    implementation("com.github.prolificinteractive:material-calendarview:2.0.1")
}
