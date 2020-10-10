package com.ysj.lib.route.template

import android.content.Context
import com.ysj.lib.route.entity.Postman
import com.ysj.lib.route.callback.InterceptorCallback

/**
 * 拦截器
 *
 * @author Ysj
 * Create time: 2020/10/5
 */
interface IInterceptor : RouteTemplate {

    /**
     * 用于告诉路由框架匹配拦截器的规则
     * - 注意：不要在其中做耗时操作，这会影响拦截器的匹配速度，进而影响路由
     *
     * @return 若返回 true 则说明是匹配的拦截器，将会执行 [onIntercept]
     */
    fun match(postman: Postman): Boolean

    /**
     * 对拦截到的数据进行处理
     * - 注意：不要在其中做过于耗时的操作，框架中只允许（主线程 2s，其它线程 10s）
     *
     * @param callback  拦截器回调
     * @return 若返回 true 则会拦截，路由框架不会执行导航
     */
    fun onIntercept(context: Context, postman: Postman, callback: InterceptorCallback)

}