package com.ysj.lib.route.plugin.core.visitor.field

import com.ysj.lib.route.plugin.core.logger.YLogger
import com.ysj.lib.route.plugin.core.visitor.BaseClassVisitor
import com.ysj.lib.route.plugin.core.visitor.entity.FieldInfo
import org.objectweb.asm.ClassVisitor
import org.objectweb.asm.FieldVisitor
import org.objectweb.asm.Opcodes

/**
 * 基类 [FieldVisitor]，需配合 [BaseClassVisitor] 使用
 *
 * 要使此类生效，记得 [attach]
 *
 * @author Ysj
 * Create time: 2020/9/30
 */
abstract class BaseFieldVisitor(val fieldInfo: FieldInfo) : FieldVisitor(Opcodes.ASM7) {

    protected val logger = YLogger.getLogger(javaClass)

    lateinit var bcv: BaseClassVisitor
        private set

    lateinit var cv: ClassVisitor
        private set

    /** 该 [FieldVisitor] 的匹配规则 */
    abstract fun match(bcv: BaseClassVisitor): Boolean

    fun attach(bcv: BaseClassVisitor) {
        this.bcv = bcv
        this.cv = bcv.getClassVisitor()
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