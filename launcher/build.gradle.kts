dependencies {
    implementation(kotlin("stdlib"))
    implementation(kotlin("stdlib-jdk8"))
    implementation(kotlin("reflect"))
    implementation("org.springframework.boot:spring-boot-starter-actuator")
    implementation("org.springframework.boot:spring-boot-starter-aop")

    implementation(project(":commons:commons-web"))
    implementation(project(":domain"))
    implementation(project(":swagger-ui"))
    implementation(project(":accounts:accounts-api-impl"))
    implementation(project(":master-data:master-data-api-impl"))
}

springBoot {
    mainClass.set("io.github.herrromich.famoney.launcher.ServerLauncherKt")
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
