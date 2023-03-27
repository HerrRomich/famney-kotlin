dependencies {
    implementation(kotlin("stdlib"))
    implementation(kotlin("stdlib-jdk8"))
    implementation(kotlin("reflect"))
    implementation("org.springframework.boot:spring-boot-starter-actuator")
    implementation("org.springframework.boot:spring-boot-starter-aop")

    implementation(project(":commons:commons-jaxrs"))
    implementation(project(":commons:commons-web"))
    implementation(project(":swagger-ui"))
    implementation(project(":accounts:accounts-api-impl"))
    implementation(project(":master-data:master-data-api-impl"))
    implementation(project(":domain:domain-migration"))
}

springBoot {
    mainClass.set("io.github.herrromich.famoney.launcher.ServerLauncherKt")
}

tasks {
    bootJar {
        from(project(":web-ui").layout.projectDirectory.dir("dist")) {
            into("BOOT-INF/classes/static/web-ui")
        }
        dependsOn(":web-ui:buildWebUI")
    }
}
