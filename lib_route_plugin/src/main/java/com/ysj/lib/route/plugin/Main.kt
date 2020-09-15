package com.ysj.lib.route.plugin

import com.android.build.gradle.AppExtension
import com.ysj.lib.route.plugin.core.RouteExtensions
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
        // 创建路由的扩展
        val routeExtensions = project.extensions.create(
            "route",
            RouteExtensions::class.java
        )
        val android = project.extensions.getByType(AppExtension::class.java)
        val routeTransform = RouteTransform(project)
        android.registerTransform(routeTransform)
        project.afterEvaluate {
            println("route ext param -> $routeExtensions")
            routeTransform.extensions = routeExtensions
        }
    }

}