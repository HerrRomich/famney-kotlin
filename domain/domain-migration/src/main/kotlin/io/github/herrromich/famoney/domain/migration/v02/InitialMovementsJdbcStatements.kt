package io.github.herrromich.famoney.domain.migration.v02

import io.github.herrromich.famoney.commons.persistence.migration.JdbcMigrationStetemnets
import java.sql.Connection

class InitialMovementsJdbcStatements(
    public override val connection: Connection
) : JdbcMigrationStetemnets() {
    val accountsSelect by lazy {
        getStatement(
            """
select a.id, a.name from account a
 """
        )
    }

    val accountMovementInsert by lazy {
        getStatementWithGeneratedKeys(
            """
insert into movement(account_id
                   , type
                   , date
                   , pos
                   , booking_date
                   , budget_period
                   , category_id
                   , comments
                   , opposit_account_id
                   , amount
                   , total)
values(?
     , ?
     , ?
     , ?
     , ?
     , ?
     , ?
     , ?
     , ?
     , ?
     , ?)
"""
        )
    }

    val accountsMovementsSumCountSelect by lazy {
        getStatement(
            """
select count(*)
     , sum(amount)
     , account_id
  from movement
 group by account_id
 """
        )
    }

    val movmentMaxPosByAccountIdAndDate by lazy {
        getStatement(
            """
select max (pos)
  from movement
 where account_id = ?
   and date = ?
""")
    }

    val accountMovementsSumUpdate by lazy {
        getStatement(
            """
update account a
  set movement_total = ?
 where a.id = ?
 """
        )
    }

    val entryItemInsert by lazy {
        getStatement(
            """
insert into entry_item(entry_id
                     , pos
                     , category_id
                     , comments
                     , amount)
values(?
     , ?
     , ?
     , ?
     , ?)
"""
        )
    }

    val categoryByParentIdAndName by lazy {
        getStatementWithGeneratedKeys(
            """
insert into entry_category(budget_id 
                         , type
                         , parent_id 
                         , name)
values (?
      , ? 
      , ? 
      , ?)
on conflict do nothing
returning id"""
        )
    }
}
