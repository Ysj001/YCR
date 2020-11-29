package com.ysj.lib.ycr.plugin.core.visitor

import com.ysj.lib.ycr.plugin.core.*
import com.ysj.lib.ycr.plugin.core.logger.YLogger
import com.ysj.lib.ycr.plugin.core.visitor.entity.ClassInfo
import org.objectweb.asm.ClassVisitor
import org.objectweb.asm.Opcodes

/**
 * 用于提前获取信息
 *
 * @author Ysj
 * Create time: 2020/8/14
 */
class PreVisitor(val tf: RouteTransform, visitor: ClassVisitor) :
    ClassVisitor(Opcodes.ASM7, visitor) {

    private val logger = YLogger.getLogger(javaClass)

    override fun visit(
        version: Int,
        access: Int,
        name: String,
        signature: String?,
        superName: String?,
        interfaces: Array<out String>?
    ) {
        super.visit(version, access, name, signature, superName, interfaces)
        if (checkYCRInterface(interfaces)) {
            if (interfaces!!.contains(CLASS_IExecutorProvider)) {
                tf.executorProviderClassName = name
            }
            val classInfo = ClassInfo(
                version,
                access,
                name,
                signature,
                superName,
                interfaces
            )
            tf.cacheClassInfo.add(classInfo)
//            logger.quiet("cache class: $classInfo")
        }
    }

    private fun checkYCRInterface(interfaces: Array<out String>?): Boolean {
        if (interfaces.isNullOrEmpty()) return false
        return interfaces.contains(CLASS_IProviderRoute)
                || interfaces.contains(CLASS_IGlobalInterceptor)
                || interfaces.contains(CLASS_IGlobalExceptionProcessor)
                || interfaces.contains(CLASS_IExecutorProvider)
    }
}