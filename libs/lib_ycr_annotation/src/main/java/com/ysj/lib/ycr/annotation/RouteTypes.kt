package com.ysj.lib.ycr.annotation

/**
 * 路由的类型
 *
 * @author Ysj
 * Create time: 2020/8/4
 */
inline class RouteTypes(val name: String) {

    companion object {

        val ACTIVITY = RouteTypes("ACTIVITY")

        val ACTION = RouteTypes("ACTION")
    }

}