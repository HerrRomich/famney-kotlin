package io.github.herrromich.famoney.accounts.api

import com.fasterxml.jackson.annotation.JsonTypeName
import io.swagger.v3.oas.annotations.extensions.Extension
import io.swagger.v3.oas.annotations.extensions.ExtensionProperty
import io.swagger.v3.oas.annotations.media.Schema
import java.math.BigDecimal
import java.time.LocalDate

@Schema(
    name = "RefundData",
    allOf = [MovementDataDTO::class],
    extensions = [Extension(
        properties = [ExtensionProperty(
            name = "x-discriminator-value",
            value = RefundDataDTO.TYPE_NAME
        )]
    )]
)
@JsonTypeName(RefundDataDTO.TYPE_NAME)
data class RefundDataDTO(
    override val date: LocalDate,
    override val bookingDate: LocalDate?,
    override val budgetPeriod: LocalDate?,
    override val categoryId: Int,
    override val amount: BigDecimal,
    override val comments: String?,
) : MovementDataDTO, EntryItemDataDTO {
    companion object {
        const val TYPE_NAME = "REFUND"
    }
}
