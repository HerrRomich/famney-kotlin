package io.github.herrromich.famoney.domain.migration

import jakarta.annotation.PostConstruct
import org.flywaydb.core.Flyway
import org.springframework.context.annotation.Configuration
import javax.sql.DataSource

@Configuration
class FlywayConfig(
    val dataSource: DataSource,
    val migrations: List<DomainMigration>
) {
    @PostConstruct
    fun migrate() {
        Flyway.configure(this.javaClass.classLoader)
            .dataSource(dataSource)
            .locations("io/github/herrromich/famoney/domain/migration")
            .javaMigrations(*migrations.toTypedArray())
            .schemas("famoney")
            .load()
            .migrate()
    }

}