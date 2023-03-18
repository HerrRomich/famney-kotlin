package com.hrrm.famoney.accounts.events.dto

import com.fasterxml.jackson.annotation.JsonTypeName
import io.swagger.v3.oas.annotations.extensions.Extension
import io.swagger.v3.oas.annotations.extensions.ExtensionProperty
import io.swagger.v3.oas.annotations.media.Schema
import java.time.LocalDateTime

@Schema(
    name = "MovementDeleteEvent",
    allOf = [MovementEventDTO::class],
    extensions = [Extension(
        properties = [ExtensionProperty(
            name = "x-discriminator-value",
            value = MovementEventDTO.DELETE_EVENT
        )]
    )]
)
@JsonTypeName(MovementEventDTO.DELETE_EVENT)
data class MovementDeleteEventDTO(
    override val accountId: Int,
    override val timestamp: LocalDateTime,
    override val position: Int
) : MovementEventDTO