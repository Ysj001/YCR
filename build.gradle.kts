// Top-level build file where you can add configuration options common to all sub-projects/modules.

buildscript {

    repositories {
        maven(MAVEN_LOCAL)
        google()
        gradlePluginPortal()
        mavenCentral()
    }

    dependencies {
        classpath("com.android.tools.build:gradle:$ANDROID_GRADLE_VERSION")
        classpath("org.jetbrains.kotlin:kotlin-gradle-plugin:$KOTLIN_VERSION")

        classpath("$YCR_GROUP_ID:ycr-plugin:$YCR_VERSION")

        classpath("com.vanniktech:gradle-maven-publish-plugin:0.14.2")
        classpath("org.jetbrains.dokka:dokka-gradle-plugin:1.4.30")
    }
}

allprojects {
    repositories {
        maven(MAVEN_LOCAL)
        maven(MAVEN_COMPONENT_LOCAL)
        maven(MAVEN_COMPONENTS)
        google()
        gradlePluginPortal()
        mavenCentral()
    }
}

childProjects.values.forEach {
    it.afterEvaluate {
        val isAndroidLib = plugins.hasPlugin("com.android.library")
        // 给所有 android-lib 工程添加 maven 发布功能
        if (isAndroidLib) mavenPublish(
            COMPONENT_GROUP_ID,
            name,
            COMPONENT_VERSION,
            "${rootProject.name}-$name",
            "aar"
        ) {
            maven {
                name = "local"
                url = MAVEN_COMPONENT_LOCAL
            }
            maven {
                name = "nexus"
                setUrl(MAVEN_COMPONENTS)
                credentials {
                    username = property("mavenCentralUserName").toString()
                    password = property("mavenCentralPassword").toString()
                }
            }
        }
    }
}

tasks.register<Delete>("clean") {
    delete(rootProject.buildDir)
}