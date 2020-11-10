package com.ysj.lib.ycr.template

import com.ysj.lib.ycr.annotation.RouteBean

/**
 * 用于提供路由的路径的接口
 *
 * @author Ysj
 * Create time: 2020/8/6
 */
interface IProviderRoute : YCRTemplate {

    /**
     * 加载路由 key: path，value: 路由信息
     */
    fun loadInto(atlas: MutableMap<String, RouteBean>)
}