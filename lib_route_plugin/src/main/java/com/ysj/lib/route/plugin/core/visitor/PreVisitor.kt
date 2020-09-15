package com.ysj.lib.route.plugin.core.visitor

import com.android.build.gradle.internal.LoggerWrapper
import com.ysj.lib.route.plugin.core.visitor.entity.ClassInfo
import org.objectweb.asm.ClassVisitor
import org.objectweb.asm.Opcodes

/**
 * 用于提前获取信息
 *
 * @author Ysj
 * Create time: 2020/8/14
 */
class PreVisitor(visitor: ClassVisitor) : ClassVisitor(Opcodes.ASM7, visitor) {

    companion object {
        val cacheClassInfo = HashSet<ClassInfo>()
    }

    private val logger = LoggerWrapper.getLogger(javaClass)

    override fun visit(
        version: Int,
        access: Int,
        name: String?,
        signature: String?,
        superName: String?,
        interfaces: Array<out String>?
    ) {
        super.visit(version, access, name, signature, superName, interfaces)
        if (interfaces != null && interfaces.contains("com/ysj/lib/route/template/IProviderRoute")) {
            val classInfo = ClassInfo(
                version,
                access,
                name,
                signature,
                superName,
                interfaces
            )
            cacheClassInfo.add(classInfo)
//            logger.quiet("cache class: $classInfo")
        }
    }

}