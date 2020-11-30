package com.ysj.lib.ycr.template

/**
 * 全局拦截器
 *
 * @author Ysj
 * Create time: 2020/11/29
 */
interface IGlobalInterceptor : IInterceptor, Comparable<IGlobalInterceptor> {

    /**
     * 优先级，用于对其进行排序，值越大优先级越高
     * - 注意：优先级可以相同，相同优先级不保证执行顺序
     *
     * @return default: 0 [Short.MIN_VALUE] ~ [Short.MAX_VALUE]
     */
    @JvmDefault
    fun priority(): Short = 0

    @JvmDefault
    override fun compareTo(other: IGlobalInterceptor): Int = other.priority() - priority()
}