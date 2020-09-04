package com.ysj.lib.route.plugin.core.visitor.method

import com.android.build.gradle.internal.LoggerWrapper
import com.ysj.lib.route.plugin.core.visitor.VisitorFactory
import com.ysj.lib.route.plugin.core.visitor.entity.ClassInfo
import com.ysj.lib.route.plugin.core.visitor.entity.MethodInfo
import org.objectweb.asm.MethodVisitor
import org.objectweb.asm.Opcodes

/**
 * 基类 [MethodVisitor]，建议配合 [VisitorFactory] 使用
 *
 * 要使此类生效，记得 [setMethodVisitor]
 *
 * @author Ysj
 * Create time: 2020/8/16
 */
abstract class BaseMethodVisitor(val methodInfo: MethodInfo) :
    MethodVisitor(Opcodes.ASM7) {

    protected val logger = LoggerWrapper.getLogger(javaClass)

    /**
     * 使用 [VisitorFactory.get] 时可以自动注入
     */
    lateinit var classInfo: ClassInfo

    /**
     * 该 [MethodVisitor] 的匹配规则
     */
    abstract fun match(classInfo: ClassInfo, methodInfo: MethodInfo): Boolean

    fun setMethodVisitor(mv: MethodVisitor) {
        this.mv = mv
    }

}