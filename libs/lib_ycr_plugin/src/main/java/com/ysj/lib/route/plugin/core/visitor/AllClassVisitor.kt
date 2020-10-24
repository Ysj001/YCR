package com.ysj.lib.route.plugin.core.visitor

import com.android.build.api.transform.Transform
import com.ysj.lib.route.plugin.core.visitor.field.BaseFieldVisitor
import com.ysj.lib.route.plugin.core.visitor.method.BaseMethodVisitor
import com.ysj.lib.route.plugin.core.visitor.method.RemoteRouteProviderInitVisitor
import com.ysj.lib.route.plugin.core.visitor.method.RemoteRouteProviderOnCreateVisitor
import com.ysj.lib.route.plugin.core.visitor.method.RouteCacheClInitVisitor
import org.objectweb.asm.ClassVisitor

/**
 * 处理所有 class
 *
 * @author Ysj
 * Create time: 2020/8/14
 */
class AllClassVisitor(transform: Transform, visitor: ClassVisitor) :
    BaseClassVisitor(transform, visitor) {

    override val fieldVisitors: ArrayList<BaseFieldVisitor> = arrayListOf()

    override val methodVisitors: ArrayList<BaseMethodVisitor> = arrayListOf(
        RemoteRouteProviderInitVisitor(),
        RemoteRouteProviderOnCreateVisitor(),
        RouteCacheClInitVisitor()
    )

}