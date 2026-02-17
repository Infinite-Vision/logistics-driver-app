import java.util.Properties

plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.kotlin.android)
    alias(libs.plugins.ksp)
    id("androidx.navigation.safeargs.kotlin")
}

android {
    namespace = "com.example.logistics_driver_app"
    compileSdk = 36

    defaultConfig {
        applicationId = "com.example.logistics_driver_app"
        minSdk = 24
        targetSdk = 36
        versionCode = 1
        versionName = "1.0"

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
        
        // Load AWS credentials from aws.properties file
        val awsPropertiesFile = rootProject.file("aws.properties")
        if (awsPropertiesFile.exists()) {
            val awsProperties = Properties()
            awsProperties.load(awsPropertiesFile.inputStream())
            
            buildConfigField("String", "AWS_ACCESS_KEY", "\"${awsProperties.getProperty("aws.access.key", "")}\"")
            buildConfigField("String", "AWS_SECRET_KEY", "\"${awsProperties.getProperty("aws.secret.key", "")}\"")
            buildConfigField("String", "S3_BUCKET_NAME", "\"${awsProperties.getProperty("s3.bucket.name", "")}\"")
            buildConfigField("String", "S3_REGION_NAME", "\"${awsProperties.getProperty("s3.region.name", "")}\"")
        } else {
            // Fallback empty values if file doesn't exist
            buildConfigField("String", "AWS_ACCESS_KEY", "\"\"")
            buildConfigField("String", "AWS_SECRET_KEY", "\"\"")
            buildConfigField("String", "S3_BUCKET_NAME", "\"\"")
            buildConfigField("String", "S3_REGION_NAME", "\"\"")
        }
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
        sourceCompatibility = JavaVersion.VERSION_21
        targetCompatibility = JavaVersion.VERSION_21
    }
    kotlin {
        jvmToolchain(21)
    }
    buildFeatures {
        viewBinding = true
        buildConfig = true
    }
}

dependencies {
    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.appcompat)
    implementation(libs.material)
    implementation(libs.androidx.recyclerview)
    implementation(libs.androidx.constraintlayout)
    implementation(libs.androidx.lifecycle.livedata.ktx)
    implementation(libs.androidx.lifecycle.viewmodel.ktx)
    implementation(libs.androidx.navigation.fragment.ktx)
    implementation(libs.androidx.navigation.ui.ktx)
    testImplementation(libs.junit)
    androidTestImplementation(libs.androidx.junit)
    
    // Room Database
    implementation(libs.androidx.room.runtime)
    implementation(libs.androidx.room.ktx)
    ksp(libs.androidx.room.compiler)
    
    // Retrofit & Networking
    implementation(libs.retrofit)
    implementation(libs.retrofit.gson)
    implementation(libs.okhttp)
    implementation(libs.okhttp.logging)
    
    // Coroutines
    implementation(libs.kotlinx.coroutines.core)
    implementation(libs.kotlinx.coroutines.android)
    
    // Gson
    implementation(libs.gson)

    // AWS S3
    implementation("com.amazonaws:aws-android-sdk-s3:2.77.0")
    implementation("com.amazonaws:aws-android-sdk-core:2.77.0")

    // Play Services
    implementation(libs.play.services.auth)
    implementation(libs.play.services.auth.api.phone)
    implementation("com.google.android.gms:play-services-location:21.3.0")
    
    androidTestImplementation(libs.androidx.espresso.core)
}
