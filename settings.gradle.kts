rootProject.name = "FaMoney"

dependencyResolutionManagement {
    repositoriesMode.set(RepositoriesMode.PREFER_SETTINGS)
    repositories {
        mavenCentral()
    }
}
pluginManagement {
    repositories {
        mavenCentral()
        gradlePluginPortal()
    }
}

plugins {
    id("org.gradle.toolchains.foojay-resolver-convention") version ("0.4.0")
}

include("web-ui")
include("launcher")
include("commons:commons-persistence")
findProject(":commons:commons-persistence")?.name = "commons-persistence"
include("commons:commons-core")
findProject(":commons:commons-core")?.name = "commons-core"
include("accounts:accounts-api")
findProject(":accounts:accounts-api")?.name = "accounts-api"
include("commons:commons-jaxrs")
findProject(":commons:commons-jaxrs")?.name = "commons-jaxrs"
include("commons:commons-immutables")
findProject(":commons:commons-immutables")?.name = "commons-immutables"
include("accounts:accounts-api-impl")
findProject(":accounts:accounts-api-impl")?.name = "accounts-api-impl"
include("commons:commons-events")
findProject(":commons:commons-events")?.name = "commons-events"
include("domain")
include("domain:domain-migration")
findProject(":domain:domain-migration")?.name = "domain-migration"
include("swagger-ui")
