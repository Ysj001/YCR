package com.ysj.lib.ycr.callback

import com.ysj.lib.ycr.entity.InterruptReason
import com.ysj.lib.ycr.entity.Postman

/**
 * 拦截器的回调
 *
 * @author Ysj
 * Create time: 2020/10/5
 */
interface InterceptorCallback {

    fun interface InterruptCallback {
        fun onInterrupt(postman: Postman, reason: InterruptReason<*>)
    }

    /**
     * 表示该拦截器允许继续后续的路由
     * - 注意：不要重复调用或同时调用 [onInterrupt]
     */
    fun onContinue(postman: Postman)

    /**
     * 表示该拦截器不允许后续路由，但不会中断已匹配的拦截器处理过程
     * - 注意：不要重复调用或同时调用 [onContinue]
     *
     * @param reason 用于描述中断原因的实体
     */
    fun onInterrupt(postman: Postman, reason: InterruptReason<*>)
}