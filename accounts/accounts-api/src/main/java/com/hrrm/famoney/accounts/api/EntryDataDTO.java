package com.hrrm.famoney.accounts.api;

import com.fasterxml.jackson.annotation.JsonTypeName;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.hrrm.famoney.commons.immutables.ImmutableStyle;
import io.swagger.v3.oas.annotations.extensions.Extension;
import io.swagger.v3.oas.annotations.extensions.ExtensionProperty;
import io.swagger.v3.oas.annotations.media.Schema;
import org.immutables.value.Value;
import org.jetbrains.annotations.NotNull;

import java.util.List;

@Schema(name = "EntryData", allOf = {
        MovementDataDTO.class
}, extensions = {
        @Extension(properties = {
                @ExtensionProperty(name = "x-discriminator-value", value = "entry")
        })
})
@JsonTypeName("entry")
@JsonDeserialize(builder = EntryDataDTO.Builder.class)
@Value.Immutable
@ImmutableStyle
public interface EntryDataDTO extends MovementDataDTO {

    public class Builder extends EntryDataDTOImpl.Builder implements MovementDataDTOBuilder<EntryDataDTO> {

    }

    @Schema(required = true)
    @NotNull
    List<EntryItemDataDTO> getEntryItems();

}
