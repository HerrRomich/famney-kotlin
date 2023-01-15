package com.hrrm.famoney.accounts.api;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonInclude.Include;
import com.fasterxml.jackson.annotation.JsonSubTypes;
import com.fasterxml.jackson.annotation.JsonSubTypes.Type;
import com.fasterxml.jackson.annotation.JsonTypeInfo;
import com.hrrm.famoney.jaxrs.DTO;
import io.swagger.v3.oas.annotations.media.DiscriminatorMapping;
import io.swagger.v3.oas.annotations.media.Schema;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.math.BigDecimal;
import java.time.LocalDate;

@Schema(name = "MovementData", subTypes = {
        EntryDataDTO.class,
        RefundDataDTO.class,
        TransferDataDTO.class
}, discriminatorProperty = "type", discriminatorMapping = {
        @DiscriminatorMapping(schema = EntryDataDTO.class, value = "entry"),
        @DiscriminatorMapping(schema = RefundDataDTO.class, value = "refund"),
        @DiscriminatorMapping(schema = TransferDataDTO.class, value = "transfer")
})
@JsonSubTypes({
        @Type(name = "entry", value = EntryDataDTO.class),
        @Type(name = "refund", value = RefundDataDTO.class),
        @Type(name = "transfer", value = TransferDataDTO.class)
})
@JsonTypeInfo(use = JsonTypeInfo.Id.NAME, include = JsonTypeInfo.As.PROPERTY, property = "type")
public interface MovementDataDTO extends DTO {

    @Schema(required = true)
    @NotNull
    LocalDate getDate();

    @JsonInclude(Include.NON_NULL)
    @Nullable
    LocalDate getBookingDate();

    @JsonInclude(Include.NON_NULL)
    @Nullable
    LocalDate getBudgetPeriod();

    @Schema(required = true)
    @NotNull
    BigDecimal getAmount();

}
