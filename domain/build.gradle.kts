plugins{
    //id("org.jooq.jooq-codegen-gradle") version "3.19.1"
}

dependencies {
    implementation("org.springframework.boot:spring-boot-starter-json")
    //implementation("org.springframework.boot:spring-boot-starter-jooq")
    implementation(project(":commons:commons-core"))
    api(project(":commons:commons-persistence"))

    //jooqCodegen("org.postgresql:postgresql")
}

/*jooq {
    configuration {
        jdbc {
            driver = "org.postgresql.Driver"
            url = "jdbc:postgresql://localhost:5432/postgres"
            user = "postgres"
            password = "welcome1"
        }
        generator {
            name = "org.jooq.codegen.KotlinGenerator"
            database {
                name = "org.jooq.meta.postgres.PostgresDatabase"
                inputSchema = "famoney"
            }
            target {
                directory = sourceSets.main.get().kotlin.sourceDirectories.filter { it.endsWith("kotlin")}.first().absolutePath
            }
        }
    }
    executions {
        create("accounts") {
            configuration {
                generator {
                    database {
                        includes = "account | account_tag | movement | entry_item"
                    }
                    target {
                        packageName = "io.github.herrromich.famoney.domain.schema.accounts"
                    }
                }
            }
        }
        create("master") {
            configuration {
                generator {
                    database {
                        includes = "entry_category"
                    }
                    target {
                        packageName = "io.github.herrromich.famoney.domain.schema.master"
                    }
                }
            }
        }
    }
}*/
