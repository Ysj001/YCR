package com.ysj.lib.ycr.apt

import javax.annotation.processing.AbstractProcessor
import javax.annotation.processing.Filer
import javax.annotation.processing.Messager
import javax.annotation.processing.ProcessingEnvironment
import javax.lang.model.util.Elements
import javax.lang.model.util.Types
import javax.tools.Diagnostic

/**
 * 注解处理器基类
 *
 * @author Ysj
 * Create time: 2020/8/4
 */
abstract class BaseProcess : AbstractProcessor() {

    // 操作 Element 工具类 (类、函数、属性都是 Element)
    lateinit var elementUtils: Elements

    // type(类信息)工具类，包含用于操作 TypeMirror 的工具方法
    lateinit var typeUtils: Types

    // 文件生成器 类/资源，Filter用来创建新的类文件，class文件以及辅助文件
    lateinit var filer: Filer

    // Messager 用来报告错误，警告和其他提示信息
    private lateinit var messager: Messager

    @Synchronized
    override fun init(processingEnv: ProcessingEnvironment) {
        super.init(processingEnv)
        elementUtils = processingEnv.elementUtils
        typeUtils = processingEnv.typeUtils
        messager = processingEnv.messager
        filer = processingEnv.filer
    }

    protected fun printlnMessage(msg: String) {
        messager.printMessage(Diagnostic.Kind.NOTE, "${javaClass.simpleName}: $msg \r ")
    }

    protected fun printlnError(msg: String) {
        messager.printMessage(Diagnostic.Kind.ERROR, "${javaClass.simpleName}: $msg \r ")
    }
}