import com.github.gradle.node.npm.task.NpxTask
import org.openapitools.generator.gradle.plugin.tasks.GenerateTask

plugins {
  id("com.github.node-gradle.node").version("7.0.1")
  id("org.openapi.generator").version("7.1.0")
}

data class Api(
  val name: String,
  val project: String,
  val jsonName: String,
  val resolveTask: String = "resolveApi",
  val destPath: String
)

val apis = mapOf(
  ":accounts:accounts-api" to Api(
    name = "AccountsApi",
    project = ":accounts:accounts-api",
    jsonName = "accounts-api.json",
    destPath = "accounts"
  ),
  ":master-data:master-data-api" to Api(
    name = "MasterDataApi",
    project = ":master-data:master-data-api",
    jsonName = "master-data-api.json",
    destPath = "master-data"
  ),
)

val apiDeps: Configuration by configurations.creating

dependencies {
  apis.values.forEach { api ->
    apiDeps(project(api.project))
  }
}

node {
  download = true
  distBaseUrl = "https://nodejs.org/dist"
  version ="18.19.0"
  npmVersion ="10.2.3"
  workDir = file("${project.projectDir}/.gradle/nodejs")
  npmWorkDir = file("${project.projectDir}/.gradle/npm")
  nodeProjectDir = file(".")
}

tasks {
  val generateTasks = apis.values
    .map {
      val copyApiDepsTask = register("copy${it.name}Deps") {
        doFirst {
          copy {
            from(project("${it.project}").sourceSets.main.get().output.resourcesDir?.resolve(it.jsonName))
            into(layout.buildDirectory.file("api-defs"))
          }
        }
        inputs.files(project(it.project).tasks.get(it.resolveTask))
        outputs.file(layout.buildDirectory.file("api-defs/${it.jsonName}"))
        dependsOn(project(it.project).tasks.get(it.resolveTask))
      }
      register<GenerateTask>("generate${it.name}AngularClient") {
        inputs.file(layout.buildDirectory.file("api-defs/${it.jsonName}"))
        doFirst {
          delete("${layout.projectDirectory}/src/app/shared/apis/${it.destPath}/*")
        }
        inputSpec.set(layout.buildDirectory.file("api-defs/${it.jsonName}").get().toString())
        outputDir.set("${layout.projectDirectory}/src/app/shared/apis/${it.destPath}")
        generatorName.set("typescript-angular")
        typeMappings.set(mapOf("set" to "Array", "DateTime" to "Date", "date" to "Date", "date-time" to "Date"))
        configOptions.set(
          mapOf(
            "ngVersion" to "17.0.0",
            "serviceSuffix" to "ApiService",
            "serviceFileSuffix" to "-api.service",
            "modelFileSuffix" to ".dto",
            "modelSuffix" to "Dto",
            "fileNaming" to "kebab-case",
            "taggedUnions" to "true",
            "stringEnums" to "false",
            "legacyDiscriminatorBehavior" to "true",
            "providedIn" to "any",
          )
        )
        dependsOn(copyApiDepsTask)
      }
    }
  register<GenerateTask>("generateTest") {
    doFirst {
      delete("${layout.projectDirectory}/src/app/shared/apis/admin-center-api/*")
    }
    inputSpec.set("${layout.buildDirectory}/api-defs/admin-center-api.yaml")
    outputDir.set("${layout.projectDirectory}/src/app/shared/apis/admin-center-api")
    generatorName.set("typescript-angular")
    typeMappings.set(mapOf("set" to "Array", "DateTime" to "Date", "date" to "Date", "date-time" to "Date"))
    configOptions.set(
      mapOf(
        "ngVersion" to "17.0.0",
        "serviceSuffix" to "ApiService",
        "serviceFileSuffix" to "-api.service",
        "modelFileSuffix" to ".dto",
        "modelSuffix" to "Dto",
        "fileNaming" to "kebab-case",
        "providedIn" to "any",
      )
    )
  }

  val generateApiAngularClient = register("generateApiAngularClient") {
    dependsOn(*generateTasks.toTypedArray())
  }

  val buildWebUi = register<NpxTask>("buildWebUI") {
    dependsOn("npmInstall", generateApiAngularClient)
    command.set("ng")
    args.set(listOf("build"))
    inputs.files("package.json", "package-lock.json", "angular.json", "tsconfig.json", "tsconfig.app.json")
  }

  processResources {
    dependsOn(buildWebUi)
  }
}
