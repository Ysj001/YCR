package com.ysj.lib.ycr.remote.entity

import java.io.Serializable

/**
 * 有优先级排序的 Class 信息实体
 *
 * @author Ysj
 * Create time: 2020/10/31
 */
class PrioritiableClassInfo(
    val applicationId: String,
    val className: String,
    val priority: Short
) : Serializable, Comparable<PrioritiableClassInfo> {

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other !is PrioritiableClassInfo) return false

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

    override fun compareTo(other: PrioritiableClassInfo): Int = other.priority - priority

}