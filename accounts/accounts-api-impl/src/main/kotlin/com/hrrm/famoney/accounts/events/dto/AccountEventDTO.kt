package com.hrrm.famoney.accounts.events.dto

import com.fasterxml.jackson.annotation.JsonSubTypes
import com.fasterxml.jackson.annotation.JsonTypeInfo
import io.swagger.v3.oas.annotations.media.DiscriminatorMapping
import io.swagger.v3.oas.annotations.media.Schema

@Schema(
    name = "AccountEvent",
    subTypes = [AccountAddEventDTO::class, AccountChangeEventDTO::class, AccountDeleteEventDTO::class, MovementEventDTO::class],
    discriminatorProperty = "event",
    discriminatorMapping = [DiscriminatorMapping(
        schema = AccountAddEventDTO::class,
        value = AccountEventDTO.ADD_EVENT
    ), DiscriminatorMapping(
        schema = AccountChangeEventDTO::class,
        value = AccountEventDTO.CHANGE_EVENT
    ), DiscriminatorMapping(schema = AccountDeleteEventDTO::class, value = AccountEventDTO.DELETE_EVENT)]
)
@JsonSubTypes(
    JsonSubTypes.Type(name = AccountEventDTO.ADD_EVENT, value = AccountAddEventDTO::class),
    JsonSubTypes.Type(name = AccountEventDTO.CHANGE_EVENT, value = AccountChangeEventDTO::class),
    JsonSubTypes.Type(name = AccountEventDTO.DELETE_EVENT, value = AccountDeleteEventDTO::class)
)
@JsonTypeInfo(use = JsonTypeInfo.Id.NAME, include = JsonTypeInfo.As.PROPERTY, property = "event")
interface AccountEventDTO : EventDTO {
    @get:Schema(required = true)
    val accountId: Int

    companion object {
        const val ADD_EVENT = "accountAdd"
        const val CHANGE_EVENT = "accountChange"
        const val DELETE_EVENT = "accountDelete"
    }
}