dependencies {
    implementation("org.springframework.boot:spring-boot-starter-json")
    implementation(project(":commons:commons-core"))
    api(project(":commons:commons-persistence"))
    implementation(project(":domain:domain-migration"))
}