import io.swagger.v3.plugins.gradle.tasks.ResolveTask
import io.swagger.v3.plugins.gradle.tasks.ResolveTask.Format.JSON

plugins {
    kotlin("kapt")
}

val resolveConfig by configurations.creating

dependencies {
    api(project(":commons:commons-jaxrs"))
    implementation(project(":commons:commons-immutables"))

    resolveConfig("io.swagger.core.v3:swagger-jaxrs2:2.2.1")

    annotationProcessor("org.immutables:value:2.9.2")
    kapt("org.immutables:value:2.9.2")
}

val resolveApi = tasks.register<ResolveTask>("resolveApi") {
    outputDir = sourceSets.main.get().output.resourcesDir
    outputFormat = JSON
    outputFileName = "accounts-api"
    prettyPrint = true
    encoding = "UTF-8"
    resourcePackages = setOf("com.hrrm.famoney.accounts.api")
    classpath = sourceSets.main.get().runtimeClasspath
    buildClasspath = resolveConfig
    dependsOn(tasks.compileJava)
}

tasks.jar {
    dependsOn(resolveApi)
}