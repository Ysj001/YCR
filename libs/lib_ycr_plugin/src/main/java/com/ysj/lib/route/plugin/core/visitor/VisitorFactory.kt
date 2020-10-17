package com.ysj.lib.route.plugin.core.visitor

import com.ysj.lib.route.plugin.core.visitor.entity.ClassInfo
import com.ysj.lib.route.plugin.core.visitor.entity.FieldInfo
import com.ysj.lib.route.plugin.core.visitor.entity.MethodInfo
import com.ysj.lib.route.plugin.core.visitor.field.BaseFieldVisitor
import com.ysj.lib.route.plugin.core.visitor.method.BaseMethodVisitor
import com.ysj.lib.route.plugin.core.visitor.method.RemoteRouteProviderInitVisitor
import com.ysj.lib.route.plugin.core.visitor.method.RemoteRouteProviderOnCreateVisitor
import com.ysj.lib.route.plugin.core.visitor.method.RouteCacheClInitVisitor
import org.objectweb.asm.ClassVisitor
import org.objectweb.asm.FieldVisitor
import org.objectweb.asm.MethodVisitor

/**
 * 用于创建 Visitor 的工厂
 *
 * @author Ysj
 * Create time: 2020/8/16
 */
object VisitorFactory {

    private val fieldVisitors: ArrayList<BaseFieldVisitor> = arrayListOf(
    )

    private val methodVisitors: ArrayList<BaseMethodVisitor> = arrayListOf(
        RemoteRouteProviderInitVisitor(),
        RemoteRouteProviderOnCreateVisitor(),
        RouteCacheClInitVisitor()
    )

    /**
     * 获取 [MethodVisitor]
     */
    fun get(classInfo: ClassInfo, methodInfo: MethodInfo, cv: ClassVisitor): MethodVisitor {
        methodVisitors.forEach {
            if (it.match(classInfo, methodInfo)) return it.also {
                it.classInfo = classInfo
                it.attach(cv)
            }
        }
        return cv.visitMethod(
            methodInfo.access,
            methodInfo.name,
            methodInfo.descriptor,
            methodInfo.signature,
            methodInfo.exceptions
        )
    }

    /**
     * 获取 [FieldVisitor]
     */
    fun get(classInfo: ClassInfo, fieldInfo: FieldInfo, cv: ClassVisitor): FieldVisitor {
        fieldVisitors.forEach {
            if (it.match(classInfo, fieldInfo)) return it.also {
                it.classInfo = classInfo
                it.attach(cv)
            }
        }
        return cv.visitField(
            fieldInfo.access,
            fieldInfo.name,
            fieldInfo.descriptor,
            fieldInfo.signature,
            fieldInfo.value
        )
    }

}