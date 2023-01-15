package com.hrrm.famoney.accounts.migration.v01

import com.hrrm.famoney.commons.persistence.migration.JdbcMigrationStetemnets
import org.springframework.core.io.ResourceLoader
import java.sql.Connection

class AccountsJdbcStatements(override val connection: Connection, override val resourceLoader: ResourceLoader) : JdbcMigrationStetemnets(
) {
    val accountInsert by lazy {
        getStatementWithGeneratedKeys("account_insert.sql")
    }

    val accountTagInsert by lazy {
        getStatement("account_tag_insert.sql")
    }

    override val basePath = "migration-scripts/accounts"
}