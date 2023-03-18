dependencies {
    implementation(kotlin("stdlib"))
    implementation(kotlin("stdlib-jdk8"))
    implementation(kotlin("reflect"))
    implementation("org.springframework.boot:spring-boot-starter-web") {
        exclude("org.springframework.boot:spring-boot-starter-tomcat")
    }
    implementation("org.springframework.boot:spring-boot-starter-jetty")
    implementation("org.springframework.boot:spring-boot-starter-actuator")
    implementation("org.springframework.boot:spring-boot-starter-aop")

    implementation(project(":domain"))
    implementation(project(":swagger-ui"))
    implementation(project(":accounts:accounts-api-impl"))
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
