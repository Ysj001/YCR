import org.gradle.api.Project
import java.io.File
import java.net.URI

/*
 * Maven 发布用的常量
 *
 * @author Ysj
 * Create time: 2021/6/28
 */

const val MAVEN_CENTRAL_RELEASE = "https://s01.oss.sonatype.org/content/repositories/releases/"

const val MAVEN_CENTRAL_SNAPSHOTS = "https://s01.oss.sonatype.org/content/repositories/snapshots/"

val Project.MAVEN_LOCAL: URI
    get() = File(rootDir, "repos").toURI()

const val LIB_VERSION = "1.0.1-SNAPSHOT"
const val LIB_GROUP_ID = "io.github.ysj001"

const val POM_URL = "https://github.com/Ysj001/YCR"

const val POM_LICENCE_NAME = "Apache-2.0 License"
const val POM_LICENCE_URL = "http://www.apache.org/licenses/LICENSE-2.0.txt"

const val POM_DEVELOPER_ID = "github_Ysj001"
const val POM_DEVELOPER_NAME = "Ysj"
const val POM_DEVELOPER_EMAIL = "543501451@qq.com"

