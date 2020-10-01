package com.ysj.lib.route

import android.app.Activity
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import com.ysj.lib.route.annotation.PACKAGE_NAME_ROUTE
import com.ysj.lib.route.annotation.PREFIX_ROUTE
import com.ysj.lib.route.annotation.RouteTypes
import com.ysj.lib.route.annotation.subGroupFromPath
import com.ysj.lib.route.template.IActionProcessor
import com.ysj.lib.route.template.IProviderRoute
import com.ysj.lib.route.template.Template
import java.security.InvalidParameterException

/**
 * 路由的管理器
 *
 * @author Ysj
 * Create time: 2020/8/4
 */
class Router private constructor() {

    private object Holder {
        val instance = Router()
    }

    companion object {
        private const val TAG = "Router"

        @JvmStatic
        fun getInstance() = Holder.instance
    }

    fun build(path: String) = Postman(subGroupFromPath(path), path)

    @Suppress("UNCHECKED_CAST", "IMPLICIT_CAST_TO_ANY")
    fun <T> navigation(context: Context, postman: Postman): T {
        val routeClassName = "${PACKAGE_NAME_ROUTE}.${PREFIX_ROUTE}${postman.group}"
        val routes = Caches.routeCache[postman.group] ?: HashMap()
        try {
            if (routes.isEmpty()) {
                val providerRoute: IProviderRoute? = getTemplateInstance(routeClassName)
                if (providerRoute != null) {
                    providerRoute.loadInto(routes)
                    Caches.routeCache[postman.group] = routes
                }
            }
            val routeBean = routes[postman.path]
                ?: findRouteBean(postman.group, postman.path)
                ?: throw InvalidParameterException("找不到该路由")
            routeBean.also {
                return when (it.types) {
                    RouteTypes.ACTIVITY -> {
                        val intent = Intent()
                        intent.component = ComponentName(it.moduleId, it.className)
                        if (context is Activity) context.startActivityForResult(intent, -1)
                        else context.startActivity(intent.apply {
                            flags = Intent.FLAG_ACTIVITY_NEW_TASK
                        })
                    }
                    RouteTypes.ACTION -> {
                        val actions: IActionProcessor? = Caches.actionCache[it.className]
                            ?: getTemplateInstance(postman.className)
                        if (actions != null) Caches.actionCache[it.className] = actions
                        actions?.doAction<T>(postman.actionName)
                    }
                    else -> throw InvalidParameterException("路由类型不正确")
                } as T
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
        return Unit as T
    }

    private fun findRouteBean(group: String, path: String) = RouteProvider.instance
        ?.routerService?.findRouteBean(group, path)?.routeBean

    private fun <T : Template> getTemplateInstance(className: String): T? {
        try {
            return Class.forName(className).getConstructor().newInstance() as T
        } catch (e: Exception) {
            e.printStackTrace()
        }
        return null
    }
}