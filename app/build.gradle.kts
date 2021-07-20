plugins {
    id("com.android.application")
    id("kotlin-android")
    id("kotlin-android-extensions")
    id("kotlin-kapt")
    id("ycr-plugin")
}

// 设置 ycr-plugin 的编译参数
ycr {
    // 若该 module 是主组件，则必须设置
    main = true
    // 设置 YCR 编译时日志输出等级（可选）
    loggerLevel = 1
}

android {
    compileSdkVersion(csv)
    buildToolsVersion(btv)

    defaultConfig {
        minSdkVersion(msv)
        targetSdkVersion(tsv)
        applicationId = appIds[project.name]
        this.versionCode = versionCode
        this.versionName = versionName
    }

    buildTypes {
        getByName("debug") {
            isMinifyEnabled = mix
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro"
            )
        }
        getByName("release") {
            isMinifyEnabled = mix
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

    // lint 耗时，关掉
    lintOptions {
        isCheckReleaseBuilds = false
    }
}

dependencies {
    implementation(fileTree("dir" to "libs", "include" to arrayOf("*.jar", "*.aar")))
    kapt("$YCR_GROUP_ID:ycr-compiler:$YCR_VERSION")

//    implementation(project(":lib_base"))
    import(":lib_base")
    if (isRelease) appIds.forEach {
        val moduleName = it.key
        if (moduleName == project.name || !rootProject.childProjects.containsKey(moduleName)) return@forEach
//        implementation(project(":$moduleName"))
        import(":$moduleName")
        println("release install module: $moduleName")
    }
}