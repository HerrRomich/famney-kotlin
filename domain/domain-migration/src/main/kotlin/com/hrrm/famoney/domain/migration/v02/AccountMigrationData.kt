package com.hrrm.famoney.domain.migration.v02

import java.math.BigDecimal
import java.time.LocalDate

data class MigrationAccounts(
    val accounts: Map<String, List<MigrationMovement>>
)

data class MigrationAccount(
    val id: Int,
    val name: String,
    val movements: List<MigrationMovement>,
)

data class MigrationMovement(
    val amount: BigDecimal,
    val category: String?,
    val comment: String?,
    val date: LocalDate,
    val bookingDate: LocalDate?,
    val budgetPeriod: LocalDate?,
    val items: List<MigrationMovementItem>?,
    val oppositAccount: String?,
    val tag: String?,
    val type: String
)

data class MigrationMovementItem(
    val amount: BigDecimal,
    val category: String,
    val comment: String?,
    val tag: String?,
    val type: String
)