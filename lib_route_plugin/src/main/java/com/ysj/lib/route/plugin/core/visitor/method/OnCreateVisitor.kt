package com.ysj.lib.route.plugin.core.visitor.method

import com.ysj.lib.route.plugin.core.visitor.PreVisitor
import com.ysj.lib.route.plugin.core.visitor.entity.ClassInfo
import com.ysj.lib.route.plugin.core.visitor.entity.MethodInfo
import org.objectweb.asm.Opcodes

/**
 * 处理 RouteProvider 中的 onCreate 方法
 *
 * @author Ysj
 * Create time: 2020/8/16
 */
class OnCreateVisitor : BaseMethodVisitor(
    MethodInfo(
        Opcodes.ACC_PUBLIC,
        "onCreate",
        "()Z"
    )
) {

    override fun match(classInfo: ClassInfo, methodInfo: MethodInfo) =
            classInfo.name == "com/ysj/lib/route/RouteProvider" && this.methodInfo == methodInfo

    override fun visitInsn(opcode: Int) {
        if (opcode == Opcodes.IRETURN) with(mv) {
            // registerRouteGroup("xxx class name")
            PreVisitor.cacheClassInfo
                .filter { it.interfaces.contains("com/ysj/lib/route/template/IProviderRoute") }
                .forEach {
                    visitVarInsn(Opcodes.ALOAD, 0)
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
                        Opcodes.INVOKESPECIAL,
                        "com/ysj/lib/route/RouteProvider",
                        "registerRouteGroup",
                        "(Lcom/ysj/lib/route/template/IProviderRoute;)V",
                        false
                    )
                    logger.quiet("注册了 ${it.name}")
                }
        }
        super.visitInsn(opcode)
    }

}