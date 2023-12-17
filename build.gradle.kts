import org.springframework.boot.gradle.plugin.ResolveMainClassName
import org.springframework.boot.gradle.tasks.bundling.BootJar

plugins {
    id("org.springframework.boot") version "3.1.6" apply false
    id("io.spring.dependency-management") version "1.1.4"
    kotlin("jvm") version "1.9.21"
    kotlin("plugin.spring") version "1.9.21" apply false
    kotlin("plugin.jpa") version "1.9.21" apply false
    id("io.swagger.core.v3.swagger-gradle-plugin") version "2.2.8" apply false
}

configure(subprojects) {
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
        jvmToolchain(21)
    }

    tasks.withType<Test> {
        useJUnitPlatform()
    }

    tasks.withType<JavaCompile> {
        options.encoding = "UTF-8"
    }

    tasks.withType<ResolveMainClassName> {
        enabled = false
    }

    tasks.withType<BootJar> {
        enabled = false
    }

    tasks.jar {
        duplicatesStrategy = DuplicatesStrategy.EXCLUDE
    }
}
