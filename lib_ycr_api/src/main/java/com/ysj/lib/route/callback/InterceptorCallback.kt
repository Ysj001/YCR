package com.ysj.lib.route.callback

import com.ysj.lib.route.entity.InterruptReason
import com.ysj.lib.route.entity.Postman

/**
 * 拦截器的回调
 *
 * @author Ysj
 * Create time: 2020/10/5
 */
interface InterceptorCallback {

    interface ContinueCallback {
        fun onContinue(postman: Postman)
    }

    interface InterruptCallback {
        fun onInterrupt(postman: Postman, reason: InterruptReason<*>)
    }

    /**
     * 继续后续的路由
     * - 注意：不要重复调用或同时调用 [onInterrupt]
     */
    fun onContinue(postman: Postman)

    /**
     * 中断后续路由
     * - 注意：不要重复调用或同时调用 [onContinue]
     *
     * @param reason 用于描述中断原因的实体
     */
    fun onInterrupt(postman: Postman, reason: InterruptReason<*>)
}