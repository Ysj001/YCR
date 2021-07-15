plugins {
    id("com.android.library")
    id("kotlin-android")
    id("kotlin-android-extensions")
    id("kotlin-kapt")
    id("ycr-plugin")
}

android {
    compileSdkVersion(csv)
    buildToolsVersion(btv)

    defaultConfig {
        minSdkVersion(msv)
        targetSdkVersion(tsv)
        this.versionCode = versionCode
        this.versionName = versionName
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
    implementation(fileTree(mapOf("dir" to "libs", "include" to listOf("*.jar"))))
    api("org.jetbrains.kotlin:kotlin-stdlib:$KOTLIN_VERSION")
    api("androidx.core:core-ktx:1.3.2")

    api("androidx.appcompat:appcompat:1.2.0")
    api("androidx.swiperefreshlayout:swiperefreshlayout:1.2.0-alpha01")
    api("com.google.android.material:material:1.3.0-alpha04")
    api("androidx.legacy:legacy-support-v4:1.0.0")
    api("androidx.constraintlayout:constraintlayout:2.0.4")

    if (isRelease) api("$YCR_GROUP_ID:ycr-api:$YCR_VERSION")
    else api("$YCR_GROUP_ID:ycr-api-dev:$YCR_VERSION")
//    releaseApi("$YCR_GROUP_ID:ycr-api:$YCR_VERSION")
//    debugApi("$YCR_GROUP_ID:ycr-api-dev:$YCR_VERSION")
    kapt("$YCR_GROUP_ID:ycr-compiler:$YCR_VERSION")
}