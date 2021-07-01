/*
 * 定义 YCR 工程常量
 *
 * @author Ysj
 * Create time: 2021/6/28
 */

/** kotlin 依赖版本 */
const val KOTLIN_VERSION = "1.4.10"

/** com.android.tools.build:gradle version */
const val ANDROID_GRADLE_VERSION = "4.1.3"

/**
 * false: 子模块可以独立运行
 * true ：打包整个项目 apk，子模块不可独立运行
 */
const val isRelease = false

// ------------------ 模块的 applicationId ------------------
val appIds = mapOf(
    "app" to "com.ysj.lib.router",
    "module_m1" to "com.ysj.lib.route.module.m1",
    "module_m2" to "com.ysj.lib.route.module.m2",
    "module_java" to "com.ysj.lib.route.module.java"
)
// -----------------------------------------------

/** compileSdkVersion */
const val csv = 29

/** buildToolsVersion */
const val btv = "29.0.3"

/** minSdkVersion */
const val msv = 19

/** targetSdkVersion */
const val tsv = 29

const val versionCode = 1
const val versionName = "1.0.0"

const val mix = false