package com.ysj.lib.ycr.annotation

import java.io.Serializable

/**
 * 路由的类型
 *
 * @author Ysj
 * Create time: 2020/8/4
 */
sealed class RouteTypes(val name: String) : Serializable {

    object ACTIVITY : RouteTypes("ACTIVITY")

    object ACTION : RouteTypes("ACTION")
}