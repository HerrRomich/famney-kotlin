plugins {
    id("org.springframework.boot") version "3.0.4" apply false
    id("io.spring.dependency-management") version "1.1.0"
    kotlin("jvm") version "1.8.0"
    kotlin("plugin.spring") version "1.8.0" apply false
    kotlin("plugin.jpa") version "1.8.0" apply false
    id("io.swagger.core.v3.swagger-gradle-plugin") version "2.2.8" apply false
}

configure(subprojects.filter { !listOf("web-ui").contains(it.name) }) {
    apply {
        plugin("org.springframework.boot")
        plugin("io.spring.dependency-management")
        plugin("org.jetbrains.kotlin.jvm")
        plugin("org.jetbrains.kotlin.plugin.spring")
        plugin("org.jetbrains.kotlin.plugin.jpa")
    }

    group = "io.github.herrromich.famoney"
    version = "1.0-SNAPSHOT"

    dependencyManagement {
        imports {
            mavenBom(org.springframework.boot.gradle.plugin.SpringBootPlugin.BOM_COORDINATES)
        }
    }

    kotlin {
        jvmToolchain(17)
    }

    tasks.withType<Test> {
        useJUnitPlatform()
    }

    tasks.withType<JavaCompile> {
        options.encoding = "UTF-8"
    }

    tasks.jar {
        duplicatesStrategy = org.gradle.api.file.DuplicatesStrategy.EXCLUDE
    }
}