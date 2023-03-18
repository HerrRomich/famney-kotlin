package io.github.herrromich.famoney.domain.migration.v02

import java.math.BigDecimal

data class EntryItemData (
    val categoryId: Int,
    val amount: BigDecimal,
    val comment: String?
)
