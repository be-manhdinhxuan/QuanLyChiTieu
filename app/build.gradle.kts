plugins {
    id("com.android.application")
    id("com.google.gms.google-services")
    id("com.google.dagger.hilt.android")
    id("dagger.hilt.android.plugin")
    alias(libs.plugins.google.android.libraries.mapsplatform.secrets.gradle.plugin)

// Thêm plugin Kotlin nếu bạn muốn sử dụng Kotlin
    // id("org.jetbrains.kotlin.android")
}

android {
    namespace = "com.example.quanlychitieu"
    compileSdk = 34

    // Cách cấu hình aaptOptions đúng
    androidResources {
        noCompress += listOf("") // Disable resource compression
    }

    defaultConfig {
        applicationId = "com.example.quanlychitieu"
        minSdk = 24
        targetSdk = 34
        versionCode = 1
        versionName = "1.0.0"
        
        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"

        javaCompileOptions {
            annotationProcessorOptions {
                arguments += mapOf(
                    "room.schemaLocation" to "$projectDir/schemas",
                    "room.incremental" to "true"
                )
            }
        }
    }

    buildTypes {
        release {
            isMinifyEnabled = false
            proguardFiles(getDefaultProguardFile("proguard-android-optimize.txt"), "proguard-rules.pro")
        }
        debug {
            isDebuggable = true
            isMinifyEnabled = false
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
        buildConfig = true
    }

    packaging {
        resources {
            excludes += "/META-INF/{AL2.0,LGPL2.1}"
        }
    }

    configurations.all {
        resolutionStrategy {
            // Ép buộc sử dụng phiên bản cụ thể của thư viện androidx.core
            force("androidx.core:core:1.12.0")
        }

        // Loại bỏ các phụ thuộc cũ của thư viện support library
        // Lưu ý: Đã sửa lỗi gõ nhầm dấu nháy đơn (') thành nháy kép (") ở dòng đầu tiên
        exclude(group = "com.android.support", module = "support-compat")
        exclude(group = "com.android.support", module = "support-v4")
        exclude(group = "com.android.support", module = "support-annotations")
        exclude(group = "com.android.support", module = "support-core-utils")
    }
}
val roomVersion = "2.5.0"
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
    implementation("com.google.android.gms:play-services-safetynet:18.0.1")

    // Facebook Login
    implementation("com.facebook.android:facebook-android-sdk:16.3.0")
    implementation("com.facebook.android:facebook-login:16.3.0")
    implementation("com.facebook.android:facebook-login:latest.release")

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
    implementation("io.insert-koin:koin-android:3.5.3")
    implementation(libs.play.services.maps)  // Thêm Koin cho DI

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

    // Thêm Glide
    implementation("com.github.bumptech.glide:glide:4.16.0")
    annotationProcessor("com.github.bumptech.glide:compiler:4.16.0")

    // Thêm CircleImageView
    implementation ("de.hdodenhof:circleimageview:3.1.0")

    implementation("com.squareup.picasso:picasso:2.8")

    implementation("com.github.chrisbanes:PhotoView:2.3.0")

    // Thêm OkHttp3 Logging Interceptor
    implementation("com.squareup.okhttp3:logging-interceptor:4.12.0")

    implementation ("androidx.room:room-runtime:2.6.1")
    annotationProcessor ("androidx.room:room-compiler:2.6.1")
    implementation ("androidx.room:room-ktx:2.6.1") // Nếu dùng Kotlin

    implementation("com.cloudinary:cloudinary-android:2.3.1")
    implementation("com.cloudinary:cloudinary-core:1.34.0")

    implementation("com.google.dagger:hilt-android:2.44")
    annotationProcessor("com.google.dagger:hilt-android-compiler:2.44")

    implementation ("com.github.bumptech.glide:glide:4.12.0")
    annotationProcessor ("com.github.bumptech.glide:compiler:4.12.0")

    // Room Persistence Library
    implementation("androidx.room:room-runtime:$roomVersion")
    // annotationProcessor("androidx.room:room-compiler:$roomVersion") // Dòng này dùng cho Java
    annotationProcessor("androidx.room:room-compiler:$roomVersion") // Sử dụng kapt cho Kotlin

    // Hỗ trợ Kotlin Extensions và Coroutines (tùy chọn)
    implementation("androidx.room:room-ktx:$roomVersion")
}
