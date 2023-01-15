dependencies {
    api ("org.springframework.boot:spring-boot")

    api("jakarta.ws.rs:jakarta.ws.rs-api")

    api("io.swagger.core.v3:swagger-annotations:2.2.1")
    api("io.swagger.core.v3:swagger-core:2.2.1")

    implementation(project(":commons:commons-immutables"))

    annotationProcessor("org.immutables:value:2.9.2")
}