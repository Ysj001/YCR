package com.ysj.lib.route.plugin.core.visitor.field

import com.android.build.gradle.internal.LoggerWrapper
import com.ysj.lib.route.plugin.core.visitor.VisitorFactory
import com.ysj.lib.route.plugin.core.visitor.entity.ClassInfo
import com.ysj.lib.route.plugin.core.visitor.entity.FieldInfo
import org.objectweb.asm.ClassVisitor
import org.objectweb.asm.FieldVisitor
import org.objectweb.asm.Opcodes

/**
 * 基类 [FieldVisitor]，需配合 [VisitorFactory] 使用
 *
 * 要使此类生效，记得 [attach]
 *
 * @author Ysj
 * Create time: 2020/9/30
 */
abstract class BaseFieldVisitor(val fieldInfo: FieldInfo) : FieldVisitor(Opcodes.ASM7) {

    protected val logger = LoggerWrapper.getLogger(javaClass)

    lateinit var cv: ClassVisitor

    /** 从 [VisitorFactory.get] 自动注入 */
    lateinit var classInfo: ClassInfo

    /** 该 [FieldVisitor] 的匹配规则 */
    abstract fun match(classInfo: ClassInfo, fieldInfo: FieldInfo): Boolean

    fun attach(cv: ClassVisitor) {
        this.cv = cv
        this.fv = visitField(
            fieldInfo.access,
            fieldInfo.name,
            fieldInfo.descriptor,
            fieldInfo.signature,
            fieldInfo.value
        )
    }

    open fun visitField(
        access: Int,
        name: String?,
        descriptor: String?,
        signature: String?,
        value: Any?
    ): FieldVisitor = cv.visitField(access, name, descriptor, signature, value)

}