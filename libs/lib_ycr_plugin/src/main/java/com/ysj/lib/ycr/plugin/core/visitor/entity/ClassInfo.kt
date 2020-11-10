package com.ysj.lib.ycr.plugin.core.visitor.entity

/**
 * Class 的信息
 *
 * @author Ysj
 * Create time: 2020/8/16
 */
class ClassInfo(
    val version: Int,
    val access: Int,
    name: String? = "",
    signature: String? = "",
    superName: String? = "",
    interfaces: Array<out String>? = arrayOf()
) {
    val name: String = name ?: ""
    val signature: String = signature ?: ""
    val superName: String = superName ?: ""
    val interfaces: Array<out String> = interfaces ?: arrayOf()

    fun simpleName() = name.split("/").last()

    override fun toString(): String {
        return """
                ClassInfo:
                version=$version
                access=$access
                name=$name
                signature=$signature
                superName=$superName
                interfaces=${interfaces.contentToString()}
        """.trimIndent()
    }

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as ClassInfo

        if (version != other.version) return false
        if (access != other.access) return false
        if (name != other.name) return false
        if (signature != other.signature) return false
        if (superName != other.superName) return false
        if (!interfaces.contentEquals(other.interfaces)) return false

        return true
    }

    override fun hashCode(): Int {
        var result = version
        result = 31 * result + access
        result = 31 * result + name.hashCode()
        result = 31 * result + signature.hashCode()
        result = 31 * result + superName.hashCode()
        result = 31 * result + interfaces.contentHashCode()
        return result
    }
}