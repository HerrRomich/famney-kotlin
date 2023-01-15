package com.hrrm.famoneys.accounts.events.dto

import com.fasterxml.jackson.annotation.JsonSubTypes
import io.swagger.v3.oas.annotations.media.DiscriminatorMapping
import io.swagger.v3.oas.annotations.media.Schema

@Schema(
    name = "MovementEvent",
    allOf = [AccountEventDTO::class],
    subTypes = [MovementAddEventDTO::class, MovementChangeEventDTO::class, MovementDeleteEventDTO::class],
    discriminatorProperty = "event",
    discriminatorMapping = [DiscriminatorMapping(
        schema = MovementAddEventDTO::class,
        value = MovementEventDTO.ADD_EVENT
    ), DiscriminatorMapping(
        schema = MovementChangeEventDTO::class,
        value = MovementEventDTO.CHANGE_EVENT
    ), DiscriminatorMapping(schema = MovementDeleteEventDTO::class, value = MovementEventDTO.DELETE_EVENT)]
)
@JsonSubTypes(
    JsonSubTypes.Type(name = MovementEventDTO.ADD_EVENT, value = MovementAddEventDTO::class),
    JsonSubTypes.Type(name = MovementEventDTO.CHANGE_EVENT, value = MovementChangeEventDTO::class),
    JsonSubTypes.Type(name = MovementEventDTO.DELETE_EVENT, value = MovementDeleteEventDTO::class)
)
interface MovementEventDTO : AccountEventDTO {
    @get:Schema(required = true)
    val position: Int?

    companion object {
        const val ADD_EVENT = "movementAdd"
        const val CHANGE_EVENT = "movementChange"
        const val DELETE_EVENT = "movementDelete"
    }
}