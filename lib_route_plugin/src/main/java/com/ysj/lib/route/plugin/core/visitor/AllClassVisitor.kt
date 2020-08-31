package com.ysj.lib.route.plugin.core.visitor

import com.android.build.gradle.internal.LoggerWrapper
import com.ysj.lib.route.plugin.core.visitor.entity.ClassInfo
import org.objectweb.asm.AnnotationVisitor
import org.objectweb.asm.ClassVisitor
import org.objectweb.asm.Opcodes
import org.objectweb.asm.TypePath

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
        logger.quiet("class visitor: $classInfo")
    }

    override fun visitAnnotation(descriptor: String?, visible: Boolean): AnnotationVisitor {
        logger.quiet("annotation visitor: $descriptor , $visible")
        return super.visitAnnotation(descriptor, visible)
    }

    override fun visitTypeAnnotation(
        typeRef: Int,
        typePath: TypePath?,
        descriptor: String?,
        visible: Boolean
    ): AnnotationVisitor {
        logger.quiet("type annotation visitor: $descriptor , $visible , $typeRef , $typePath")
        return super.visitTypeAnnotation(typeRef, typePath, descriptor, visible)
    }

//    override fun visitMethod(
//        access: Int,
//        name: String?,
//        descriptor: String?,
//        signature: String?,
//        exceptions: Array<out String>?
//    ): MethodVisitor? {
//        val mv = super.visitMethod(access, name, descriptor, signature, exceptions)
//        val methodInfo = MethodInfo(access, name, descriptor, signature, exceptions)
//        logger.quiet("method visitor: $methodInfo")
//        return MethodVisitorFactory.get(classInfo, methodInfo, mv)
//    }

    private fun checkAccess(access: Int, flag: Int): Boolean {
        return (access and flag) == flag
    }
}