import io.swagger.v3.plugins.gradle.tasks.ResolveTask
import io.swagger.v3.plugins.gradle.tasks.ResolveTask.Format.JSON

val resolveConfig by configurations.creating

dependencies {
    api(project(":commons:commons-jaxrs"))

    resolveConfig("io.swagger.core.v3:swagger-jaxrs2-jakarta:2.2.8")
}

val resolveApi = tasks.register<ResolveTask>("resolveApi") {
    outputDir = sourceSets.main.get().output.resourcesDir
    outputFormat = JSON
    outputFileName = "accounts-api"
    prettyPrint = true
    encoding = "UTF-8"
    resourcePackages = setOf("io.github.herrromich.famoney.accounts.api")
    classpath = sourceSets.main.get().runtimeClasspath
    buildClasspath = resolveConfig
    dependsOn(tasks.compileJava)
}

tasks.jar {
    dependsOn(resolveApi)
}
