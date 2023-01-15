package com.hrrm.famoney.accounts.api

import com.fasterxml.jackson.annotation.JsonInclude
import com.fasterxml.jackson.annotation.JsonSubTypes
import com.fasterxml.jackson.annotation.JsonTypeInfo
import com.hrrm.famoney.jaxrs.DTO
import io.swagger.v3.oas.annotations.media.DiscriminatorMapping
import io.swagger.v3.oas.annotations.media.Schema
import java.math.BigDecimal
import java.time.LocalDate

@Schema(
    name = "MovementData",
    subTypes = [EntryDataDTO::class, RefundDataDTO::class, TransferDataDTO::class],
    discriminatorProperty = "type",
    discriminatorMapping = [DiscriminatorMapping(schema = EntryDataDTO::class, value = "entry"), DiscriminatorMapping(
        schema = RefundDataDTO::class,
        value = "refund"
    ), DiscriminatorMapping(schema = TransferDataDTO::class, value = "transfer")]
)
@JsonSubTypes(
    JsonSubTypes.Type(name = "entry", value = EntryDataDTO::class),
    JsonSubTypes.Type(name = "refund", value = RefundDataDTO::class),
    JsonSubTypes.Type(name = "transfer", value = TransferDataDTO::class)
)
@JsonTypeInfo(use = JsonTypeInfo.Id.NAME, include = JsonTypeInfo.As.PROPERTY, property = "type")
interface MovementDataDTO : DTO {
    @get:Schema(required = true)
    val date: LocalDate

    @get:JsonInclude(JsonInclude.Include.NON_NULL)
    val bookingDate: LocalDate?

    @get:JsonInclude(JsonInclude.Include.NON_NULL)
    val budgetPeriod: LocalDate?

    @get:Schema(required = true)
    val amount: BigDecimal
}