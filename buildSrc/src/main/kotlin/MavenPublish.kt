
import com.android.build.gradle.LibraryExtension
import org.gradle.api.JavaVersion
import org.gradle.api.Project
import org.gradle.api.internal.artifacts.dsl.LazyPublishArtifact
import org.gradle.api.plugins.JavaPluginExtension
import org.gradle.api.publish.PublishingExtension
import org.gradle.api.publish.maven.MavenPublication
import org.gradle.api.tasks.bundling.Jar
import org.gradle.api.tasks.javadoc.Javadoc
import org.gradle.external.javadoc.StandardJavadocDocletOptions
import org.gradle.kotlin.dsl.*
import java.net.URI

/*
 * Maven 发布相关扩展
 *
 * @author Ysj
 * Create time: 2021/6/29
 */

/**
 * 便捷的发布到 maven 仓库
 *
 * 需要在该 module 根目录的的 gradle.properties 中定义：
 * - POM_DESCRIPTION
 * - POM_ARTIFACT_ID
 * - POM_PACKAGING
 */
fun Project.mavenPublish() {
    // 添加发布需要的 plugin
    apply(plugin = "maven-publish")
    apply(plugin = "signing")
    // 获取 module 中定义的发布信息
    val pomDesc = properties["POM_DESCRIPTION"] as String
    val pomAftId = properties["POM_ARTIFACT_ID"] as String
    val pomPkgType = properties["POM_PACKAGING"] as String
    mavenPublish(MAVEN_LOCAL, pomDesc, pomAftId, pomPkgType)
}

/**
 * 便捷的发布到 maven 仓库
 *
 * - [gradle-developers](https://docs.gradle.org/current/userguide/publishing_maven.html)
 * - [android-developers](https://developer.android.google.cn/studio/build/maven-publish-plugin#groovy)
 *
 * @param desc The description for the publication represented by this POM.
 * @param aftId Sets the artifactId for this publication.
 * @param packaging Sets the packaging for the publication represented by this POM.
 */
fun Project.mavenPublish(
    reposPath: URI,
    desc: String,
    aftId: String,
    packaging: String
) = afterEvaluate {
    // 判断 module 类型
    val isAndroidApp = project.plugins.hasPlugin("com.android.application")
    val isAndroidLib = project.plugins.hasPlugin("com.android.library")
    val isAndroidProject = isAndroidApp || isAndroidLib
    // 添加打包源码的任务，这样方便查看 lib 的源码
    @Suppress("UnstableApiUsage")
    if (!isAndroidProject) extensions.getByType(JavaPluginExtension::class.java).apply {
        withJavadocJar()
        withSourcesJar()
    }
    else tasks.register<Jar>("androidSourcesJar") {
        archiveClassifier.set("sources")
        from(project.extensions.getByType<LibraryExtension>().sourceSets["main"].java.srcDirs)
    }
    // 配置 maven 发布任务
    extensions.configure<PublishingExtension>("publishing") {
        publications {
            create<MavenPublication>("mavenJava") {
                groupId = LIB_GROUP_ID
                artifactId = aftId
                version = LIB_VERSION
                when (packaging) {
                    "aar" -> from(components["release"])
                    "jar" -> from(components["java"])
                }
                // android 的源码打包
                if (isAndroidProject) artifact(LazyPublishArtifact(tasks.named("androidSourcesJar")))
                pom {
                    name.set(aftId)
                    description.set(desc)
                    this.packaging = packaging
                    url.set(POM_URL)
                    licenses {
                        license {
                            name.set(POM_LICENCE_NAME)
                            url.set(POM_LICENCE_URL)
                        }
                    }
                    developers {
                        developer {
                            id.set(POM_DEVELOPER_ID)
                            name.set(POM_DEVELOPER_NAME)
                            email.set(POM_DEVELOPER_EMAIL)
                        }
                    }
                }
            }
        }
        repositories.maven {
            url = reposPath
        }
    }
    if (JavaVersion.current().isJava9Compatible) tasks.named<Javadoc>("javadoc") {
        (options as StandardJavadocDocletOptions).addBooleanOption("html5", true)
    }
}