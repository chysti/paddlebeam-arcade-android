plugins { id("com.android.application") }

android {
    namespace = "com.chystialex.paddlebeamarcade"
    compileSdk = 36
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
    }

    defaultConfig {
        applicationId = "com.chystialex.paddlebeamarcade"
        minSdk = 23
        targetSdk = 36
        versionCode = 3
        versionName = "1.0.2"
    }
    signingConfigs {
        create("releaseUpload") {
            storeFile = rootProject.file("paddlebeam-upload.jks")
            storePassword = rootProject.file("upload-key-password.txt").readText().trim()
            keyAlias = "paddlebeam-upload"
            keyPassword = storePassword
        }
    }
    buildTypes {
        release {
            signingConfig = signingConfigs.getByName("releaseUpload")
            isMinifyEnabled = true
            proguardFiles(getDefaultProguardFile("proguard-android-optimize.txt"), "proguard-rules.pro")
        }
    }
}
