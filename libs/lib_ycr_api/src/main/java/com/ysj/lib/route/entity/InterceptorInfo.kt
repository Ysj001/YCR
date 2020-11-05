package com.ysj.lib.route.entity

import java.io.Serializable

/**
 * 拦截器信息实体
 *
 * @author Ysj
 * Create time: 2020/10/31
 */
class InterceptorInfo(
    val applicationId: String,
    val className: String,
    val priority: Short
) : Serializable, Comparable<InterceptorInfo> {

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other !is InterceptorInfo) return false

        if (applicationId != other.applicationId) return false
        if (className != other.className) return false

        return true
    }

    override fun hashCode(): Int {
        var result = applicationId.hashCode()
        result = 31 * result + className.hashCode()
        return result
    }

    override fun toString(): String {
        return "InterceptorInfo(applicationId='$applicationId', className='$className', priority=$priority)"
    }

    override fun compareTo(other: InterceptorInfo): Int =
        other.priority - priority

}