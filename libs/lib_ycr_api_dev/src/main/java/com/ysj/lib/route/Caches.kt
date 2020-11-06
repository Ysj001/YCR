package com.ysj.lib.route

import com.ysj.lib.route.annotation.RouteBean
import com.ysj.lib.route.template.IActionProcessor
import com.ysj.lib.route.template.IInterceptor
import java.util.*
import kotlin.collections.HashMap

/**
 * 组件化框架中所有的缓存
 *
 * @author Ysj
 * Create time: 2020/8/5
 */
internal object Caches {

    /** 路由的缓存 key：组名，value：该组中所有的路由 */
    val routeCache = HashMap<String, MutableMap<String, RouteBean>>()

    /** 行为的缓存 key：action 的 className，value：[IActionProcessor] 的实现 */
    val actionCache = HashMap<String, IActionProcessor>()

    /** 所有拦截器 */
    val interceptors = TreeSet<IInterceptor>()

}