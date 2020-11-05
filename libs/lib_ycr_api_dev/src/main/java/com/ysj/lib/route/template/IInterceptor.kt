package com.ysj.lib.route.template

import com.ysj.lib.route.INTERCEPTOR_TIME_OUT_MAIN_TH
import com.ysj.lib.route.INTERCEPTOR_TIME_OUT_SUB_TH
import com.ysj.lib.route.callback.InterceptorCallback
import com.ysj.lib.route.entity.Postman

/**
 * 拦截器
 *
 * @author Ysj
 * Create time: 2020/10/5
 */
interface IInterceptor : RouteTemplate {

    /**
     * 拦截器优先级，用于对匹配的拦截器进行排序，值越大优先级越高
     *
     * @return default: 0 [Short.MIN_VALUE] ~ [Short.MAX_VALUE]
     */
    fun priority(): Short

    /**
     * 对拦截到的数据进行处理
     * - 注意：不要在其中做过于耗时的操作，
     * 框架中只允许（主线程 [INTERCEPTOR_TIME_OUT_MAIN_TH]，其它线程 [INTERCEPTOR_TIME_OUT_SUB_TH]）
     *
     * @param callback  拦截器结果回调
     */
    fun onIntercept(postman: Postman, callback: InterceptorCallback)

}