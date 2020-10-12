package com.ysj.lib.route.plugin.core.visitor

import com.android.build.gradle.internal.LoggerWrapper
import com.ysj.lib.route.plugin.core.visitor.entity.ClassInfo
import com.ysj.lib.route.plugin.core.visitor.entity.FieldInfo
import com.ysj.lib.route.plugin.core.visitor.entity.MethodInfo
import org.objectweb.asm.ClassVisitor
import org.objectweb.asm.FieldVisitor
import org.objectweb.asm.MethodVisitor
import org.objectweb.asm.Opcodes

/**
 * 处理所有 class
 *
 * @author Ysj
 * Create time: 2020/8/14
 */
class AllClassVisitor(visitor: ClassVisitor) : ClassVisitor(Opcodes.ASM7, visitor) {

    private val logger = LoggerWrapper.getLogger(javaClass)

    private lateinit var classInfo: ClassInfo

    override fun visit(
        version: Int,
        access: Int,
        name: String?,
        signature: String?,
        superName: String?,
        interfaces: Array<out String>?
    ) {
        super.visit(version, access, name, signature, superName, interfaces)
        classInfo = ClassInfo(
            version,
            access,
            name,
            signature,
            superName,
            interfaces
        )
//        logger.quiet("class visitor: $classInfo")
    }

    override fun visitField(
        access: Int,
        name: String?,
        descriptor: String?,
        signature: String?,
        value: Any?
    ): FieldVisitor {
        val fieldInfo = FieldInfo(access, name, descriptor, signature, value)
        return VisitorFactory.get(classInfo, fieldInfo, cv)
    }

    override fun visitMethod(
        access: Int,
        name: String?,
        descriptor: String?,
        signature: String?,
        exceptions: Array<out String>?
    ): MethodVisitor {
        val methodInfo = MethodInfo(access, name, descriptor, signature, exceptions)
        return VisitorFactory.get(classInfo, methodInfo, cv)
    }

}