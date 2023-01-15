package com.hrrm.famoney.accounts.api;

import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.hrrm.famoney.commons.immutables.ImmutableStyle;
import io.swagger.v3.oas.annotations.media.Schema;
import org.immutables.value.Value;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.math.BigDecimal;

@Schema(name = "EntryItemData")
@JsonDeserialize(builder = EntryItemDataDTO.Builder.class)
@Value.Immutable
@ImmutableStyle
public interface EntryItemDataDTO {

    public class Builder extends EntryItemDataDTOImpl.Builder {
    }

    @Schema(required = true)
    @NotNull
    Integer getCategoryId();

    @Schema(required = true)
    @NotNull
    BigDecimal getAmount();

    @Nullable
    String getComments();

}
