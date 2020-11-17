package com.ysj.lib.ycr.plugin.core.visitor.method

import com.ysj.lib.ycr.plugin.core.CLASS_IProviderRoute
import com.ysj.lib.ycr.plugin.core.CLASS_RouteBean
import com.ysj.lib.ycr.plugin.core.RouteTransform
import com.ysj.lib.ycr.plugin.core.visitor.BaseClassVisitor
import com.ysj.lib.ycr.plugin.core.visitor.entity.MethodInfo
import org.objectweb.asm.Opcodes

/**
 * 处理 IRouteProvider 中的 loadInto(Ljava/util/Map;)V 方法
 *
 * @author Ysj
 * Create time: 2020/8/16
 */
class IRouteProviderLoadIntoVisitor : BaseMethodVisitor(
    MethodInfo(
        Opcodes.ACC_PUBLIC,
        "loadInto",
        "(Ljava/util/Map;)V",
        "(Ljava/util/Map<Ljava/lang/String;L$CLASS_RouteBean;>;)V"
    )
) {

    private var showLog = true

    override fun match(bcv: BaseClassVisitor): Boolean =
        bcv.classInfo.interfaces.contains(CLASS_IProviderRoute)
                && bcv.methodInfo == methodInfo

    override fun visitLdcInsn(value: Any?) = super.visitLdcInsn(
        if (value != "It is automatically modified to 'application id' at compile time") value
        else (bcv.transform as RouteTransform).moduleAppExt.defaultConfig.applicationId.also {
            if (showLog) logger.lifecycle("${bcv.classInfo.simpleName()} loadInto application id --> $it")
            showLog = false
        }
    )

}