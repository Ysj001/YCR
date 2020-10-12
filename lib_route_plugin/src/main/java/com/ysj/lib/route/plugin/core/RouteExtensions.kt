package com.ysj.lib.route.plugin.core

/**
 * 路由的扩展属性
 *
 * @author Ysj
 * Create time: 2020/9/6
 */
open class RouteExtensions {

    companion object{
        const val NAME = "route"
    }

    /** 是否是主组件 */
    var main: Boolean = false

    override fun toString(): String {
        return "main=$main"
    }
}