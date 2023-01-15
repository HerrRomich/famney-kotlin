package com.hrrm.famoneys.accounts.events.dto

import com.fasterxml.jackson.annotation.JsonTypeName
import com.hrrm.famoney.commons.immutables.ImmutableStyle
import io.swagger.v3.oas.annotations.extensions.Extension
import io.swagger.v3.oas.annotations.extensions.ExtensionProperty
import io.swagger.v3.oas.annotations.media.Schema
import org.immutables.value.Value
import java.util.*

@Schema(
    name = "MovementAddEvent",
    allOf = [MovementEventDTO::class],
    extensions = [Extension(
        properties = [ExtensionProperty(
            name = "x-discriminator-value",
            value = MovementEventDTO.Companion.ADD_EVENT
        )]
    )]
)
@JsonTypeName(MovementEventDTO.Companion.ADD_EVENT)
@Value.Immutable
@ImmutableStyle
interface MovementAddEventDTO : MovementEventDTO {
    val movementData: Optional<Any?>?
}