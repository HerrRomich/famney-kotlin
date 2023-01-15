package com.hrrm.famoney.accounts.api;

import com.fasterxml.jackson.annotation.JsonTypeName;
import com.hrrm.famoney.commons.immutables.ImmutableStyle;
import io.swagger.v3.oas.annotations.extensions.Extension;
import io.swagger.v3.oas.annotations.extensions.ExtensionProperty;
import io.swagger.v3.oas.annotations.media.Schema;
import org.immutables.value.Value;

@Schema(name = "RefundData", allOf = {
        MovementDataDTO.class
}, extensions = {
        @Extension(properties = {
                @ExtensionProperty(name = "x-discriminator-value", value = "refund")
        })
})
@JsonTypeName("refund")
@Value.Immutable
@ImmutableStyle
public interface RefundDataDTO extends MovementDataDTO, EntryItemDataDTO {

    public class Builder extends RefundDataDTOImpl.Builder implements MovementDataDTOBuilder<RefundDataDTO> {

    }

}
