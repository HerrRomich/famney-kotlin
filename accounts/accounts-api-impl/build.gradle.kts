dependencies {
    api(project(":accounts:accounts-api"))
    implementation(project(":domain"))

    implementation(project(":commons:commons-events"))

    api ("org.springframework.boot:spring-boot-starter-jersey")

}
