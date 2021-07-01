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

        classpath("$LIB_GROUP_ID:ycr-plugin:$LIB_VERSION")

        classpath("com.vanniktech:gradle-maven-publish-plugin:0.14.2")
        classpath("org.jetbrains.dokka:dokka-gradle-plugin:1.4.30")
    }
}

allprojects {
    repositories {
        maven(MAVEN_LOCAL)
        google()
        gradlePluginPortal()
        mavenCentral()
    }
}

tasks.register<Delete>("clean") {
    delete(rootProject.buildDir)
}