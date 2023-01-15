package com.hrrm.famoney.accounts.api;

import com.hrrm.famoney.commons.immutables.ImmutableStyle;
import com.hrrm.famoney.jaxrs.DTO;
import com.hrrm.famoney.jaxrs.IdDTO;
import io.swagger.v3.oas.annotations.media.Schema;
import org.immutables.value.Value;
import org.jetbrains.annotations.NotNull;

import java.math.BigDecimal;

@Schema(
        name = "Account",
        allOf = { AccountDataDTO.class }
)
@Value.Immutable
@ImmutableStyle
public interface AccountDTO extends DTO, IdDTO, AccountDataDTO {

    @Schema(required = true)
    @NotNull
    Integer getMovementCount();

    @Schema(required = true)
    @NotNull
    BigDecimal getTotal();

}
