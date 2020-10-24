package com.ysj.lib.route.plugin.core.visitor.method

import com.ysj.lib.route.plugin.core.logger.YLogger
import com.ysj.lib.route.plugin.core.visitor.BaseClassVisitor
import com.ysj.lib.route.plugin.core.visitor.entity.MethodInfo
import org.objectweb.asm.ClassVisitor
import org.objectweb.asm.MethodVisitor
import org.objectweb.asm.Opcodes

/**
 * 基类 [MethodVisitor]，需配合 [BaseClassVisitor] 使用
 *
 * 要使此类生效，记得 [attach]
 *
 * @author Ysj
 * Create time: 2020/8/16
 */
abstract class BaseMethodVisitor(val methodInfo: MethodInfo) : MethodVisitor(Opcodes.ASM7) {

    protected val logger = YLogger.getLogger(javaClass)

    lateinit var bcv: BaseClassVisitor
        private set

    lateinit var cv: ClassVisitor
        private set

    /** 该 [MethodVisitor] 的匹配规则 */
    abstract fun match(bcv: BaseClassVisitor): Boolean

    open fun attach(bcv: BaseClassVisitor) {
        this.bcv = bcv
        this.cv = bcv.getClassVisitor()
        this.mv = visitMethod(
            methodInfo.access,
            methodInfo.name,
            methodInfo.descriptor,
            methodInfo.signature,
            methodInfo.exceptions
        )
    }

    open fun visitMethod(
        access: Int,
        name: String?,
        descriptor: String?,
        signature: String?,
        exceptions: Array<out String>?
    ): MethodVisitor = cv.visitMethod(access, name, descriptor, signature, exceptions)
}