package com.ysj.lib.route.remote

import android.database.MatrixCursor
import android.os.Bundle
import android.util.Log
import com.ysj.lib.route.Caches
import com.ysj.lib.route.annotation.RouteBean

/**
 * 夸进程的路由服务
 *
 * @author Ysj
 * Create time: 2020/8/18
 */
internal class RouteService private constructor() : IRouteService.Stub() {

    private object Holder {
        val instance = RouteService()
    }

    companion object {
        private const val TAG = "RouteService"

        /** 用于获取路由服务的 KEY */
        const val ROUTE_SERVICE = "ROUTE_SERVICE"

        fun getInstance() = Holder.instance
    }

    val cursor = Cursor()

    override fun registerRouteGroup(group: String, param: RemoteParam) {
        val routeMap = HashMap<String, RouteBean>()
        param.params.entries.forEach { routeMap[it.key] = (it.value as RouteWrapper).routeBean }
        Caches.routeCache[group] = routeMap
        Log.i(TAG, "registerRouteGroup: $group , $routeMap")
    }

    class Cursor : MatrixCursor(arrayOf(ROUTE_SERVICE)) {
        override fun getExtras() = Bundle().apply { putBinder(ROUTE_SERVICE, getInstance()) }
    }
}