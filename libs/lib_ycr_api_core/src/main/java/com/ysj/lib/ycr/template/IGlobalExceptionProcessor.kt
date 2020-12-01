package com.ysj.lib.ycr.template

import com.ysj.lib.ycr.entity.Postman
import com.ysj.lib.ycr.exception.IYCRExceptions

/**
 * 全局异常处理
 *
 * @author Ysj
 * Create time: 2020/11/15
 */
interface IGlobalExceptionProcessor : YCRTemplate, Comparable<IGlobalExceptionProcessor> {

    /**
     * 优先级，用于对其进行排序，值越大优先级越高
     * - 注意：优先级可以相同，相同优先级不保证执行顺序
     *
     * @return default: 0 [Short.MIN_VALUE] ~ [Short.MAX_VALUE]
     */
    @JvmDefault
    fun priority(): Short = 0

    /**
     * 当路由过程未设置局部异常处理器，或局部异常处理器返回 false 时会进入该回调
     *
     * @param postman 发生异常的路由过程实体
     * @param e       发生的异常
     * @return 返回 true 则不执行后面的处理器
     */
    fun handleException(postman: Postman, e: IYCRExceptions): Boolean

    @JvmDefault
    override fun compareTo(other: IGlobalExceptionProcessor): Int {
        var compare = other.priority() - priority()
        if (compare == 0) compare = 1
        return compare
    }
}