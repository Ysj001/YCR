package com.ysj.lib.route

import com.ysj.lib.route.annotation.RouteBean

/**
 * 组件化框架中所有的缓存
 *
 * @author Ysj
 * Create time: 2020/8/5
 */
internal object Caches {

    /**
     * 路由的缓存 key：组名，value：该组中所有的路由
     */
    val routeCache = HashMap<String, MutableMap<String, RouteBean>>()
}