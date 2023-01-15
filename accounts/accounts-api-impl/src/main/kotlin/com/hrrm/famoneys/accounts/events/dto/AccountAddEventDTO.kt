package com.hrrm.famoneys.accounts.events.dto

import com.fasterxml.jackson.annotation.JsonTypeName
import com.hrrm.famoney.commons.immutables.ImmutableStyle
import io.swagger.v3.oas.annotations.extensions.Extension
import io.swagger.v3.oas.annotations.extensions.ExtensionProperty
import io.swagger.v3.oas.annotations.media.Schema
import org.immutables.value.Value

@Schema(
    name = "AccountAddEvent",
    allOf = [AccountEventDTO::class],
    extensions = [Extension(
        properties = [ExtensionProperty(
            name = "x-discriminator-value",
            value = AccountEventDTO.Companion.ADD_EVENT
        )]
    )]
)
@JsonTypeName(AccountEventDTO.Companion.ADD_EVENT)
@Value.Immutable
@ImmutableStyle
interface AccountAddEventDTO : AccountEventDTO