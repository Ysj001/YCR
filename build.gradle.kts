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
    applyComponentPublish()
}

fun Project.applyComponentPublish() = afterEvaluate {
    if (!plugins.hasPlugin("com.android.library")) return@afterEvaluate
    // 给所有组件工程添加 maven 发布功能
    val desc = "${rootProject.name}-$name"
    mavenPublish(COMPONENT_GROUP_ID, name, COMPONENT_VERSION, desc, "aar") {
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
    // 发布该组件，并发布所有依赖该组件的组件
    afterEvaluate {
        tasks.forEach { task ->
            val prefix = "publishAllPublicationsTo"
            if (!task.name.startsWith(prefix)) return@forEach
            tasks.register<Task>("componentPublishTo${task.name.substring(prefix.length)}") {
                group = "publishing"
                dependsOn(task.path)
                componentDependency[project.name]?.forEach { projectName ->
                    rootProject.findProject(projectName)
                        ?.tasks
                        ?.find { it.name == task.name }
                        ?.also { dependsOn(it.path) }
                }
            }
        }
    }
}

tasks.register<Delete>("clean") {
    delete(rootProject.buildDir)
}