package io.github.herrromich.famoney.accounts.api.dto

import com.fasterxml.jackson.annotation.JsonTypeName
import io.swagger.v3.oas.annotations.extensions.Extension
import io.swagger.v3.oas.annotations.extensions.ExtensionProperty
import io.swagger.v3.oas.annotations.media.Schema
import java.math.BigDecimal
import java.time.LocalDate

@Schema(
    name = "EntryData",
    allOf = [MovementDataDTO::class],
    extensions = [Extension(properties = [ExtensionProperty(name = "x-discriminator-value", value = EntryDataDTO.TYPE_NAME)])]
)
@JsonTypeName(EntryDataDTO.TYPE_NAME)
data class EntryDataDTO(
    override val date: LocalDate,
    override val bookingDate: LocalDate?,
    override val budgetPeriod: LocalDate?,
    override val amount: BigDecimal,
    @get:Schema(required = true)
    val entryItems: List<EntryItemDataDTO>,
) : MovementDataDTO {
    companion object {
        const val TYPE_NAME = "ENTRY"
    }
}
