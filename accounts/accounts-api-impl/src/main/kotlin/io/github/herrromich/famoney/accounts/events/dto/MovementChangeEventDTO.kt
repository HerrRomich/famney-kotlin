package io.github.herrromich.famoney.accounts.events.dto

import com.fasterxml.jackson.annotation.JsonTypeName
import io.swagger.v3.oas.annotations.extensions.Extension
import io.swagger.v3.oas.annotations.extensions.ExtensionProperty
import io.swagger.v3.oas.annotations.media.Schema
import java.time.LocalDateTime

@Schema(
    name = "MovementChangeEvent",
    allOf = [MovementEventDTO::class],
    extensions = [Extension(
        properties = [ExtensionProperty(
            name = "x-discriminator-value",
            value = MovementEventDTO.CHANGE_EVENT
        )]
    )]
)
@JsonTypeName(MovementEventDTO.CHANGE_EVENT)
data class MovementChangeEventDTO(
    override val accountId: Int,
    override val timestamp: LocalDateTime,
    override val position: Int,
    @get:Schema(required = true)
    val positionAfter: Int
) : MovementEventDTO