package com.ysj.lib.ycr.apt

import com.ysj.lib.ycr.annotation.Route
import com.ysj.lib.ycr.annotation.RouteParam

/*
 * 注解处理器支持的注解
 *
 * @author Ysj
 * Create time: 2020/8/4
 */

/**用于支持注解 [Route]*/
const val ANNOTATION_TYPE_ROUTE = "com.ysj.lib.ycr.annotation.Route"

/**用于支持注解 [RouteParam]*/
const val ANNOTATION_TYPE_PARAMETER = "com.ysj.lib.ycr.annotation.RouteParam"