plugins {
    kotlin("kapt")
}

dependencies {
    implementation(project(":accounts:accounts-api"))
    implementation(project(":accounts:accounts-domain"))

    implementation(project(":commons:commons-events"))

    implementation ("org.springframework.boot:spring-boot-starter-jersey")

}
