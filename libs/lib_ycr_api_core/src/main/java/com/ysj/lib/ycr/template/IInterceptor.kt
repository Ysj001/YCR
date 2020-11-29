package com.ysj.lib.ycr.template

import com.ysj.lib.ycr.callback.InterceptorCallback
import com.ysj.lib.ycr.entity.Postman

/**
 * 拦截器
 *
 * @author Ysj
 * Create time: 2020/10/5
 */
interface IInterceptor : YCRTemplate {

    /**
     * 对拦截到的路由过程进行处理
     *
     * @param callback  拦截器结果回调
     */
    fun onIntercept(postman: Postman, callback: InterceptorCallback)

}