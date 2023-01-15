package com.hrrm.famoney.accounts.api

import com.hrrm.famoney.jaxrs.DTO
import com.hrrm.famoney.jaxrs.IdDTO
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
