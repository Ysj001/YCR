plugins {
    if (isRelease) id("com.android.library")
    else id("com.android.application")
    id("kotlin-android")
    id("kotlin-kapt")
    id("ycr-plugin")
}

ycr.loggerLevel = 2

android {
    compileSdkVersion(csv)

    resourcePrefix = "module_java_"

    defaultConfig {
        minSdkVersion(msv)
        targetSdkVersion(tsv)
        if (!isRelease) applicationId = appIds[project.name]
        versionCode = 1
        versionName = "1.0"
    }

    if (!isRelease) sourceSets.getByName("main") {
        manifest.srcFile("src/dev/AndroidManifest.xml")
        java.srcDirs("src/main/java", "src/dev/java")
        res.srcDirs("src/main/res", "src/dev/res")
    }

    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_1_8
        targetCompatibility = JavaVersion.VERSION_1_8
    }
}

dependencies {
    implementation(fileTree(mapOf("dir" to "libs", "include" to listOf("*.jar"))))
    import(":lib_base")
    implementation("androidx.appcompat:appcompat:1.2.0")
    implementation("androidx.constraintlayout:constraintlayout:2.0.3")

    kapt("$YCR_GROUP_ID:ycr-compiler:$YCR_VERSION")
}