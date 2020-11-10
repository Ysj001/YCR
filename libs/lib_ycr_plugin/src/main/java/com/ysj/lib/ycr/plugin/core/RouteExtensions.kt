package com.ysj.lib.ycr.plugin.core

import com.ysj.lib.ycr.plugin.core.logger.YLogger

/**
 * 路由的扩展属性
 *
 * @author Ysj
 * Create time: 2020/9/6
 */
open class RouteExtensions {

    companion object {
        const val NAME = "ycr"
    }

    /** 是否是主组件 */
    var main: Boolean = false

    /** 设置日志等级 [YLogger]（verbose:0 ~ error:5） */
    var loggerLevel: Int = 0

    override fun toString(): String {
        return "main=$main , loggerLevel=$loggerLevel"
    }
}