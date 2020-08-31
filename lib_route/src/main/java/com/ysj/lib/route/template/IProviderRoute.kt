package com.ysj.lib.route.template

import com.ysj.lib.route.annotation.RouteBean

/**
 * 用于提供路由的路径的接口
 *
 * @author Ysj
 * Create time: 2020/8/6
 */
interface IProviderRoute {

    /**
     * 加载路由路径
     */
    fun loadInto(atlas: MutableMap<String, RouteBean>)
}