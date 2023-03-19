dependencies {
    api(project(":commons:commons-core"))

    api("org.springframework.boot:spring-boot-starter-data-jpa") {
        exclude("org.hibernate.orm", "hibernate-core")
    }
    api("org.eclipse.persistence:eclipselink:4.0.1")
    implementation("org.postgresql:postgresql")
    api("org.flywaydb:flyway-core")

    implementation("com.google.guava:guava:31.1-jre")
}
