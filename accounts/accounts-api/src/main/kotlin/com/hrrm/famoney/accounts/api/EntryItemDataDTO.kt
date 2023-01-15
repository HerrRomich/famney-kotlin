package com.hrrm.famoney.accounts.api

import io.swagger.v3.oas.annotations.media.Schema
import java.math.BigDecimal

@Schema(name = "EntryItemData")
interface EntryItemDataDTO {
    @get:Schema(required = true)
    val categoryId: Int

    @get:Schema(required = true)
    val amount: BigDecimal
    val comments: String?
}