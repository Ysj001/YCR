package com.ysj.lib.ycr.plugin.core.visitor

import com.android.build.api.transform.Transform
import com.ysj.lib.ycr.plugin.core.logger.YLogger
import com.ysj.lib.ycr.plugin.core.visitor.entity.ClassInfo
import com.ysj.lib.ycr.plugin.core.visitor.entity.FieldInfo
import com.ysj.lib.ycr.plugin.core.visitor.entity.MethodInfo
import com.ysj.lib.ycr.plugin.core.visitor.field.BaseFieldVisitor
import com.ysj.lib.ycr.plugin.core.visitor.method.BaseMethodVisitor
import org.objectweb.asm.ClassVisitor
import org.objectweb.asm.FieldVisitor
import org.objectweb.asm.MethodVisitor
import org.objectweb.asm.Opcodes

/**
 *
 *
 * @author Ysj
 * Create time: 2020/10/23
 */
abstract class BaseClassVisitor(val transform: Transform, visitor: ClassVisitor?) :
    ClassVisitor(Opcodes.ASM7, visitor) {

    protected val logger = YLogger.getLogger(javaClass)

    abstract val fieldVisitors: ArrayList<BaseFieldVisitor>

    abstract val methodVisitors: ArrayList<BaseMethodVisitor>

    lateinit var classInfo: ClassInfo
        private set

    lateinit var fieldInfo: FieldInfo
        private set

    lateinit var methodInfo: MethodInfo
        private set

    override fun visit(
        version: Int,
        access: Int,
        name: String?,
        signature: String?,
        superName: String?,
        interfaces: Array<out String>?
    ) {
        super.visit(version, access, name, signature, superName, interfaces)
        classInfo = ClassInfo(
            version,
            access,
            name,
            signature,
            superName,
            interfaces
        )
    }

    override fun visitField(
        access: Int,
        name: String?,
        descriptor: String?,
        signature: String?,
        value: Any?
    ): FieldVisitor {
        fieldInfo = FieldInfo(access, name, descriptor, signature, value)
        return getFieldVisitor()
    }

    override fun visitMethod(
        access: Int,
        name: String?,
        descriptor: String?,
        signature: String?,
        exceptions: Array<out String>?
    ): MethodVisitor {
        methodInfo = MethodInfo(access, name, descriptor, signature, exceptions)
        return getMethodVisitor()
    }

    /**
     * 获取 [MethodVisitor]
     */
    protected open fun getMethodVisitor(): MethodVisitor {
        methodVisitors.forEach {
            if (it.match(this)) return it.also { it.attach(this) }
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
    protected open fun getFieldVisitor(): FieldVisitor {
        fieldVisitors.forEach {
            if (it.match(this)) return it.also { it.attach(this) }
        }
        return cv.visitField(
            fieldInfo.access,
            fieldInfo.name,
            fieldInfo.descriptor,
            fieldInfo.signature,
            fieldInfo.value
        )
    }

    internal fun getClassVisitor() = cv

}