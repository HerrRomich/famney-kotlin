package io.github.herrromich.famoney.accounts.events.dto

import com.fasterxml.jackson.annotation.JsonTypeName
import io.swagger.v3.oas.annotations.extensions.Extension
import io.swagger.v3.oas.annotations.extensions.ExtensionProperty
import io.swagger.v3.oas.annotations.media.Schema
import java.time.LocalDateTime

@Schema(
    name = "AccountAddEvent",
    allOf = [AccountEventDTO::class],
    extensions = [Extension(
        properties = [ExtensionProperty(
            name = "x-discriminator-value",
            value = AccountEventDTO.ADD_EVENT
        )]
    )]
)
@JsonTypeName(AccountEventDTO.ADD_EVENT)
data class AccountAddEventDTO(
    override val accountId: Int,
    override val timestamp: LocalDateTime
) : AccountEventDTO
