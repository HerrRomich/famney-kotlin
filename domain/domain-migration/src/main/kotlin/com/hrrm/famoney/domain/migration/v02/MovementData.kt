package com.hrrm.famoney.domain.migration.v02

import java.math.BigDecimal
import java.time.LocalDate

data class MovementData (
    val accountId: Int,
    val type: String,
    val date: LocalDate,
    val pos: Int,
    val bookingDate: LocalDate?,
    val budgetPeriod: LocalDate?,
    val amount: BigDecimal,
    val entryItems: List<EntryItemData>?,
    val categoryId: Int?,
    val comment: String?,
    val oppositAccountId: Int?
)

