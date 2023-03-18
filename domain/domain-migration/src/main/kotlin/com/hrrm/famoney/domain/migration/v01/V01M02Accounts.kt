package com.hrrm.famoney.domain.migration.v01

import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.module.kotlin.readValue
import com.hrrm.famoney.commons.persistence.migration.MigrationException
import com.hrrm.famoney.domain.migration.AccountsJavaMigration
import mu.KotlinLogging
import org.flywaydb.core.api.MigrationVersion
import org.flywaydb.core.api.migration.Context
import org.springframework.core.io.ResourceLoader
import org.springframework.stereotype.Service
import java.sql.Date
import java.sql.PreparedStatement
import java.sql.ResultSet
import java.sql.SQLException

@Service
class V01M02Accounts(val resourceLoader: ResourceLoader, val objectMapper: ObjectMapper) : AccountsJavaMigration {
    private val logger = KotlinLogging.logger { }

    override fun getVersion() = MigrationVersion.fromVersion("01.02")

    override fun getDescription() = "Creates data of test accounts."

    override fun getChecksum() = 0x4f673c5f

    override fun canExecuteInTransaction() = true

    override fun migrate(context: Context) {
        logger.info { "Starting migration: \"$version\"" }
        try {
            AccountsJdbcStatements(
                context.connection
            ).use { jdbcStatements ->
                this::class.java.getResourceAsStream("V1M2Accounts.json").use { dataStream ->
                    val data= objectMapper.readValue<MigrationAccounts>(dataStream)
                    data.accounts.forEach { account ->
                        insertAccountData(
                            jdbcStatements, account
                        )
                    }
                }
                logger.info { "Migration: \"$version\" is successfully completed." }
            }
        } catch (e: SQLException) {
            throw MigrationException("Statement failed.", e)
        } catch (e: Exception) {
            logger.error(e) { "Migration: \"$version\" is failed." }
            throw e
        }
    }

    private fun insertAccountData(jdbcStatements: AccountsJdbcStatements, account: MigrationAccount) {
        logger.debug { "Inserting account data for account: \"${account.name}\"." }
        logger.trace {
            """Inserting account data: 
              |$account.""".trimMargin()
        }
        val accountId = insertAccount(
            jdbcStatements.accountInsert, account
        )
        account.tags.forEach { accountTag ->
            insertAccountTag(jdbcStatements.accountTagInsert, accountId, accountTag)
        }
        logger.debug { "Account data for account: \"${account.name}\" is successfully inserted." }
        logger.trace {
            """Inserting account data: 
                |$account""".trimMargin()
        }
    }

    private fun insertAccount(stmt: PreparedStatement, account: MigrationAccount): Int {
        logger.debug { "Inserting account: \"${account.name}\"." }
        try {
            callInsertAccount(
                stmt, account
            ).use { generatedKeys ->
                logger.debug { "Account: \"${account.name}\" is successfully inserted." }
                if (generatedKeys.next()) {
                    val accountId: Int = generatedKeys.getInt(1)
                    logger.trace { "Account: \"${account.name}\" is successfully inserted with id: $accountId." }
                    return accountId
                }
                logger.error { "Account: \"${account.name}\" couldn't be inserted." }
                throw MigrationException("Account: \"${account.name}\" couldn't be inserted.")
            }
        } catch (e: SQLException) {
            logger.error(e) { "Account: \"${account.name}\" couldn't be inserted." }
            throw MigrationException(e)
        }
    }

    private fun callInsertAccount(stmt: PreparedStatement, account: MigrationAccount): ResultSet {
        stmt.setInt(1, 1)
        stmt.setDate(2, Date.valueOf(account.openDate))
        stmt.setString(3, account.name)
        stmt.executeUpdate()
        return stmt.generatedKeys
    }

    private fun insertAccountTag(stmt: PreparedStatement, accountId: Int, accountTag: String) {
        logger.debug { "Inserting tag: $accountTag into account with id: $accountId." }
        try {
            stmt.setInt(1, accountId)
            stmt.setString(2, accountTag)
            stmt.executeUpdate()
            logger.debug { "Tag: $accountTag is successfully inserted into account with id: $accountId." }
        } catch (e: SQLException) {
            logger.debug(e) { "Tag: $accountTag couldn't be inserted into account with id: $accountId." }
            throw MigrationException(e)
        }
    }

    companion object {
        private const val DEFAULT_BUDGET_ID = 1
    }
}