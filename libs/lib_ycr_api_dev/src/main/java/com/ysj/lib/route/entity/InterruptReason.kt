package com.ysj.lib.route.entity

import java.io.Serializable

/**
 * 用于描述拦截器中断原因的实体
 *
 * @author Ysj
 * Create time: 2020/10/11
 */
open class InterruptReason<T>(
    var code: Int,
    var msg: String? = "",
    var data: T? = null
) : Serializable