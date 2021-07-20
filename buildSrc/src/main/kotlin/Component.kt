
import org.gradle.api.Project
import org.gradle.internal.impldep.org.codehaus.plexus.util.xml.pull.XmlPullParser
import org.gradle.kotlin.dsl.project
import java.io.File
import java.io.InputStream
import java.net.URI
import java.util.concurrent.ConcurrentHashMap
import java.util.concurrent.ConcurrentSkipListSet

/*
 * 定义组件相关扩展
 *
 * @author Ysj
 * Create time: 2021/7/17
 */

/** 组件本地的 maven */
val Project.MAVEN_COMPONENT_LOCAL: URI
    get() = File(rootDir, "repos_component").toURI()

/** 组件远端的 maven-snapshots */
const val MAVEN_COMPONENTS = "http://localhost:8081/repository/maven-snapshots/"

/** 组件发布发 group id */
const val COMPONENT_GROUP_ID = "com.ysj.ycr.component"

/** 组件的版本 */
const val COMPONENT_VERSION = "1.0.0-SNAPSHOT"

private const val MAVEN_METADATA = "maven-metadata.xml"

/** 组件被哪些 module 依赖了。key：组件，value：依赖该组件的 module */
val componentDependency = ConcurrentHashMap<String, MutableSet<String>>()

/**
 * 导入某个组件，优先使用 maven 中的，如果 maven 没有则使用源码
 *
 * @param name 要导入的组件的 project 的 name
 * @param configName 导入方式
 */
fun Project.import(
    name: String,
    configName: String = "implementation"
) = dependencies.run {
    var modules = componentDependency[name]
    if (modules == null) {
        modules = ConcurrentSkipListSet()
        componentDependency[name] = modules
    }
    modules.add(this@import.name)
    val old = System.currentTimeMillis()
    val libPath = "$COMPONENT_GROUP_ID${File.separator}$name".replace(".", File.separator)
    val remoteLibVersion = remoteLibMetadata("$MAVEN_COMPONENTS$libPath")?.use { it.parseVersion() }
    deleteIdeaCacheLib(name)
    val localLibDir = File(file(MAVEN_COMPONENT_LOCAL), libPath)
    val localLibVersion = localLibDir.localLibMetadata()?.use { it.parseVersion() }
    println("Component import process time: ${System.currentTimeMillis() - old}ms")
    if (remoteLibVersion == null && localLibVersion == null) add(configName, project(":$name"))
    else {
        if (remoteLibVersion != null && localLibVersion != null && remoteLibVersion > localLibVersion) {
            delete(localLibDir)
        }
        add(configName, "$COMPONENT_GROUP_ID:$name:$COMPONENT_VERSION")
    }
}

/**
 * 解析 [MAVEN_METADATA] 中的 lastUpdated
 */
private fun InputStream?.parseVersion() = if (this == null) null else xml.newPullParser().run {
    setInput(this@parseVersion, "UTF-8")
    var type = eventType
    while (type != XmlPullParser.END_DOCUMENT) {
        if (type != XmlPullParser.START_TAG || name != "lastUpdated") {
            type = next()
            continue
        }
        return@run nextText().toLong()
    }
    null
}

private fun File.localLibMetadata() =
    if (!exists()) null
    else walkTopDown().maxDepth(1)
        .find { it.name == MAVEN_METADATA }
        ?.inputStream()

private fun remoteLibMetadata(path: String) = catching {
    requestGet("$path/$MAVEN_METADATA").execute().run {
        if (isSuccessful) body?.byteStream() else null
    }
}

private fun Project.deleteIdeaCacheLib(lib: String) {
    val libFilePath = "$COMPONENT_GROUP_ID${File.separator}$lib"
    val libMetadataPath = "descriptors${File.separator}$COMPONENT_GROUP_ID${File.separator}$lib"
    File(gradle.gradleUserHomeDir, "caches${File.separator}modules-2")
        .walkTopDown().maxDepth(1).forEach { cacheRoot ->
            if (!cacheRoot.isDirectory) return@forEach
            val name = cacheRoot.name
            if (name.startsWith("files")) delete(File(cacheRoot, libFilePath))
            else if (name.startsWith("metadata")) delete(File(cacheRoot, libMetadataPath))
        }
}

private inline fun <R> catching(block: () -> R): R? {
    try {
        return block()
    } catch (e: Exception) {
        println("Component import warning: ${e.message}")
    }
    return null
}