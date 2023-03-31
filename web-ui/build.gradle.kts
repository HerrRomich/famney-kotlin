import com.github.gradle.node.npm.task.NpxTask
import org.openapitools.generator.gradle.plugin.tasks.GenerateTask

plugins {
  id("com.github.node-gradle.node") version "3.5.1"
  id("org.openapi.generator").version("6.3.0")
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
  apis.values.forEach{api ->
    apiDeps(project(api.project))
  }
}

node {
  version.set("16.13.1")
  npmVersion.set("8.12.1")
  nodeProjectDir.set(file("."))
}

tasks {
  val copyApiDeps = register("copyApiDeps") {
    doFirst {
      copy {
        val jsons = apiDeps.resolvedConfiguration.resolvedArtifacts
          .flatMap { artifact ->
            val projectPath = (artifact.id.componentIdentifier as? ProjectComponentIdentifier)?.projectPath
            val jsonName = apis[projectPath]?.jsonName
            jsonName?.let { name -> zipTree(artifact.file).filter { it.name == name } } ?: emptyList()
          }
        from(*jsons.toTypedArray())
        into("$buildDir/api-defs")
      }
    }
    dependsOn(*apis.values.map { "${it.project}:${it.resolveTask}" }.toTypedArray())
  }

  val deleteApis = register<Delete>("deleteApis") {
    delete("$projectDir/src/app/shared/apis")
  }

  val generateTasks = apis.values
    .map {
      register<GenerateTask>("generate${it.name}AngularClient") {
        inputSpec.set("$buildDir/api-defs/${it.jsonName}")
        outputDir.set("$projectDir/src/app/shared/apis/${it.destPath}")
        generatorName.set("typescript-angular")
        typeMappings.set(mapOf("set" to "Array", "DateTime" to "Date", "date" to "Date", "date-time" to "Date"))
        configOptions.set(
          mapOf(
            "ngVersion" to "15.0.0",
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
        dependsOn(copyApiDeps, deleteApis)
      }
    }

  val generateApiAngularClient = register("generateApiAngularClient") {
    dependsOn(*generateTasks.toTypedArray())
  }

  register<NpxTask>("buildWebUI") {
    dependsOn(generateApiAngularClient, "npmInstall")
    command.set("ng")
    args.set(listOf("build"))
    inputs.files("package.json", "package-lock.json", "angular.json", "tsconfig.json", "tsconfig.app.json")
  }
}
