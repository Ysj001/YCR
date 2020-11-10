package com.ysj.lib.ycr.plugin.core.visitor.method

import com.ysj.lib.ycr.plugin.core.RouteTransform
import com.ysj.lib.ycr.plugin.core.visitor.BaseClassVisitor
import com.ysj.lib.ycr.plugin.core.visitor.PreVisitor
import com.ysj.lib.ycr.plugin.core.visitor.entity.MethodInfo
import org.objectweb.asm.Opcodes

/**
 * 处理 YCR 中的 getCustomExecutor
 *
 * @author Ysj
 * Create time: 2020/10/7
 */
class YCRGetCustomExecutorVisitor : BaseMethodVisitor(
    MethodInfo(
        Opcodes.ACC_PRIVATE or Opcodes.ACC_FINAL,
        "getCustomExecutor",
        "()Ljava/util/concurrent/ThreadPoolExecutor;"
    )
) {

    override fun match(bcv: BaseClassVisitor): Boolean =
        bcv.classInfo.name == "com/ysj/lib/ycr/YCR\$Companion" && methodInfo == bcv.methodInfo

    override fun visitInsn(opcode: Int) {
        if (opcode == Opcodes.ARETURN) with(mv) {
            // private fun getCustomExecutor(): ThreadPoolExecutor? = XXX().providerExecutor()
            (bcv.transform as RouteTransform).cacheClassInfo
                .filter { it.interfaces.contains("com/ysj/lib/ycr/template/IExecutorProvider") }
                .forEach {
                    visitTypeInsn(Opcodes.NEW, it.name)
                    visitInsn(Opcodes.DUP)
                    visitMethodInsn(
                        Opcodes.INVOKESPECIAL,
                        it.name,
                        "<init>",
                        "()V",
                        false
                    )
                    visitMethodInsn(
                        Opcodes.INVOKEVIRTUAL,
                        it.name,
                        "providerExecutor",
                        "()Ljava/util/concurrent/ThreadPoolExecutor;",
                        false
                    )
                    visitInsn(Opcodes.ARETURN)
                    logger.lifecycle("注册了 ${it.name}")
                }
            return
        }
        super.visitInsn(opcode)
    }
}