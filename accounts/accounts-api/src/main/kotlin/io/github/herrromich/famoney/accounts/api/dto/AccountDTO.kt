package io.github.herrromich.famoney.accounts.api.dto

import io.github.herrromich.famoney.jaxrs.DTO
import io.github.herrromich.famoney.jaxrs.IdDTO
import io.swagger.v3.oas.annotations.media.Schema
import java.math.BigDecimal
import java.time.LocalDate

@Schema(name = "Account", allOf = [AccountDataDTO::class])
data class AccountDTO(
    override val id: Int,
    override val name: String,
    override val openingDate: LocalDate,
    override val tags: Set<String>,

    @get:Schema(required = true)
    val total: BigDecimal,
) : DTO, IdDTO, AccountDataDTO
