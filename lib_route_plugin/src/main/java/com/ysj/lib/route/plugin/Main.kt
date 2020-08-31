package com.ysj.lib.route.plugin

import com.android.build.gradle.AppExtension
import com.ysj.lib.route.plugin.core.RouteTransform
import org.gradle.api.Plugin
import org.gradle.api.Project

/**
 * 插件入口
 *
 * @author Ysj
 */
class Main : Plugin<Project> {

    override fun apply(project: Project) {
        val android = project.extensions.getByType(AppExtension::class.java)
        project.afterEvaluate {
            println("Plugin 1 --> ${android.defaultConfig}")
            println("Plugin 2 --> ${android.defaultConfig.applicationId}")
            println("Plugin 3 --> ${project.dependencies}")
        }
        val transform = RouteTransform(project)
        android.registerTransform(transform)
    }

}