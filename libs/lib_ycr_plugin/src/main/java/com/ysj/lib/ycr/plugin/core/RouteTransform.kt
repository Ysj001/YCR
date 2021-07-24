package com.ysj.lib.ycr.plugin.core

import com.android.Version
import com.android.build.api.transform.*
import com.android.build.gradle.AppExtension
import com.android.build.gradle.internal.pipeline.TransformManager
import com.android.utils.FileUtils
import com.ysj.lib.ycr.plugin.core.logger.YLogger
import com.ysj.lib.ycr.plugin.core.visitor.AllClassVisitor
import com.ysj.lib.ycr.plugin.core.visitor.PreVisitor
import com.ysj.lib.ycr.plugin.core.visitor.entity.ClassInfo
import org.gradle.api.Project
import org.objectweb.asm.ClassReader
import org.objectweb.asm.ClassWriter
import java.io.File
import java.io.FileOutputStream
import java.io.InputStream
import java.util.*
import java.util.jar.JarEntry
import java.util.jar.JarFile
import java.util.jar.JarOutputStream
import java.util.zip.ZipEntry
import kotlin.collections.HashSet

/**
 * 路由的 Transform
 *
 * @author Ysj
 * Create time: 2020/8/16
 */
class RouteTransform(private val project: Project) : Transform() {

    companion object {
        const val PLUGIN_NAME = "YCRPlugin"
    }

    /** 主 module 的 [AppExtension] */
    lateinit var mainModuleAppExt: AppExtension

    /** 本 module 的 [AppExtension] */
    lateinit var moduleAppExt: AppExtension

    /** 本 module 的 [RouteExtensions] */
    lateinit var moduleRouteExt: RouteExtensions

    private val logger = YLogger.getLogger(javaClass)

    /** 不需要的 [JarEntry] 用于提升处理速度 */
    private val notNeedJarEntriesCache by lazy(LazyThreadSafetyMode.NONE) { HashSet<String>() }

    /** 缓存的 [ClassInfo] */
    val cacheClassInfo = HashSet<ClassInfo>()

    /** 用于监测 IExecutorProvider 的实现是否存在多个 */
    var executorProviderClassName: String? = null
        set(value) {
            if (field != null && field != value) {
                throw RuntimeException(
                    """
                    存在多个 IExecutorProvider 请检查：
                    $value
                    或
                    $executorProviderClassName
                """.trimIndent()
                )
            }
            field = value
        }

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
        if (src.name in notNeedJarEntriesCache) {
            FileUtils.copyFile(src, dest)
            return
        }
        JarFile(src).use { jf ->
            JarOutputStream(dest.outputStream()).use { jos ->
                jf.entries().forEach {
                    jf.getInputStream(this).use { ips ->
                        val zipEntry = ZipEntry(name)
                        if (notNeedJarEntries()) {
                            jos.putNextEntry(zipEntry)
                            jos.write(ips.readBytes())
                        } else {
//                            logger.quiet("process jar element --> $name")
                            val cr = ClassReader(ips)
                            val cw = ClassWriter(cr, 0)
                            val cv = AllClassVisitor(this@RouteTransform, cw)
                            cr.accept(cv, 0)
                            jos.putNextEntry(zipEntry)
                            jos.write(cw.toByteArray())
                        }
                        jos.closeEntry()
                    }
                }
            }
        }
    }

    private fun processDir(input: DirectoryInput, output: TransformOutputProvider) {
        val src = input.file
        val dest = output.getContentLocation(
            input.name,
            input.contentTypes,
            input.scopes,
            Format.DIRECTORY
        )
        FileUtils.copyDirectory(src, dest)
        dest.walk().forEach {
            if (!it.isNeedFile()) return@forEach
//            logger.quiet("process file --> ${it.name}")
            it.inputStream().use { fis ->
                val cr = ClassReader(fis)
                val cw = ClassWriter(cr, 0)
                val cv = AllClassVisitor(this, cw)
                cr.accept(cv, ClassReader.EXPAND_FRAMES)
                FileOutputStream(it).use { fos ->
                    fos.write(cw.toByteArray())
                }
            }
        }
    }

    private fun prePrecess(jis: Collection<JarInput>, dis: Collection<DirectoryInput>) {
        jis.forEach { input ->
            JarFile(input.file).use { jf ->
                /*
                    由于该 transform 可能后于其他 transform
                    此时该 transform 的输入源会变为其他 transform 的输出
                    此时输入源的名称会发生变化，因此不能简单通过 input.file.name 过滤
                 */
                val entries = jf.entries()
                entries.forEach entry@{
                    if (notNeedJarEntries()) {
                        if (!entries.hasMoreElements()) notNeedJarEntriesCache.add(input.file.name)
                        return@entry
                    }
                    logger.verbose("need process in jar --> ${name}")
                    jf.getInputStream(this).preVisitor()
                }
            }
        }
        dis.forEach { input ->
            input.file.walk().forEach test@{
                if (!it.isNeedFile()) return@test
                logger.verbose("need process in dir --> ${it.name}")
                it.inputStream().preVisitor()
            }
        }
    }

    private fun InputStream.preVisitor() = use {
        val cr = ClassReader(it)
        val cw = ClassWriter(cr, 0)
        val cv = PreVisitor(this@RouteTransform, cw)
        cr.accept(cv, 0)
    }

    private fun JarEntry.notNeedJarEntries(): Boolean =
        name.endsWith(".class").not()
                || name.startsWith("kotlin/")
                || name.startsWith("kotlinx/")
                || name.startsWith("javax/")
                || name.startsWith("org/intellij/")
                || name.startsWith("org/jetbrains/")
                || name.startsWith("org/junit/")
                || name.startsWith("org/hamcrest/")
                || name.startsWith("com/squareup/")
                || name.startsWith("androidx/")
//                || name.startsWith("android/")
                || name.startsWith("com/google/android/")

    private fun File.isNeedFile(): Boolean = isFile && extension == "class"

    private inline fun <T> Enumeration<T>.forEach(block: T.() -> Unit) {
        while (hasMoreElements()) nextElement().block()
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
        YLogger.LOGGER_LEVEL = moduleRouteExt.loggerLevel
        logger.quiet("=================== $PLUGIN_NAME transform start ===================")
        logger.quiet(">>> gradle version: ${project.gradle.gradleVersion}")
        logger.quiet(">>> gradle plugin version: ${Version.ANDROID_GRADLE_PLUGIN_VERSION}")
        logger.quiet(">>> isIncremental: ${transformInvocation.isIncremental}")
        logger.quiet(">>> loggerLevel: ${YLogger.LOGGER_LEVEL}")
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