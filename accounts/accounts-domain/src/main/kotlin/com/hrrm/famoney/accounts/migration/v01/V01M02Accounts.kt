package com.hrrm.famoney.accounts.migration.v01

import com.fasterxml.jackson.core.type.TypeReference
import com.fasterxml.jackson.databind.ObjectMapper
import com.hrrm.famoney.accounts.migration.AccountsJavaMigration
import com.hrrm.famoney.commons.persistence.migration.MigrationException
import mu.KotlinLogging
import org.flywaydb.core.api.MigrationVersion
import org.flywaydb.core.api.migration.Context
import org.springframework.core.io.ResourceLoader
import org.springframework.stereotype.Service
import java.sql.Date
import java.sql.PreparedStatement
import java.sql.ResultSet
import java.sql.SQLException

class V01M02Accounts(val resourceLoader: ResourceLoader, val objectMapper: ObjectMapper) : AccountsJavaMigration {
    private val logger = KotlinLogging.logger { }

    override fun getVersion() = MigrationVersion.fromVersion("01.02")!!

    override fun getDescription() = "Creates data of test accounts."

    override fun getChecksum() = 0x4f673c5f

    override fun isUndo() = false

    override fun isBaselineMigration() = false

    override fun canExecuteInTransaction() = true

    override fun migrate(context: Context) {
        logger.info { "Starting migration: \"$version\"" }
        try {
            AccountsJdbcStatements(
                context.connection, resourceLoader
            ).use { jdbcStatements ->
                resourceLoader.getResource("V1M2Accounts.json").inputStream.use { dataStream ->
                    val accountData = objectMapper.readValue(dataStream, object : TypeReference<AccountData>() {})
                    insertAccountData(
                        jdbcStatements, accountData
                    )
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

    private fun insertAccountData(jdbcStatements: AccountsJdbcStatements, accountData: AccountData) {
        logger.debug { "Inserting account data for account: \"${accountData.name}\"." }
        logger.trace {
            """Inserting account data: 
$accountData."""
        }
        val accountId = insertAccount(
            jdbcStatements.accountInsert, accountData
        )
        accountData.tags.forEach { accountTag ->
            insertAccountTag(jdbcStatements.accountTagInsert, accountId, accountTag)
        }
        logger.debug { "Account data for account: \"${accountData.name}\" is successfully inserted." }
        logger.trace {
            """Inserting account data: 
$accountData"""
        }
    }

    private fun insertAccount(stmt: PreparedStatement, accountData: AccountData): Int {
        logger.debug { "Inserting account: \"${accountData.name}\"." }
        try {
            callInsertAccount(
                stmt, accountData
            ).use { generatedKeys ->
                logger.debug { "Account: \"${accountData.name}\" is successfully inserted." }
                if (generatedKeys.next()) {
                    val accountId: Int = generatedKeys.getInt(1)
                    logger.trace { "Account: \"${accountData.name}\" is successfully inserted with id: $accountId." }
                    return accountId
                }
                logger.error { "Account: \"${accountData.name}\" couldn't be inserted." }
                throw MigrationException("Account: \"${accountData.name}\" couldn't be inserted.")
            }
        } catch (e: SQLException) {
            logger.error(e) { "Account: \"${accountData.name}\" couldn't be inserted." }
            throw MigrationException(e)
        }
    }

    private fun callInsertAccount(stmt: PreparedStatement, accountData: AccountData): ResultSet {
        stmt.setInt(1, 1)
        stmt.setDate(2, Date.valueOf(accountData.openDate))
        stmt.setString(3, accountData.name)
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