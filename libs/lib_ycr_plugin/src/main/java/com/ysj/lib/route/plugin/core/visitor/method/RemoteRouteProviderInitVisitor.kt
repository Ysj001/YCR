package com.ysj.lib.route.plugin.core.visitor.method

import com.ysj.lib.route.plugin.core.RouteTransform
import com.ysj.lib.route.plugin.core.visitor.entity.ClassInfo
import com.ysj.lib.route.plugin.core.visitor.entity.MethodInfo
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

    override fun match(classInfo: ClassInfo, methodInfo: MethodInfo) =
        classInfo.name == "com/ysj/lib/route/remote/RemoteRouteProvider" && this.methodInfo == methodInfo

    override fun visitLdcInsn(value: Any?) = super.visitLdcInsn(
        if (value == "auto inject your main application id") RouteTransform.mainModuleAppExt.defaultConfig.applicationId
        else value
    )

}