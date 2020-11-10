package com.ysj.lib.ycr.plugin.core.visitor.method

import com.ysj.lib.ycr.plugin.core.RouteTransform
import com.ysj.lib.ycr.plugin.core.visitor.BaseClassVisitor
import com.ysj.lib.ycr.plugin.core.visitor.entity.MethodInfo
import org.objectweb.asm.Opcodes

/**
 * 处理 RemoteRouteProvider 中的 <init>()V 方法
 *
 * @author Ysj
 * Create time: 2020/8/16
 */
class RemoteRouteProviderInitVisitor : BaseMethodVisitor(
    MethodInfo(
        Opcodes.ACC_PUBLIC,
        "<init>",
        "()V"
    )
) {

    override fun match(bcv: BaseClassVisitor): Boolean =
        bcv.classInfo.name == "com/ysj/lib/ycr/remote/RemoteRouteProvider" && bcv.methodInfo == methodInfo

    override fun visitLdcInsn(value: Any?) = super.visitLdcInsn(
        if (value != "It is automatically modified to 'main application id' at compile time") value
        else (bcv.transform as RouteTransform).mainModuleAppExt.defaultConfig.applicationId.also {
            logger.lifecycle("RemoteRouteProvider init application id --> $it")
        }
    )

}