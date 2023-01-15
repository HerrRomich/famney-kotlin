package com.hrrm.famoney.jaxrs;

import com.hrrm.famoney.commons.immutables.ImmutableStyle;
import io.swagger.v3.oas.annotations.media.Schema;
import org.immutables.value.Value;
import org.jetbrains.annotations.Nullable;

@Schema(name = "ApiError")
@Value.Immutable
@ImmutableStyle
public interface ApiErrorDTO extends DTO {

    String getCode();

    String getMessage();

    @Nullable
    String getDescription();

}
