package io.github.herrromich.famoney.domain.migration.v02

import java.sql.Connection

class MigrationData(connection: Connection) : AutoCloseable {

    private val _jdbcStatements: InitialMovementsJdbcStatements

    val categories: MutableMap<Int, MutableMap<Int?, MutableMap<String, Int>>> = mutableMapOf()

    val jdbcStatements: InitialMovementsJdbcStatements
        get() = _jdbcStatements

    init {
        _jdbcStatements = InitialMovementsJdbcStatements(connection,)
    }

    override fun close() {
        _jdbcStatements.close()
    }

}

data class Category(
    val id: Int,
    val name: String
)
