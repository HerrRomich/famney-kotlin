package com.hrrm.famoney.accounts.migration.v02

import com.hrrm.famoney.commons.persistence.migration.JdbcMigrationStetemnets
import org.springframework.core.io.ResourceLoader
import java.sql.Connection
import java.sql.PreparedStatement

class InitialMovementsJdbcStatements(override val connection: Connection, override val resourceLoader: ResourceLoader) : JdbcMigrationStetemnets(
) {
    val accountIdByNameSelect by lazy {
        getStatement("account_name_by_id_select.sql")
    }

    val accountMovementInsert: PreparedStatement
        get() = getStatementWithGeneratedKeys("movement_insert.sql")

    val accountMovementsMinMaxDatesSelect: PreparedStatement
        get() = getStatement("account_movements_min_max_dates_select.sql")

    val accountsMovementsSumCountSelect: PreparedStatement
        get() = getStatement("accounts_movements_sum_count_select.sql")

    val accountMovementsSumCountUpdate: PreparedStatement
        get() = getStatement("account_movements_sum_count_update.sql")

    val accountMovementsSumCountBetweenMovementDatesSelect: PreparedStatement
        get() = getStatement("account_movements_sum_count_between_movement_dates_select.sql")

    val accountMovementsSumCountBetweenBookingDatesSelect: PreparedStatement
        get() = getStatement("account_movements_sum_count_between_booking_dates_select.sql")

    val movementSliceInsert: PreparedStatement
        get() = getStatement("movement_slice_insert.sql")

    val accountMovementsMaxDateBetweenDatesSelect: PreparedStatement
        get() = getStatement("account_movements_max_date_between_dates_select.sql")

    val entryItemInsert: PreparedStatement
        get() = getStatement("entry_item_insert.sql")

    override val basePath = "migration-scripts/initial_movements"
}