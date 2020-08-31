package com.ysj.lib.route.remote

import android.database.MatrixCursor
import android.os.Bundle
import android.util.Log

/**
 * 路由服务
 *
 * @author Ysj
 * Create time: 2020/8/18
 */
class RouteService private constructor() : IRouteService.Stub() {

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
//        Caches.routeCache
        Log.i(TAG, "registerRouteGroup: $param")
    }

    class Cursor : MatrixCursor(arrayOf(ROUTE_SERVICE)) {
        override fun getExtras() = Bundle().apply { putBinder(ROUTE_SERVICE, getInstance()) }
    }
}