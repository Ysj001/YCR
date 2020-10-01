package com.ysj.lib.route.remote

import android.util.Log
import com.ysj.lib.route.Caches
import com.ysj.lib.route.annotation.RouteBean

/**
 * 夸进程的路由服务
 *
 * @author Ysj
 * Create time: 2020/8/18
 */
internal class RouteService : IRouteService.Stub() {

    companion object {
        private const val TAG = "RouteService"

        /** 用于获取路由服务的 KEY */
        const val ROUTE_SERVICE = "ROUTE_SERVICE"
    }

    override fun registerRouteGroup(group: String, param: RemoteParam) {
        val routeMap = HashMap<String, RouteBean>()
        param.params.forEach { routeMap[it.key] = (it.value as RouteWrapper).routeBean }
        Caches.routeCache[group] = routeMap
        Log.i(TAG, "registerRouteGroup: ${Caches.routeCache.size} , $group , $param")
    }

    override fun findRouteBean(group: String?, path: String?): RouteWrapper? {
        if (group.isNullOrEmpty() || path.isNullOrEmpty()) return null
        val routeBean = Caches.routeCache[group]?.get(path)
        return if (routeBean == null) null else RouteWrapper(routeBean)
    }

}