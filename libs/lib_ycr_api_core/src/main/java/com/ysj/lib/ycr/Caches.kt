package com.ysj.lib.ycr

import com.ysj.lib.ycr.annotation.RouteBean
import com.ysj.lib.ycr.template.IActionProcessor
import com.ysj.lib.ycr.template.IGlobalExceptionProcessor
import com.ysj.lib.ycr.template.IGlobalInterceptor
import com.ysj.lib.ycr.template.IProviderParam
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

    /** 参数注入器的缓存 key：目标的 className，value：[IProviderParam] 的实现 */
    val paramCache = HashMap<String, IProviderParam>()

    /** 所有全局拦截器 */
    val interceptors = TreeSet<IGlobalInterceptor>()

    /** 所有全局异常处理器 */
    val globalExceptionProcessors = TreeSet<IGlobalExceptionProcessor>()
}