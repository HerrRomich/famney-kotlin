package com.hrrm.famoney.accounts.api

import com.fasterxml.jackson.annotation.JsonTypeName
import io.swagger.v3.oas.annotations.extensions.Extension
import io.swagger.v3.oas.annotations.extensions.ExtensionProperty
import io.swagger.v3.oas.annotations.media.Schema
import java.math.BigDecimal
import java.time.LocalDate

@Schema(
    name = "EntryData",
    allOf = [MovementDataDTO::class],
    extensions = [Extension(properties = [ExtensionProperty(name = "x-discriminator-value", value = "entry")])]
)
@JsonTypeName("entry")
data class EntryDataDTO(
    override val date: LocalDate,
    override val bookingDate: LocalDate?,
    override val budgetPeriod: LocalDate?,
    override val amount: BigDecimal,
    @get:Schema(required = true)
    val entryItems: List<EntryItemDataDTO>,
) : MovementDataDTO
