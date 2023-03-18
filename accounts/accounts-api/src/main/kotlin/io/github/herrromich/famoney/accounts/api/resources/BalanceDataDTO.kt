package io.github.herrromich.famoney.accounts.api.resources

import com.fasterxml.jackson.annotation.JsonTypeName
import io.github.herrromich.famoney.accounts.api.MovementDataDTO
import io.swagger.v3.oas.annotations.extensions.Extension
import io.swagger.v3.oas.annotations.extensions.ExtensionProperty
import io.swagger.v3.oas.annotations.media.Schema
import java.math.BigDecimal
import java.time.LocalDate

@Schema(
    name = "BalanceData",
    allOf = [MovementDataDTO::class],
    extensions = [Extension(properties = [ExtensionProperty(name = "x-discriminator-value", value = "balance")])]
)
@JsonTypeName("BALANCE")
data class BalanceDataDTO(
    override val date: LocalDate,
    override val bookingDate: LocalDate?,
    override val budgetPeriod: LocalDate?,
    override val amount: BigDecimal,
) : MovementDataDTO