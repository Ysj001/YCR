package com.ysj.lib.route.apt

import com.ysj.lib.route.annotation.Autowrite
import com.ysj.lib.route.annotation.Route

/*
 * 注解处理器支持的注解
 *
 * @author Ysj
 * Create time: 2020/8/4
 */

/**用于支持注解 [Route]*/
const val ANNOTATION_TYPE_ROUTE = "com.ysj.lib.route.annotation.Route"

/**用于支持注解 [Autowrite]*/
const val ANNOTATION_TYPE_PARAMETER = "com.ysj.lib.route.annotation.Parameter"