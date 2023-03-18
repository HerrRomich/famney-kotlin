package com.hrrm.famoney.accounts.api

import com.hrrm.famoney.jaxrs.DTO
import com.hrrm.famoney.jaxrs.IdDTO
import io.swagger.v3.oas.annotations.media.Schema
import java.math.BigDecimal
import java.time.LocalDate

@Schema(name = "Account", allOf = [AccountDataDTO::class])
data class AccountDTO(
    override val id: Int,
    override val name: String,
    override val openDate: LocalDate,
    override val tags: Set<String>,
    @get:Schema(required = true)
    val movementCount: Int,

    @get:Schema(required = true)
    val total: BigDecimal,
) : DTO, IdDTO, AccountDataDTO
