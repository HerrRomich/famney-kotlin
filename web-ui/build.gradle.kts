import com.github.gradle.node.npm.task.NpxTask

plugins {
    id("com.github.node-gradle.node") version "3.3.0"
}

node {
  version.set("16.13.1")
  npmVersion.set("8.12.1")
  nodeProjectDir.set(file("."))
}


tasks {
  val buildFrontend = register<NpxTask>("buildWebUI") {
    dependsOn("npmInstall")
    command.set("ng")
    args.set(listOf("build"))
    inputs.files("package.json", "package-lock.json", "angular.json", "tsconfig.json", "tsconfig.app.json")
  }
}
