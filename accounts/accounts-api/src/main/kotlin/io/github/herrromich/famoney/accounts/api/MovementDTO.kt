package io.github.herrromich.famoney.accounts.api

import io.github.herrromich.famoney.jaxrs.DTO
import io.github.herrromich.famoney.jaxrs.IdDTO
import io.swagger.v3.oas.annotations.media.Schema
import java.math.BigDecimal

@Schema(name = "Movement")
data class MovementDTO(
    override val id: Int,
    val data: MovementDataDTO,
    @get:Schema(required = true)
    val position: Int,
    @get:Schema(required = true)
    val total: BigDecimal,
) : DTO, IdDTO
