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
        RouteTransform.moduleRouteExt = project.extensions.create(
            RouteExtensions.NAME,
            RouteExtensions::class.java
        )
        // 创建路由的扩展
        RouteTransform.moduleAppExt = project.extensions.getByType(AppExtension::class.java)
        val routeTransform = RouteTransform(project)
        RouteTransform.moduleAppExt.registerTransform(routeTransform)
        project.afterEvaluate(::initExtensions)
    }

    private fun initExtensions(project: Project) {
        var mainProject: Project? = null
        var index = 0
        project.rootProject.subprojects { subProject ->
            subProject.afterEvaluate {
                index++
                val routePlugin = it.plugins.findPlugin(javaClass)
                if (routePlugin != null) {
                    val routeExt = it.extensions
                        .findByName(RouteExtensions.NAME) as RouteExtensions
                    if (routeExt.main) {
                        if (mainProject != null) {
                            throw Exception("检测到主组件被重复定义，请检查 ${it.name} 或 ${mainProject!!.name} 下的 build.gradle")
                        }
                        mainProject = it
                    }
                }
                if (index == project.rootProject.subprojects.size || mainProject != null) {
                    RouteTransform.mainModuleAppExt =
                        mainProject?.extensions?.getByType(AppExtension::class.java)
                            ?: throw Exception(
                                """
                            未定义主组件，请在你的主组件的 build.gradle 中添加如下代码
                            route {
                                main = true
                            }
                        """.trimIndent()
                            )
                }
            }
        }
    }
}