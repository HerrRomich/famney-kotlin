package io.github.herrromich.famoney.accounts.api

import io.swagger.v3.oas.annotations.media.Schema
import java.math.BigDecimal

interface EntryItemDataDTO {
    @get:Schema(required = true)
    val categoryId: Int

    @get:Schema(required = true)
    val amount: BigDecimal
    val comments: String?
}

@Schema(name = "EntryItemData")
data class BasicEntryItemDataDTO(
    override val categoryId: Int,
    override val amount: BigDecimal,
    override val comments: String?,
) : EntryItemDataDTO
