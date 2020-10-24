package com.ysj.lib.route.plugin.core.visitor.method

import com.ysj.lib.route.plugin.core.visitor.BaseClassVisitor
import com.ysj.lib.route.plugin.core.visitor.PreVisitor
import com.ysj.lib.route.plugin.core.visitor.entity.MethodInfo
import org.objectweb.asm.Opcodes

/**
 * 处理 Caches 的静态初始化块
 *
 * @author Ysj
 * Create time: 2020/10/7
 */
class RouteCacheClInitVisitor : BaseMethodVisitor(
    MethodInfo(
        Opcodes.ACC_STATIC,
        "<clinit>",
        "()V"
    )
) {

    override fun match(bcv: BaseClassVisitor): Boolean =
        bcv.classInfo.name == "com/ysj/lib/route/Caches" && methodInfo == bcv.methodInfo

    override fun visitInsn(opcode: Int) {
        if (opcode == Opcodes.RETURN) with(mv) {
            // init {
            //    interceptors.add(xxx)
            // }
            PreVisitor.cacheClassInfo
                .filter { it.interfaces.contains("com/ysj/lib/route/template/IInterceptor") }
                .forEach {
                    visitFieldInsn(
                        Opcodes.GETSTATIC,
                        bcv.classInfo.name,
                        "interceptors",
                        "Ljava/util/ArrayList;"
                    )
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
                        "java/util/ArrayList",
                        "add",
                        "(Ljava/lang/Object;)Z",
                        false
                    )
                    visitInsn(Opcodes.POP)
                    logger.lifecycle("注册了 ${it.name}")
                }
        }
        super.visitInsn(opcode)
    }
}