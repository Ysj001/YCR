package com.ysj.lib.ycr.plugin.core.visitor.method

import com.ysj.lib.ycr.plugin.core.CLASS_Caches
import com.ysj.lib.ycr.plugin.core.CLASS_IGlobalExceptionProcessor
import com.ysj.lib.ycr.plugin.core.CLASS_IGlobalInterceptor
import com.ysj.lib.ycr.plugin.core.RouteTransform
import com.ysj.lib.ycr.plugin.core.visitor.BaseClassVisitor
import com.ysj.lib.ycr.plugin.core.visitor.entity.ClassInfo
import com.ysj.lib.ycr.plugin.core.visitor.entity.MethodInfo
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
        bcv.classInfo.name == CLASS_Caches && methodInfo == bcv.methodInfo

    override fun visitInsn(opcode: Int) {
        if (opcode == Opcodes.RETURN) with(mv) {
            // init {
            //    interceptors.add(xxx)
            //    globalExceptionProcessors.add(xxx)
            // }
            (bcv.transform as RouteTransform).cacheClassInfo
                .filter(::filter)
                .forEach {
                    visitFieldInsn(
                        Opcodes.GETSTATIC,
                        bcv.classInfo.name,
                        if (hasInterface(it, CLASS_IGlobalInterceptor)) "interceptors"
                        else "globalExceptionProcessors",
                        "Ljava/util/TreeSet;"
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
                        "java/util/TreeSet",
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

    private fun filter(classInfo: ClassInfo): Boolean {
        if (classInfo.access and Opcodes.ACC_ABSTRACT == Opcodes.ACC_ABSTRACT) return false
        return hasInterface(classInfo, CLASS_IGlobalInterceptor, CLASS_IGlobalExceptionProcessor)
    }

    private fun hasInterface(classInfo: ClassInfo?, vararg interfaceName: String): Boolean {
        classInfo ?: return false
        var result = interfaceName.find { it in classInfo.interfaces } != null
        if (!result) {
            result = hasInterface(
                (bcv.transform as RouteTransform)
                    .cacheClassInfo
                    .find { it.name == classInfo.superName },
                *interfaceName
            )
        }
        return result
    }
}