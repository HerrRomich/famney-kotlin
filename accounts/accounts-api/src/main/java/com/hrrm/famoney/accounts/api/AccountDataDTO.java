package com.hrrm.famoney.accounts.api;

import com.hrrm.famoney.commons.immutables.ImmutableStyle;
import io.swagger.v3.oas.annotations.media.Schema;
import org.immutables.value.Value;
import org.jetbrains.annotations.NotNull;

import java.time.LocalDateTime;
import java.util.Set;

@Schema(
        name = "AccountData",
        subTypes = { AccountDTO.class }
)
@Value.Immutable
@ImmutableStyle
public interface AccountDataDTO {

    @Schema(required = true)
    @NotNull
    String getName();

    @Schema(required = true)
    @NotNull
    LocalDateTime getOpenDate();

    @NotNull
    Set<String> getTags();

}
