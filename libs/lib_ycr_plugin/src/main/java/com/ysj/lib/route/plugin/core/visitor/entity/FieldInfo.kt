package com.ysj.lib.route.plugin.core.visitor.entity

/**
 * 字段信息
 *
 * @author Ysj
 * Create time: 2020/9/30
 */
class FieldInfo(
    val access: Int,
    name: String? = "",
    descriptor: String? = "",
    signature: String? = "",
    val value: Any? = null
) {
    val name: String = name ?: ""
    val descriptor: String = descriptor ?: ""
    val signature: String = signature ?: ""

    override fun toString(): String {
        return """
            FieldInfo:
            access=$access
            name=$name
            descriptor=$descriptor
            signature=$signature
            value=$value)
        """.trimIndent()
    }

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other !is FieldInfo) return false

        if (access != other.access) return false
        if (name != other.name) return false
        if (descriptor != other.descriptor) return false
        if (signature != other.signature) return false

        return true
    }

    override fun hashCode(): Int {
        var result = access
        result = 31 * result + name.hashCode()
        result = 31 * result + descriptor.hashCode()
        result = 31 * result + signature.hashCode()
        result = 31 * result + value.hashCode()
        return result
    }

}