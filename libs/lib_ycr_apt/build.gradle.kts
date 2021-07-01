plugins {
    id("java-library")
    id("kotlin")
    id("kotlin-kapt")
}

dependencies {
    implementation("org.jetbrains.kotlin:kotlin-stdlib-jdk8:$KOTLIN_VERSION")
    // 注解处理器
    compileOnly("com.google.auto.service:auto-service:1.0-rc7")
    kapt("com.google.auto.service:auto-service:1.0-rc7")
    // 用 kotlinpoet 可以通过类调用的形式来生成代码
    implementation("com.squareup:kotlinpoet:1.7.2")
    implementation("$LIB_GROUP_ID:ycr-annotation:$LIB_VERSION")
}

java {
    sourceCompatibility = JavaVersion.VERSION_1_8
    targetCompatibility = JavaVersion.VERSION_1_8
}

mavenPublish()