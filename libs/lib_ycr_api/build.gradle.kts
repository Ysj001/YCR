plugins {
    id("com.android.library")
    id("kotlin-android")
}

android {
    compileSdkVersion(29)

    defaultConfig {
        minSdkVersion(19)
        targetSdkVersion(29)
        versionCode = 1
        versionName = "1.0"
    }

    sourceSets.getByName("main") {
        java.srcDirs(
            "src/main/java",
            "../lib_ycr_api_core/src/main/java"
        )
    }

    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_1_8
        targetCompatibility = JavaVersion.VERSION_1_8
    }

    kotlinOptions {
        jvmTarget = "1.8"
        freeCompilerArgs = listOf("-Xjvm-default=enable")
    }
}

dependencies {
    compileOnly("org.jetbrains.kotlin:kotlin-stdlib:$KOTLIN_VERSION")
    compileOnly("androidx.core:core-ktx:1.3.2")
    compileOnly("androidx.appcompat:appcompat:1.2.0")
    api("$LIB_GROUP_ID:ycr-annotation:$LIB_VERSION")
}

mavenPublish()