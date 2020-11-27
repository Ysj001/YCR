package com.ysj.lib.ycr.template

/**
 * 用于参数注入
 *
 * @author Ysj
 * Create time: 2020/11/20
 */
interface IProviderParam : YCRTemplate {

    /**
     * 注入参数
     *
     * @param target 要注入参数的对象
     */
    fun injectParam(target: Any)
}