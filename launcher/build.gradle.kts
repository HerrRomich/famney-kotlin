plugins {
    id("org.springframework.boot") version "2.7.0"
}

dependencies {
    implementation(kotlin("stdlib"))
    implementation(kotlin("stdlib-jdk8"))
    implementation("org.springframework.boot:spring-boot-starter-web")
    implementation("org.springframework.boot:spring-boot-starter-actuator")
    implementation("org.springframework.boot:spring-boot-starter-aop")
}

springBoot {
    mainClass.set("com.hrrm.famoney.launcher.ServerLauncherKt")
}

tasks {
    jar {
        from(project(":web-ui").layout.projectDirectory.dir("dist")) {
            into("static/web-ui")
        }
        dependsOn(":web-ui:buildWebUI")
    }
    bootJar {
        from(project(":web-ui").layout.projectDirectory.dir("dist")) {
            into("BOOT-INF/classes/static/web-ui")
        }
        dependsOn(":web-ui:buildWebUI")
    }
}
