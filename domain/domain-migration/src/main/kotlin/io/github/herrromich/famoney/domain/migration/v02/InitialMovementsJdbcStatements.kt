package io.github.herrromich.famoney.domain.migration.v02

import io.github.herrromich.famoney.commons.persistence.migration.JdbcMigrationStetemnets
import java.sql.Connection

class InitialMovementsJdbcStatements(
    public override val connection: Connection
) : JdbcMigrationStetemnets() {
    val accountIdByNameSelect by lazy {
        getStatement(
            """
select a.id from account a
 where a.name = ?
 """
        )
    }

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
     , 0)
"""
        )
    }

    val accountMovementsMinMaxDatesSelect by lazy {
        getStatement(
            """
select min(date)
     , max(date)
     , min(booking_date)
     , max(booking_date)
  from movement
 where account_id = ?
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

    val accountMovementsSumCountUpdate by lazy {
        getStatement(
            """
update account a
  set movement_count = ?
    , movement_total = ?
 where a.id = ?
 """
        )
    }

    val accountMovementsSumCountBetweenMovementDatesSelect by lazy {
        getStatement(
            """
select count(*)
     , sum(amount)
  from movement
 where date between ? and ?
   and account_id = ?
"""
        )
    }

    val accountMovementsSumCountBetweenBookingDatesSelect by lazy {
        getStatement(
            """
select count(*)
     , sum(amount)
  from movement
 where booking_date between ? and ?
   and account_id = ?
"""
        )
    }

    val movementSliceInsert by lazy {
        getStatement(
            """
insert into movement_slice(account_id
                         , date
                         , movement_count
                         , movement_sum
                         , booking_count
                         , booking_sum)
values(?
     , ?
     , ?
     , ?
     , ?
     , ?)
"""
        )
    }

    val accountMovementsMaxDateBetweenDatesSelect by lazy {
        getStatement(
            """
select max(date)
  from movement
 where date between ? and ?
   and account_id = ?
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

    val categoryByParentName by lazy {
        getStatement(
            """
select id
  from entry_category
 where parent_id = ?
   and type = ?
"""
        )
    }

    val categoryByParentIdAndName by lazy {
        getStatementWithGeneratedKeys(
            """
insert into entry_category(type 
                         , parent_id 
                         , name)
values (?
      , ? 
      , ?)
on conflict do nothing
returning id"""
        )
    }
}