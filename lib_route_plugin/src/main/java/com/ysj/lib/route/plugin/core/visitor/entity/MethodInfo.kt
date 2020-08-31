package com.ysj.lib.route.plugin.core.visitor.entity

/**
 * Method 的信息
 *
 * @author Ysj
 * Create time: 2020/8/16
 */
class MethodInfo(
    val access: Int,
    name: String? = "",
    descriptor: String? = "",
    signature: String? = "",
    exceptions: Array<out String>? = arrayOf()
) {
    val name: String = name ?: ""
    val descriptor: String = descriptor ?: ""
    val signature: String = signature ?: ""
    val exceptions: Array<out String> = exceptions ?: arrayOf()

    override fun toString(): String {
        return """
                MethodInfo:
                access=$access
                name=$name
                descriptor=$descriptor
                signature=$signature
                exceptions=${exceptions.contentToString()}
        """.trimIndent()
    }

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as MethodInfo

        if (access != other.access) return false
        if (name != other.name) return false
        if (descriptor != other.descriptor) return false
        if (signature != other.signature) return false
        if (!exceptions.contentEquals(other.exceptions)) return false

        return true
    }

    override fun hashCode(): Int {
        var result = access
        result = 31 * result + name.hashCode()
        result = 31 * result + descriptor.hashCode()
        result = 31 * result + signature.hashCode()
        result = 31 * result + exceptions.contentHashCode()
        return result
    }
}