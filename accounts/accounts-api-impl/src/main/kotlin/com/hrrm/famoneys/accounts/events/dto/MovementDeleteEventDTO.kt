package com.hrrm.famoneys.accounts.events.dto

import com.fasterxml.jackson.annotation.JsonTypeName
import com.hrrm.famoneys.commons.immutables.ImmutableStyle
import io.swagger.v3.oas.annotations.extensions.Extension
import io.swagger.v3.oas.annotations.media.Schema
import org.immutables.value.Value

@Schema(
    name = "MovementDeleteEvent",
    allOf = [MovementEventDTO::class],
    extensions = [Extension(
        properties = [ExtensionProperty(
            name = "x-discriminator-value",
            value = MovementEventDTO.Companion.DELETE_EVENT
        )]
    )]
)
@JsonTypeName(MovementEventDTO.Companion.DELETE_EVENT)
@Value.Immutable
@ImmutableStyle
interface MovementDeleteEventDTO : MovementEventDTO