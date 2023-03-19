dependencies {
    api("io.swagger.core.v3:swagger-annotations-jakarta:2.2.8")
    api("io.swagger.core.v3:swagger-core-jakarta:2.2.8")
    api("io.swagger.parser.v3:swagger-parser-v3:2.1.12")

    implementation(project(":commons:commons-core"))
    api(project(":commons:commons-web"))
    api ("org.springframework.boot:spring-boot-starter-jersey") {
        exclude("org.springframework.boot", "spring-boot-starter-tomcat")
    }
}