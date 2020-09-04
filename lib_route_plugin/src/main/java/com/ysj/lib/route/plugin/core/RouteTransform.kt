package com.ysj.lib.route.plugin.core

import com.android.Version
import com.android.build.api.transform.*
import com.android.build.gradle.internal.LoggerWrapper
import com.android.build.gradle.internal.pipeline.TransformManager
import com.android.utils.FileUtils
import com.ysj.lib.route.plugin.core.visitor.AllClassVisitor
import com.ysj.lib.route.plugin.core.visitor.PreVisitor
import org.gradle.api.Project
import org.objectweb.asm.ClassReader
import org.objectweb.asm.ClassWriter
import java.io.FileOutputStream
import java.io.InputStream
import java.util.jar.JarFile
import java.util.jar.JarOutputStream
import java.util.zip.ZipEntry

/**
 * 路由的 Transform
 *
 * @author Ysj
 * Create time: 2020/8/16
 */
class RouteTransform(private val project: Project) : Transform() {

    companion object {
        const val PLUGIN_NAME = "RoutePlugin"
    }

    private val logger = LoggerWrapper.getLogger(javaClass)

    override fun getName() = PLUGIN_NAME

    /**
     * 作用类型
     */
    override fun getInputTypes(): MutableSet<QualifiedContent.ContentType> =
        TransformManager.CONTENT_CLASS

    /**
     * 是否支持增量编译
     */
    override fun isIncremental() = false

    /**
     * 作用域
     */
    override fun getScopes(): MutableSet<in QualifiedContent.Scope> =
        TransformManager.SCOPE_FULL_PROJECT

    override fun transform(transformInvocation: TransformInvocation) {
        super.transform(transformInvocation)
        doTransform(transformInvocation) { context,
                                           inputs,
                                           referencedInputs,
                                           outputProvider,
                                           isIncremental ->
            if (!isIncremental) outputProvider.deleteAll()
            // 预处理，用于提前获取信息
            inputs.forEach { prePrecess(it.jarInputs, it.directoryInputs) }
            inputs.forEach {
                // 处理 jar
                it.jarInputs.forEach { input -> processJar(input, outputProvider) }
                // 处理源码
                it.directoryInputs.forEach { input -> processDir(input, outputProvider) }
            }
        }
    }

    private fun processJar(input: JarInput, output: TransformOutputProvider) {
        val src = input.file
        val dest = output.getContentLocation(
            input.name,
            input.contentTypes,
            input.scopes,
            Format.JAR
        )
        if (!src.name.startsWith("classes")) {
            FileUtils.copyFile(src, dest)
            return
        }
        val jarFile = JarFile(src)
        val jos = JarOutputStream(dest.outputStream())
        jarFile.entries().toList()
            .filter { true }
            .forEach {
                val inputStream = jarFile.getInputStream(it)
                val zipEntry = ZipEntry(it.name)
                if (it.name.endsWith(".class")) {
                    //                    logger.quiet("process jar element --> ${element.name}")
                    val cr = ClassReader(inputStream)
                    val cw = ClassWriter(cr, ClassWriter.COMPUTE_MAXS)
                    val cv = AllClassVisitor(cw)
                    cr.accept(cv, ClassReader.EXPAND_FRAMES)
                    jos.putNextEntry(zipEntry)
                    jos.write(cw.toByteArray())
                } else {
                    jos.putNextEntry(zipEntry)
                    jos.write(inputStream.readBytes())
                }
                jos.closeEntry()
                inputStream.close()
            }
        jos.close()
        jarFile.close()
    }

    private fun processDir(input: DirectoryInput, output: TransformOutputProvider) {
        val src = input.file
        val dest = output.getContentLocation(
            input.name,
            input.contentTypes,
            input.scopes,
            Format.DIRECTORY
        )
        src.walk()
            .filter { it.isFile }
            .filter { it.extension in listOf("class") }
            .filter { it.name !in listOf("BuildConfig.class") }
            .forEach {
                logger.quiet("process file --> ${it.name}")
                val inputStream = it.inputStream()
                val cr = ClassReader(inputStream)
                val cw = ClassWriter(cr, 0)
                val cv = AllClassVisitor(cw)
                cr.accept(cv, ClassReader.EXPAND_FRAMES)
                val fos = FileOutputStream(it)
                fos.write(cw.toByteArray())
                fos.close()
                inputStream.close()
            }
        FileUtils.copyDirectory(src, dest)
    }

    private fun prePrecess(jis: Collection<JarInput>, dis: Collection<DirectoryInput>) {
        jis.filter { it.file.name.startsWith("classes") }
            .forEach { input ->
                val jarFile = JarFile(input.file)
                jarFile.entries().toList()
                    .filter { it.name.endsWith(".class") }
                    .forEach { preVisitor(jarFile.getInputStream(it)) }
            }
        dis.forEach { input ->
            input.file.walk()
                .filter { it.isFile }
                .filter { it.extension in listOf("class") }
                .filter { it.name !in listOf("BuildConfig.class") }
                .forEach { preVisitor(it.inputStream()) }
        }
    }

    private fun preVisitor(inputStream: InputStream) {
        val cr = ClassReader(inputStream)
        val cw = ClassWriter(cr, 0)
        val cv = PreVisitor(cw)
        cr.accept(cv, ClassReader.EXPAND_FRAMES)
        inputStream.close()
    }

    private inline fun doTransform(
        transformInvocation: TransformInvocation,
        block: (
            context: Context,
            inputs: Collection<TransformInput>,
            referencedInputs: Collection<TransformInput>,
            outputProvider: TransformOutputProvider,
            isIncremental: Boolean
        ) -> Unit
    ) {
        logger.quiet("=================== $PLUGIN_NAME transform start ===================")
        logger.quiet(">>> gradle version: ${project.gradle.gradleVersion}")
        logger.quiet(">>> gradle plugin version: ${Version.ANDROID_GRADLE_PLUGIN_VERSION}")
        logger.quiet(">>> isIncremental: ${transformInvocation.isIncremental}")
        val startTime = System.currentTimeMillis()
        block(
            transformInvocation.context,
            transformInvocation.inputs,
            transformInvocation.referencedInputs,
            transformInvocation.outputProvider,
            transformInvocation.isIncremental
        )
        logger.quiet(">>> process time: ${System.currentTimeMillis() - startTime} ms")
        logger.quiet("=================== $PLUGIN_NAME transform end   ===================")
    }
}