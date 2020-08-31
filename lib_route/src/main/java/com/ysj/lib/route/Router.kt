package com.ysj.lib.route

import android.app.Activity
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import com.ysj.lib.route.annotation.PACKAGE_NAME_ROUTE
import com.ysj.lib.route.annotation.PREFIX_ROUTE
import com.ysj.lib.route.annotation.RouteTypes
import com.ysj.lib.route.annotation.subGroupFromPath
import com.ysj.lib.route.template.IProviderRoute
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
        fun getInstance() = Holder.instance
    }

    fun build(path: String) = Postman(subGroupFromPath(path), path)

    fun navigation(context: Context, postman: Postman): Any? {
        val routeClassName = "${PACKAGE_NAME_ROUTE}.${PREFIX_ROUTE}${postman.group}"
        var routes = Caches.routeCache[postman.group]
        try {
            if (routes.isNullOrEmpty()) {
                val routeClass = Class.forName(routeClassName)
                val providerRoute = routeClass.getConstructor().newInstance() as IProviderRoute
                routes = HashMap()
                providerRoute.loadInto(routes)
                Caches.routeCache[postman.group] = routes
            }
            val routeBean = routes[postman.path] ?: throw InvalidParameterException("找不到该路由")
            routeBean.also {
                when (it.types) {
                    RouteTypes.ACTIVITY -> {
                        val intent = Intent()
                        intent.component = ComponentName(it.moduleId, it.className)
                        if (context is Activity) context.startActivityForResult(intent, -1)
                        else context.startActivity(intent.apply {
                            flags = Intent.FLAG_ACTIVITY_NEW_TASK
                        })
                    }
                    else -> throw InvalidParameterException("路由类型不正确")
                }
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
        return null
    }

}