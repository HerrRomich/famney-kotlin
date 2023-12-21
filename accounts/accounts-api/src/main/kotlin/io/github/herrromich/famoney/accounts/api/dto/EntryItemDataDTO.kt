package io.github.herrromich.famoney.accounts.api.dto

import com.fasterxml.jackson.databind.annotation.JsonDeserialize
import io.swagger.v3.oas.annotations.media.Schema
import java.math.BigDecimal

@Schema(name = "EntryItemData")
@JsonDeserialize(`as` =  BasicEntryItemDataDTO::class)
interface EntryItemDataDTO {
    @get:Schema(required = true)
    val categoryId: Int

    @get:Schema(required = true)
    val amount: BigDecimal
    val comments: String?
}

data class BasicEntryItemDataDTO(
    override val categoryId: Int,
    override val amount: BigDecimal,
    override val comments: String?,
) : EntryItemDataDTO
