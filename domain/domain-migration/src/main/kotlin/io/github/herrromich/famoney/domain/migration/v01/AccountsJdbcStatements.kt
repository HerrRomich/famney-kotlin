package io.github.herrromich.famoney.domain.migration.v01

import io.github.herrromich.famoney.commons.persistence.migration.JdbcMigrationStetemnets
import java.sql.Connection

class AccountsJdbcStatements(override val connection: Connection) :
    JdbcMigrationStetemnets(
    ) {
    val accountInsert by lazy {
        getStatementWithGeneratedKeys(
            """
insert into account(budget_id
                  , opening_date
                  , name)
values(?
     , ?
     , ?)
"""
        )
    }

    val accountTagInsert by lazy {
        getStatement("""
insert into account_tag(account_id
                      , tag)
values(?
     , ?)
""")
    }
}
