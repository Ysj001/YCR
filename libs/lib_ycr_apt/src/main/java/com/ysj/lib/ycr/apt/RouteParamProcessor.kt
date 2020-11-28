package com.ysj.lib.ycr.apt

import com.google.auto.service.AutoService
import com.squareup.kotlinpoet.*
import com.ysj.lib.ycr.annotation.RouteParam
import com.ysj.lib.ycr.annotation.SUFFIX_ROUTE_PARAM
import java.io.Serializable
import java.util.*
import javax.annotation.processing.*
import javax.lang.model.SourceVersion
import javax.lang.model.element.Element
import javax.lang.model.element.TypeElement
import javax.lang.model.type.TypeKind

/**
 * 处理 [RouteParam]
 *
 * @author Ysj
 * Create time: 2020/11/19
 */
@AutoService(Processor::class)
// JDK 编译版本
@SupportedSourceVersion(SourceVersion.RELEASE_8)
// 要处理的注解
@SupportedAnnotationTypes(ANNOTATION_TYPE_PARAMETER)
class RouteParamProcessor : BaseProcess() {

    companion object {

        const val TEMPLATE_PATH = "com.ysj.lib.ycr.template.IProviderParam"

        private val routeParamClass = RouteParam::class.java
    }

    override fun init(processingEnv: ProcessingEnvironment) {
        super.init(processingEnv)
        printlnMessage("YCR APT 开始处理...")
    }

    override fun process(
        annotations: MutableSet<out TypeElement>,
        roundEnv: RoundEnvironment
    ): Boolean {
        if (annotations.isEmpty()) return false
        // 获取所有被 @Route 注解的 element
        val elements = roundEnv.getElementsAnnotatedWith(routeParamClass)
        if (elements.isEmpty()) return false
        try {
            processParameter(elements)
        } catch (e: java.lang.Exception) {
            printlnError("@${routeParamClass.simpleName} 注解处理失败: ${e.message}")
        }
        return true
    }

    @Throws(Exception::class)
    private fun processParameter(elements: Set<Element>) {
        val parameterMap: MutableMap<TypeElement, MutableList<Element>> = HashMap()
        for (element in elements) {
            // 由于该注解作用在参数上，因此这里获取到的是类元素
            val typeElement = element.enclosingElement as TypeElement
            var fieldElements: MutableList<Element>? = parameterMap[typeElement]
            if (fieldElements == null) {
                fieldElements = ArrayList()
            }
            fieldElements.add(element)
            parameterMap[typeElement] = fieldElements
        }
        createFile(parameterMap)
    }

    @Throws(Exception::class)
    private fun createFile(parameterMap: MutableMap<TypeElement, MutableList<Element>>) {
        if (parameterMap.isEmpty()) return
        // Activity 的类型
        val activityType = elementUtils.getTypeElement(AFFECT_ACTIVITY).asType()
        // 生成类似如下方法：
        // override fun injectParam(target: Any) {
        //     target as MainActivity;
        //     target.aaaa = target.intent.getStringExtra("aaaa");
        //     target.bbbb = target.intent.getIntExtra("bbbb", target.bbbb);
        // }
        val funInjectParam = FunSpec.builder("injectParam")
            .addModifiers(KModifier.PUBLIC, KModifier.OVERRIDE)
            .addParameter("target", ANY)
        parameterMap.forEach { entry ->
            val type = entry.key
            val typeClass = ClassName.bestGuess(type.qualifiedName.toString())
            if (!typeUtils.isSubtype(type.asType(), activityType)) throw RuntimeException(
                "@${routeParamClass.simpleName} 注解只能作用在：$AFFECT_ACTIVITY"
            )
            funInjectParam.clearBody()
                .addStatement("target as %T", typeClass)
            entry.value.forEach { funInjectParam.addStatement(getStatement(it)) }
            // 生成类文件
            val typeSpec = TypeSpec.classBuilder(type.simpleName.toString() + SUFFIX_ROUTE_PARAM)
                .addModifiers(KModifier.PUBLIC)
                .addSuperinterface(ClassName.bestGuess(TEMPLATE_PATH))
                .addFunction(funInjectParam.build())
                .build()
            FileSpec.builder(typeClass.packageName, typeSpec.name!!).addType(typeSpec).build()
                .writeTo(filer)
            printlnMessage("@${routeParamClass.simpleName} --- 已处理：class:${type.simpleName}")
        }
    }

    private fun getStatement(fieldElement: Element): String {
        // 获取 field 上的注解
        val parameter: RouteParam = fieldElement.getAnnotation(routeParamClass)
        val fieldName = fieldElement.simpleName.toString()
        var name: String = parameter.name
        if (name.isEmpty()) name = fieldName
        val typeMirror = fieldElement.asType()
        val variable = "target.$fieldName"
        val head = "$variable = target.intent."
        return when (typeMirror.kind) {
            TypeKind.BOOLEAN -> """${head}getBooleanExtra("$name", $variable)"""
            TypeKind.BYTE -> """${head}getByteExtra("$name", $variable)"""
            TypeKind.INT -> """${head}getIntExtra("$name", $variable)"""
            TypeKind.SHORT -> """${head}getShortExtra("$name", $variable)"""
            TypeKind.LONG -> """${head}getLongExtra("$name", $variable)"""
            TypeKind.FLOAT -> """${head}getFloatExtra("$name", $variable)"""
            TypeKind.DOUBLE -> """${head}getDoubleExtra("$name", $variable)"""
            else -> {
                val typeName = typeMirror.toString()
                when {
                    typeName == String::class.java.name -> """${head}getStringExtra("$name")"""
                    typeUtils.isSubtype(
                        typeMirror,
                        elementUtils.getTypeElement(Serializable::class.java.name).asType()
                    ) -> """${head}getSerializableExtra("$name") as $typeName"""
                    typeUtils.isSubtype(
                        typeMirror,
                        elementUtils.getTypeElement("android.os.Bundle").asType()
                    ) -> """${head}getBundleExtra("$name")"""
                    typeUtils.isSubtype(
                        typeMirror,
                        elementUtils.getTypeElement("android.os.Parcelable").asType()
                    ) -> """${head}getParcelableExtra("$name") as $typeName"""
                    else -> throw RuntimeException("@${routeParamClass.simpleName} 注解不支持该类型：$typeName")
                }
            }
        }
    }
}