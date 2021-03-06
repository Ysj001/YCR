package com.ysj.lib.ycr.plugin.core.visitor.method

import com.ysj.lib.ycr.plugin.core.CLASS_IProviderRoute
import com.ysj.lib.ycr.plugin.core.CLASS_RemoteRouteProvider
import com.ysj.lib.ycr.plugin.core.RouteTransform
import com.ysj.lib.ycr.plugin.core.visitor.BaseClassVisitor
import com.ysj.lib.ycr.plugin.core.visitor.entity.MethodInfo
import org.objectweb.asm.Opcodes

/**
 * 处理 RemoteRouteProvider 中的 onCreate 方法
 *
 * @author Ysj
 * Create time: 2020/8/16
 */
class RemoteRouteProviderOnCreateVisitor : BaseMethodVisitor(
    MethodInfo(
        Opcodes.ACC_PUBLIC,
        "onCreate",
        "()Z"
    )
) {

    override fun match(bcv: BaseClassVisitor): Boolean =
        bcv.classInfo.name == CLASS_RemoteRouteProvider && bcv.methodInfo == methodInfo

    override fun visitInsn(opcode: Int) {
        if (opcode == Opcodes.IRETURN) with(mv) {
            // registerRouteGroup("xxx class name")
            (bcv.transform as RouteTransform).cacheClassInfo
                .filter { it.interfaces.contains(CLASS_IProviderRoute) }
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
                        bcv.classInfo.name,
                        "registerRouteGroup",
                        "(L$CLASS_IProviderRoute;)V",
                        false
                    )
                    logger.lifecycle("注册了 ${it.name}")
                }
        }
        super.visitInsn(opcode)
    }

}