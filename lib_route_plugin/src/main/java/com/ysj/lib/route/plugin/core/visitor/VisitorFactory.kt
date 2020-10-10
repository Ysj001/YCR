package com.ysj.lib.route.plugin.core.visitor

import com.ysj.lib.route.plugin.core.visitor.entity.ClassInfo
import com.ysj.lib.route.plugin.core.visitor.entity.MethodInfo
import com.ysj.lib.route.plugin.core.visitor.field.BaseFieldVisitor
import com.ysj.lib.route.plugin.core.visitor.method.BaseMethodVisitor
import com.ysj.lib.route.plugin.core.visitor.method.OnCreateVisitor
import com.ysj.lib.route.plugin.core.visitor.method.RouteCacheClInitVisitor
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
        OnCreateVisitor(),
        RouteCacheClInitVisitor()
    )

    /**
     * 获取 [MethodVisitor]
     */
    fun get(classInfo: ClassInfo, methodInfo: MethodInfo, mv: MethodVisitor): MethodVisitor {
        methodVisitors.forEach {
            if (it.match(classInfo, methodInfo)) return it.also {
                it.classInfo = classInfo
                it.attach(mv)
            }
        }
        return mv
    }

}