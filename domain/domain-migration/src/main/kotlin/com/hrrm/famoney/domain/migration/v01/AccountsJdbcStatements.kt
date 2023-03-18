package com.hrrm.famoney.domain.migration.v01

import com.hrrm.famoney.commons.persistence.migration.JdbcMigrationStetemnets
import java.sql.Connection

class AccountsJdbcStatements(override val connection: Connection) :
    JdbcMigrationStetemnets(
    ) {
    val accountInsert by lazy {
        getStatementWithGeneratedKeys(
            """
insert into account(budget_id
                  , open_date
                  , name
                  , movement_count
                  , movement_total)
values(?
     , ?
     , ?
     , 0
     , 0)
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