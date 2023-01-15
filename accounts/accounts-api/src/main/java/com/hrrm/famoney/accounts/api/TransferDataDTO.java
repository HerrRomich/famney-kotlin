package com.hrrm.famoney.accounts.api;

import com.fasterxml.jackson.annotation.JsonTypeName;
import com.hrrm.famoney.commons.immutables.ImmutableStyle;
import io.swagger.v3.oas.annotations.extensions.Extension;
import io.swagger.v3.oas.annotations.extensions.ExtensionProperty;
import io.swagger.v3.oas.annotations.media.Schema;
import org.immutables.value.Value;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

@Schema(name = "TransferData", allOf = {
        MovementDataDTO.class
}, extensions = {
        @Extension(properties = {
                @ExtensionProperty(name = "x-discriminator-value", value = "transfer")
        })
})
@JsonTypeName("transfer")
@Value.Immutable
@ImmutableStyle
public interface TransferDataDTO extends MovementDataDTO {

    class Builder extends TransferDataDTOImpl.Builder implements MovementDataDTOBuilder<TransferDataDTO> {
    }

    @Schema(required = true)
    @NotNull
    Integer getOppositAccountId();

    @Nullable
    String getComments();

}
