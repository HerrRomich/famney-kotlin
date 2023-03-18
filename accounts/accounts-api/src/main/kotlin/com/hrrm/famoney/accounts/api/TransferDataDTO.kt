package com.hrrm.famoney.accounts.api

import com.fasterxml.jackson.annotation.JsonTypeName
import io.swagger.v3.oas.annotations.extensions.Extension
import io.swagger.v3.oas.annotations.extensions.ExtensionProperty
import io.swagger.v3.oas.annotations.media.Schema
import java.math.BigDecimal
import java.time.LocalDate

@Schema(
    name = "TransferData",
    allOf = [MovementDataDTO::class],
    extensions = [Extension(
        properties = [ExtensionProperty(
            name = "x-discriminator-value",
            value = TransferDataDTO.TYPE_NAME
        )]
    )]
)
@JsonTypeName(TransferDataDTO.TYPE_NAME)
data class TransferDataDTO(
    override val date: LocalDate,
    override val bookingDate: LocalDate?,
    override val budgetPeriod: LocalDate?,
    override val amount: BigDecimal,
    @get:Schema(required = true)
    val oppositAccountId: Int,
    val comments: String?,
) : MovementDataDTO {
    companion object {
        const val TYPE_NAME = "TRANSFER"
    }
}