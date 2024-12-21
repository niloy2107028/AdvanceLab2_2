plugins {
    alias(libs.plugins.android.application)
    id("com.google.gms.google-services")
}

android {
    namespace = "com.example.adminkuetapp"
    compileSdk = 34

    defaultConfig {
        applicationId = "com.example.adminkuetapp"
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

    packagingOptions {
        // Exclude duplicate files that might cause issues
        exclude("META-INF/DEPENDENCIES")
        exclude("META-INF/LICENSE")
        exclude("META-INF/LICENSE.txt")
        exclude("META-INF/NOTICE")
        exclude("META-INF/NOTICE.txt")
    }
}

dependencies {
    // AndroidX Libraries
    implementation(libs.appcompat)
    implementation(libs.material)
    implementation(libs.activity)
    implementation(libs.constraintlayout)

    // Firebase libraries
    implementation(platform("com.google.firebase:firebase-bom:33.6.0"))
    implementation("com.google.firebase:firebase-database")
    implementation("com.google.firebase:firebase-storage")
    implementation("com.google.firebase:firebase-analytics")

    implementation("com.google.firebase:firebase-database:20.0.5")
    implementation("com.google.firebase:firebase-auth:21.0.5")

    // Testing Libraries
    testImplementation(libs.junit)
    androidTestImplementation(libs.ext.junit)
    androidTestImplementation(libs.espresso.core)

    // Material Design library
    implementation("com.google.android.material:material:1.9.0")

    // Google APIs
    implementation("com.google.android.gms:play-services-auth:20.7.0") // Google Sign-In for Drive API
    implementation("com.google.android.gms:play-services-drive:17.0.0") // Google Drive API

    // Google API Client
    implementation("com.google.apis:google-api-services-drive:v3-rev136-1.25.0") {
        exclude(group = "org.apache.httpcomponents")
    }
    implementation("com.google.api-client:google-api-client-android:1.34.0") {
        exclude(group = "org.apache.httpcomponents")
    }

    // Google HTTP Client for Drive API
    implementation("com.google.http-client:google-http-client-gson:1.40.1") // For HTTP requests

    // Ensure that the Gson library is available for JSON parsing
    implementation("com.google.code.gson:gson:2.8.9")

    //circle image view
    implementation(libs.circleimageview)

    implementation(libs.picasso)

}
