dependencies {
    implementation(project(":commons:commons-persistence"))
}

tasks.classes {
    doLast {
        kotlin.sourceSets.main.get().kotlin.sourceDirectories.forEach { src ->
            copy {
                from(src) {
                    include("**/*.sql")
                    include("**/*.json")
                }
                val outputDir = sourceSets.main.get().output.classesDirs.first { gen ->
                    gen.parentFile.name == src.name
                }
                into(outputDir)
            }
        }
    }
}