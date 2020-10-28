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
        project.extensions.create(RouteExtensions.NAME, RouteExtensions::class.java)
        project.extensions.getByType(AppExtension::class.java).also { appExt ->
            appExt.registerTransform(RouteTransform(project).apply {
                project.afterEvaluate {
                    moduleAppExt = appExt
                    moduleRouteExt = project.extensions.getByType(RouteExtensions::class.java)
                }
                getMainAppExt(project) { mainAppExt -> mainModuleAppExt = mainAppExt }
            })
        }
    }

    /**
     * 获取主组件的 [AppExtension]
     */
    private fun getMainAppExt(project: Project, block: (AppExtension) -> Unit) {
        var mainProject: Project? = null
        var index = 0
        project.rootProject.subprojects { subProject ->
            if (subProject.plugins.hasPlugin(javaClass)) {
                subProject.extensions.getByType(RouteExtensions::class.java).also { routeExt ->
                    if (!routeExt.main) return@also
                    mainProject = subProject
                }
            }
            subProject.afterEvaluate {
                index++
                if (!it.plugins.hasPlugin(javaClass)) return@afterEvaluate
                it.extensions.getByType(RouteExtensions::class.java).also { routeExt ->
                    if (!routeExt.main) return@also
                    if (mainProject != null) throw Exception(
                        """
                        检测到主组件被重复定义，请检查 ${it.name} 或 ${mainProject!!.name} 下的 build.gradle
                        """.trimIndent()
                    )
                    mainProject = it
                }
                if (index != project.rootProject.subprojects.size && mainProject == null) return@afterEvaluate
                block(
                    mainProject
                        ?.extensions
                        ?.getByType(AppExtension::class.java)
                        ?: throw Exception(
                            """
                            未定义主组件，请在你的主组件的 build.gradle 中添加如下代码
                            ${RouteExtensions.NAME} {
                                main = true
                            }
                            """.trimIndent()
                        )
                )
            }
        }
    }
}