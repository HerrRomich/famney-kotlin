package com.hrrm.famoney.domain.migration.v02

import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.module.kotlin.readValue
import com.hrrm.famoney.commons.persistence.migration.MigrationException
import com.hrrm.famoney.domain.migration.DomainMigration
import mu.KotlinLogging
import org.flywaydb.core.api.MigrationVersion
import org.flywaydb.core.api.migration.Context
import org.springframework.stereotype.Service
import java.math.BigDecimal
import java.sql.*
import java.sql.Date
import java.time.Instant
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.temporal.ChronoUnit
import java.util.*
import kotlin.math.sign

@Service
class V2M2InitialMovements(private val objectMapper: ObjectMapper) : DomainMigration {
    private val logger = KotlinLogging.logger { }

    override fun getVersion() = MigrationVersion.fromVersion("02.02")
    override fun getDescription() = "Initial account movements"
    override fun getChecksum() = -0x5e4eb5d4
    override fun canExecuteInTransaction() = true

    override fun migrate(context: Context) {
        logger.info { "Starting migration: \"$version $description\"." }
        try {
            MigrationData(
                context.connection
            ).use { migrationData ->
                insertAccountsMovements(migrationData)
                updateAccountsMovemntsCountAndSum(migrationData)
                logger.info(
                    "Migration: \"{} {}\" is successfully completed.",
                    version,
                    description
                )
            }
        } catch (e: SQLException) {
            val message = "It is unable to perform migration: \"$version $description\"."
            logger.error(message, e)
            throw MigrationException(message, e)
        } catch (e: MigrationException) {
            val message = "It is unable to perform migration: \"$version $description\"."
            logger.error(message, e)
            throw MigrationException(message, e)
        }
    }

    private fun insertAccountsMovements(migrationData: MigrationData) {
        logger.info("Inserting initial movements.")
        this::class.java.getResourceAsStream("Pupsik_Wallet.json").use { dataStream ->
            val accountsNameToId = migrationData.jdbcStatements.accountsSelect
                .executeQuery().use { resultSet ->
                    generateSequence {
                        if (resultSet.next()) {
                            val accountId = resultSet.getInt(1)
                            val accountName = resultSet.getString(2)
                            accountName to accountId
                        } else null
                    }.toMap()
                }

            val accountsData = objectMapper.readValue<MigrationAccounts>(dataStream)
                .accounts.map { (accountName, movements) ->
                    val accountId = accountsNameToId.getValue(accountName)
                    AccountData(
                        id = accountId,
                        name = accountName,
                        movements = movements.map { movement ->
                            MovementData(
                                accountId = accountId,
                                type = MOVEMENT_TYPES.getValue(movement.type),
                                date = movement.date,
                                pos = 0,
                                bookingDate = movement.bookingDate,
                                budgetPeriod = movement.budgetPeriod,
                                amount = movement.amount,
                                entryItems = movement.items?.map { entry ->
                                    EntryItemData(
                                        categoryId = findCategoryIdByCategoryFullName(
                                            migrationData,
                                            entry.category,
                                            entry.amount.signum()
                                        ),
                                        amount = entry.amount,
                                        comment = entry.comment
                                    )
                                },
                                categoryId = movement.category?.let {
                                    findCategoryIdByCategoryFullName(
                                        migrationData,
                                        it,
                                        movement.amount.signum()
                                    )
                                },
                                comment = movement.comment,
                                oppositAccountId = movement.oppositAccount?.let { accountsNameToId.getValue(it) }
                            )
                        }
                    )
                }.toList()
            accountsData.forEach { accountData ->
                insertAccountMovements(
                    migrationData,
                    accountData,
                )
            }
            logger.info("Initial movements successfully inserted.")
        }
    }

    private fun getMaxPositionByAccountIdAndDate(migrationData: MigrationData, accountId: Int, date: LocalDate): Int {
        val stmt = migrationData.jdbcStatements.movmentMaxPosByAccountIdAndDate
        stmt.setInt(1, accountId)
        stmt.setDate(2, Date.valueOf(date))
        val resultSet = stmt.executeQuery()
        return if (resultSet.next()) resultSet.getInt(1) else 0
    }

    private fun insertAccountMovements(
        migrationData: MigrationData,
        accountData: AccountData,
    ) {
        logger.info { "Inserting initial movements for account: \"${accountData.name}\"." }
        try {
            accountData.movements.forEach { movement ->
                val pos = getMaxPositionByAccountIdAndDate(migrationData, movement.accountId, movement.date) + 1
                val movementId = insertMovement(
                    migrationData.jdbcStatements.accountMovementInsert,
                    movement.copy(pos = pos),
                )
                if (movement.type == "entry" && !movement.entryItems.isNullOrEmpty()) {
                    insertEntryItems(
                        migrationData,
                        movement.entryItems,
                        movementId
                    )
                }
            }
            insertMovmentSlices(
                migrationData,
                accountData.id
            )
            migrationData.jdbcStatements.connection
                .commit()
            logger.info { "Initial movements for account: \"${accountData.name}\" are successfully inserted." }
            logger.debug { "${accountData.movements.size} initial movements for account: \"${accountData.name}\" are successfully inserted." }
        } catch (e: MigrationException) {
            val message = "It is unable to insert movements for account: \"${accountData.name}\""
            logger.error(message, e)
            throw MigrationException(message, e)
        } catch (e: SQLException) {
            val message = "It is unable to insert movements for account: \"${accountData.name}\""
            logger.error(message, e)
            throw MigrationException(message, e)
        }
    }

    private fun findCategoryIdByCategoryFullName(
        migrationData: MigrationData,
        categoryFullName: String,
        amountSignum: Int
    ): Int {
        val categoryLevels = categoryFullName.split(": ")
        return findCategoryIdByCategoryLevels(
            migrationData,
            amountSignum,
            null,
            categoryLevels
        )
    }

    private fun findCategoryIdByCategoryLevels(
        migrationData: MigrationData,
        amountSignum: Int,
        parentId: Int?,
        categoryLevels: List<String>
    ): Int {
        val currentLevel = categoryLevels.first()
        val reducedCategoryLevels = categoryLevels.drop(1)
        val categories = migrationData.categories.getOrPut(amountSignum) { mutableMapOf(null to mutableMapOf()) }
        val parent = categories.getValue(parentId)
        val categoryId = parent.getOrPut(currentLevel) {
            val categoryId = getOrCreateCategory(
                migrationData,
                amountSignum,
                parentId,
                currentLevel
            )
            categories[categoryId] = mutableMapOf()
            return categoryId
        }
        return if (reducedCategoryLevels.isNotEmpty()) categoryId
        else findCategoryIdByCategoryLevels(
            migrationData,
            amountSignum,
            categoryId,
            reducedCategoryLevels
        )
    }

    private fun getOrCreateCategory(
        migrationData: MigrationData,
        amountSignum: Int,
        parentId: Int?,
        name: String
    ): Int {
        val stmt = migrationData.jdbcStatements.categoryByParentIdAndName
        val categoryType = when (amountSignum.sign) {
            -1 -> "EXPENSE"
            1 -> "INCOME"
            else -> throw MigrationException("Movement amount is not allowed.")
        }
        stmt.setString(1, categoryType)
        parentId?.let { stmt.setInt(2, it) } ?: stmt.setNull(2, Types.INTEGER)
        stmt.setString(3, name)
        stmt.executeUpdate()
        val generatedKeys = stmt.generatedKeys
        if (generatedKeys.next()) {
            return generatedKeys.getInt(1)
        } else {
            throw MigrationException("Could not find / create entry category.")
        }

    }

    private fun insertMovement(
        insertMovementStatement: PreparedStatement,
        movement: MovementData
    ): Int {
        logger.trace {
            """Inserting account movement: 
            |${objectMapper.writeValueAsString(movement)}.""".trimMargin()
        }
        try {
            val generatedKeys: ResultSet = callInsertMovement(
                insertMovementStatement,
                movement
            )
            if (generatedKeys.next()) {
                val movementId: Int = generatedKeys.getInt(1)
                logger.trace(
                    "Account movement is successfully inserted with id: {}.",
                    movementId
                )
                return movementId
            }
            throw MigrationException("")
        } catch (ex: Exception) {
            val message = getUnableInsertMovementMessage(movement)
            logger.error(message)
            throw MigrationException(message, ex)
        } catch (ex: SQLException) {
            val message = getUnableInsertMovementMessage(movement)
            logger.error(message)
            throw MigrationException(message, ex)
        }
    }

    private fun getUnableInsertMovementMessage(movement: MovementData) =
        """It was unable to insert an account movement:
                    |${objectMapper.writeValueAsString(movement)}.""".trimMargin()

    private fun callInsertMovement(
        insertMovementStatement: PreparedStatement,
        movement: MovementData
    ): ResultSet {
        insertMovementStatement.setInt(1, movement.accountId)
        insertMovementStatement.setString(2, movement.type)
        insertMovementStatement.setDate(3, Date.valueOf(movement.date))
        insertMovementStatement.setInt(4, movement.pos)
        movement.bookingDate?.run { insertMovementStatement.setDate(5, Date.valueOf(this)) }
            ?: insertMovementStatement.setNull(5, Types.DATE)
        movement.budgetPeriod?.run { insertMovementStatement.setDate(6, Date.valueOf(this)) }
            ?: insertMovementStatement.setNull(6, Types.DATE)
        movement.categoryId?.run { insertMovementStatement.setInt(7, this) }
            ?: insertMovementStatement.setNull(7, Types.INTEGER)
        movement.comment?.run { insertMovementStatement.setString(8, this) }
            ?: insertMovementStatement.setNull(8, Types.VARCHAR)
        movement.oppositAccountId?.run { insertMovementStatement.setInt(9, this) }
            ?: insertMovementStatement.setNull(9, Types.INTEGER)
        insertMovementStatement.setBigDecimal(10, movement.amount)
        insertMovementStatement.executeUpdate()
        return insertMovementStatement.getGeneratedKeys()
    }

    private fun insertEntryItems(migrationData: MigrationData, entryItems: List<EntryItemData>, movementId: Int) {
        val insertEntryItemStmt = migrationData.jdbcStatements.entryItemInsert
        logger.debug("Inserting entry items.")
        entryItems.forEachIndexed{ pos, entryItem ->
            insertEntryItemStmt.setInt(1, movementId)
            insertEntryItemStmt.setInt(2, pos + 1)
            insertEntryItemStmt.setInt(3, entryItem.categoryId)
            entryItem.comment?.run { insertEntryItemStmt.setString(4, this) }
                ?: insertEntryItemStmt.setNull(4, Types.VARCHAR)
            insertEntryItemStmt.setBigDecimal(5, entryItem.amount)
            insertEntryItemStmt.executeUpdate()
        }
        logger.debug { "${entryItems.size} entry items are inserted." }
    }


    private fun updateAccountsMovemntsCountAndSum(migrationData: MigrationData) {
        val accountsMovementsSumCountSelectStmt = migrationData.jdbcStatements.accountsMovementsSumCountSelect
        val accountMovementsSumCountUpdateStmt = migrationData.jdbcStatements.accountMovementsSumCountUpdate
        try {
            accountsMovementsSumCountSelectStmt.executeQuery().use { countsAndSums ->
                while (countsAndSums.next()) {
                    accountMovementsSumCountUpdateStmt.setInt(
                        1,
                        countsAndSums.getInt(1)
                    )
                    accountMovementsSumCountUpdateStmt.setBigDecimal(
                        2,
                        countsAndSums.getBigDecimal(2)
                    )
                    accountMovementsSumCountUpdateStmt.setInt(
                        3,
                        countsAndSums.getInt(3)
                    )
                    accountMovementsSumCountUpdateStmt.executeUpdate()
                }
            }
        } catch (e: SQLException) {
            throw MigrationException(
                "Cannot update account movements counts and sums.",
                e
            )
        }
    }

    private fun insertMovmentSlices(migrationData: MigrationData, accountId: Int) {
        try {
            val accountMovementsMinMaxDatesSelectStmt = migrationData.jdbcStatements.accountMovementsMinMaxDatesSelect
            val accountMovementsSumCountBetweenMovementDatesSelectStmt =
                migrationData.jdbcStatements.accountMovementsSumCountBetweenMovementDatesSelect
            val accountMovementsSumCountBetweenBookingDatesSelectStmt =
                migrationData.jdbcStatements.accountMovementsSumCountBetweenBookingDatesSelect
            val insertMovementSliceStmt = migrationData.jdbcStatements.movementSliceInsert
            accountMovementsMinMaxDatesSelectStmt.setInt(1, accountId)
            accountMovementsSumCountBetweenMovementDatesSelectStmt.setInt(3, accountId)
            accountMovementsSumCountBetweenBookingDatesSelectStmt.setInt(3, accountId)
            insertMovementSliceStmt.setInt(1, accountId)
            accountMovementsMinMaxDatesSelectStmt.executeQuery().use { resultSet ->
                if (!resultSet.next()) {
                    return
                }
                val minDateTimestamp = resultSet.getTimestamp(1)
                val maxDateTimestamp = resultSet.getTimestamp(2)
                val minBookingDateTimestamp = resultSet.getTimestamp(3)
                val maxBookingDateTimestamp = resultSet.getTimestamp(4)
                if (minDateTimestamp == null || maxDateTimestamp == null || minBookingDateTimestamp == null || maxBookingDateTimestamp == null) {
                    return
                }
                val minTime = Math.min(
                    minDateTimestamp.time,
                    minBookingDateTimestamp.time
                )
                val minDate = LocalDateTime.ofInstant(
                    Instant.ofEpochMilli(minTime),
                    TimeZone.getDefault()
                        .toZoneId()
                )
                    .truncatedTo(ChronoUnit.DAYS)
                val maxTime = Math.max(
                    maxDateTimestamp.time,
                    maxBookingDateTimestamp.time
                )
                val maxDate = LocalDateTime.ofInstant(
                    Instant.ofEpochMilli(maxTime),
                    TimeZone.getDefault()
                        .toZoneId()
                )
                    .truncatedTo(ChronoUnit.DAYS)
                var startDate = minDate.withDayOfMonth(1)
                    .plusMonths(1)
                var prevDate = minDate
                var movementCount = 0
                var movementSum = BigDecimal.ZERO
                var bookingCount = 0
                var bookingSum = BigDecimal.ZERO
                while (startDate.compareTo(maxDate) < 0) {
                    accountMovementsSumCountBetweenMovementDatesSelectStmt.setTimestamp(
                        1,
                        Timestamp.valueOf(prevDate)
                    )
                    accountMovementsSumCountBetweenMovementDatesSelectStmt.setTimestamp(
                        2,
                        Timestamp.valueOf(startDate)
                    )
                    accountMovementsSumCountBetweenMovementDatesSelectStmt
                        .executeQuery().use { countAndSumByMovementDateResultSet ->
                            if (countAndSumByMovementDateResultSet.next()) {
                                movementCount += Optional.ofNullable<Int>(countAndSumByMovementDateResultSet.getInt(1))
                                    .orElse(0)
                                movementSum = movementSum.add(
                                    Optional.ofNullable<BigDecimal>(
                                        countAndSumByMovementDateResultSet
                                            .getBigDecimal(2)
                                    )
                                        .orElse(BigDecimal.ZERO)
                                )
                            }
                        }
                    accountMovementsSumCountBetweenBookingDatesSelectStmt.setTimestamp(
                        1,
                        Timestamp.valueOf(prevDate)
                    )
                    accountMovementsSumCountBetweenBookingDatesSelectStmt.setTimestamp(
                        2,
                        Timestamp.valueOf(startDate)
                    )
                    accountMovementsSumCountBetweenBookingDatesSelectStmt
                        .executeQuery().use { countAndSumByBookingDateResultSet ->
                            if (countAndSumByBookingDateResultSet.next()) {
                                bookingCount += Optional.ofNullable<Int>(countAndSumByBookingDateResultSet.getInt(1))
                                    .orElse(0)
                                bookingSum = bookingSum.add(
                                    Optional.ofNullable<BigDecimal>(
                                        countAndSumByBookingDateResultSet
                                            .getBigDecimal(2)
                                    )
                                        .orElse(BigDecimal.ZERO)
                                )
                            }
                        }
                    insertMovementSliceStmt.setTimestamp(
                        2,
                        Timestamp.valueOf(startDate)
                    )
                    insertMovementSliceStmt.setInt(
                        3,
                        movementCount
                    )
                    insertMovementSliceStmt.setBigDecimal(
                        4,
                        movementSum
                    )
                    insertMovementSliceStmt.setInt(
                        5,
                        bookingCount
                    )
                    insertMovementSliceStmt.setBigDecimal(
                        6,
                        bookingSum
                    )
                    insertMovementSliceStmt.execute()
                    prevDate = startDate
                    startDate = startDate.plusMonths(1)
                }
            }
        } catch (e: SQLException) {
            throw MigrationException("Error adding movement slices.", e)
        }
    }

    companion object {
        private val MOVEMENT_TYPES = mapOf(
            "check" to "ENTRY",
            "transfer" to "TRANSFER",
            "entry" to "ENTRY",
            "balance" to "BALANCE"
        )
    }
}