package io.github.herrromich.famoney.accounts.api.dto

import com.fasterxml.jackson.annotation.JsonTypeName
import io.swagger.v3.oas.annotations.extensions.Extension
import io.swagger.v3.oas.annotations.extensions.ExtensionProperty
import io.swagger.v3.oas.annotations.media.Schema
import java.math.BigDecimal
import java.time.LocalDate

@Schema(
    name = "BalanceData",
    allOf = [MovementDataDTO::class],
    extensions = [Extension(
        properties = [ExtensionProperty(
            name = "x-discriminator-value",
            value = BalanceDataDTO.TYPE_NAME
        )]
    )]
)
@JsonTypeName(BalanceDataDTO.TYPE_NAME)
data class BalanceDataDTO(
    override val date: LocalDate,
    override val bookingDate: LocalDate?,
    override val budgetPeriod: LocalDate?,
    override val amount: BigDecimal,
) : MovementDataDTO {
    companion object {
        const val TYPE_NAME = "BALANCE"
    }
}
