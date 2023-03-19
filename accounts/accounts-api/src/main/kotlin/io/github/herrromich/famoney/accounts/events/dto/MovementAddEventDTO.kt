package io.github.herrromich.famoney.accounts.events.dto

import com.fasterxml.jackson.annotation.JsonTypeName
import io.swagger.v3.oas.annotations.extensions.Extension
import io.swagger.v3.oas.annotations.extensions.ExtensionProperty
import io.swagger.v3.oas.annotations.media.Schema
import java.time.LocalDateTime
import java.util.*

@Schema(
    name = "MovementAddEvent",
    allOf = [MovementEventDTO::class],
    extensions = [Extension(
        properties = [ExtensionProperty(
            name = "x-discriminator-value",
            value = MovementEventDTO.ADD_EVENT
        )]
    )]
)
@JsonTypeName(MovementEventDTO.ADD_EVENT)
data class MovementAddEventDTO(
    override val accountId: Int,
    override val timestamp: LocalDateTime,
    override val position: Int,
    val movementData: Any?
) : MovementEventDTO
