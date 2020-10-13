package com.ysj.lib.route.apt

import com.google.auto.service.AutoService
import com.squareup.kotlinpoet.*
import com.squareup.kotlinpoet.ParameterizedTypeName.Companion.parameterizedBy
import com.ysj.lib.route.annotation.*
import java.io.IOException
import javax.annotation.processing.*
import javax.lang.model.SourceVersion
import javax.lang.model.element.Element
import javax.lang.model.element.TypeElement

/**
 * 用于处理该注解 [Route]
 *
 * @author Ysj
 * Create time: 2020/8/4
 */
@AutoService(Processor::class)
// JDK 编译版本
@SupportedSourceVersion(SourceVersion.RELEASE_8)
// 要处理的注解
@SupportedAnnotationTypes(ANNOTATION_TYPE_ROUTE)
class RouteProcessor : BaseProcess() {

    companion object {
        const val TEMPLATE_PATH = "com.ysj.lib.route.template.IProviderRoute"
    }

    private lateinit var moduleName: String

    override fun init(processingEnv: ProcessingEnvironment) {
        super.init(processingEnv)
        val options: Map<String, String> = processingEnv.options
        moduleName = options["moduleName"].toString()
        if (moduleName == "null") printInitFailure()
        printlnMessage("开始处理 --> $moduleName module")
    }

    override fun process(
        annotations: MutableSet<out TypeElement>, roundEnv: RoundEnvironment
    ): Boolean {
        if (moduleName == "null") return false
        if (annotations.isEmpty()) return false
        // 获取所有被 @Route 注解的 element
        val elements = roundEnv.getElementsAnnotatedWith(Route::class.java)
        if (elements.isEmpty()) return false
        try {
            processRouter(elements)
        } catch (e: Exception) {
            printlnError("@Route 注解处理失败：${e.message}")
        }
        return true
    }

    /**
     * 处理注解 [Route] 目的是将其信息先解析后存起来，并生成对应的文件
     */
    @Throws(Exception::class)
    private fun processRouter(elements: MutableSet<out Element>) {
        // 用于记录用于生成文件所需要的参数，key：group，value：该 group 下的所有路由信息
        val params = HashMap<String, MutableList<RouteBean>>()
        elements.forEach {
            val route: Route = it.getAnnotation(Route::class.java)
            var group: String = route.group
            var path: String = route.path
            checkRouterPath(path)
            if (group.isEmpty()) {
                group = subGroupFromPath(path)
                // 从 path 中去除 group
                path = path.replace("/$group", "")
            }
            var routeBeans = params[group]
            if (routeBeans == null) {
                routeBeans = ArrayList()
                params[group] = routeBeans
            }
            val routeBean = RouteBean().apply {
                this.path = path
                this.group = group
                typeElement = it as TypeElement
            }
            // 检查注解作用的 element 类型
            when {
                typeUtils.isSubtype(it.asType(), elementUtils.getTypeElement(AFFECT_ACTIVITY).asType()) -> {
                    routeBean.types = RouteTypes.ACTIVITY
                }
                typeUtils.isSubtype(it.asType(), elementUtils.getTypeElement(AFFECT_ACTION).asType()) -> {
                    routeBean.types = RouteTypes.ACTION
                }
                else -> {
                    printlnError("""
                        @Router 注解目前只能用于:
                        -  $AFFECT_ACTIVITY
                        -  $AFFECT_ACTION
                    """.trimMargin())
                }
            }
            routeBeans.add(routeBean)
            printlnMessage("@Route --- 已处理：group:$group , path:$path")
        }
        createRouteFile(params)
    }

    @Throws(IOException::class)
    private fun createRouteFile(params: Map<String, MutableList<RouteBean>>) {
        if (params.isEmpty()) return
        params.forEach { entry ->
            // 生成类似如下方法
            // override fun loadInto(atlas: MutableMap<String, RouteBean>) {
            //        atlas["/app/MainActivity"] = RouteBean().apply {
            //            group = "app"
            //            path = "/MainActivity"
            //            types = RouteTypes.ACTIVITY
            //            className = "com.ysj.lib.route.module.app.MainActivity"
            //            moduleId = "com.ysj.lib.router"
            //        }
            // }
            val loadInto =
                    FunSpec.builder("loadInto").addModifiers(KModifier.PUBLIC, KModifier.OVERRIDE)
                        .addParameter(
                            "atlas", MUTABLE_MAP.parameterizedBy(
                                STRING, ClassName.bestGuess(RouteBean::class.java.name)
                            )
                        )
            entry.value.forEach { routeBean ->
                loadInto.addStatement(
                    """
                    atlas["/${routeBean.group}${routeBean.path}"] = RouteBean().apply {
                        group = "${routeBean.group}"
                        path = "${routeBean.path}"
                        types = %T.%L
                        className = "${(routeBean.typeElement as TypeElement).qualifiedName}"
                        moduleId = "$moduleName"
                    }
                """.trimIndent(),
                    ClassName.bestGuess(RouteTypes::class.java.name),
                    routeBean.types!!.name
                )
            }
            val typeSpec = TypeSpec.classBuilder(PREFIX_ROUTE + entry.key)
                .addModifiers(KModifier.PUBLIC)
                .addSuperinterface(ClassName.bestGuess(TEMPLATE_PATH))
                .addFunction(loadInto.build()).build()
            FileSpec.builder(PACKAGE_NAME_ROUTE, typeSpec.name!!).addType(typeSpec).build()
                .writeTo(filer)
        }
    }

    private fun printInitFailure() {
        printlnError(
            """
                请在模块的 build.gradle 的 defaultConfig 中添加：
                javaCompileOptions {
                    kapt {
                        arguments {
                        // 告诉注解处理器该 module 的 applicationId
                        arg("moduleName", applicationId)
                        }
                    }
                }
            """.trimIndent()
        )
    }
}
