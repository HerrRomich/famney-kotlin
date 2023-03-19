package io.github.herrromich.famoney.accounts.events.dto

import com.fasterxml.jackson.annotation.JsonTypeName
import io.swagger.v3.oas.annotations.extensions.Extension
import io.swagger.v3.oas.annotations.extensions.ExtensionProperty
import io.swagger.v3.oas.annotations.media.Schema
import java.time.LocalDateTime

@Schema(
    name = "AccountChangeEvent",
    allOf = [AccountEventDTO::class],
    extensions = [Extension(
        properties = [ExtensionProperty(
            name = "x-discriminator-value",
            value = AccountEventDTO.CHANGE_EVENT
        )]
    )]
)
@JsonTypeName(AccountEventDTO.CHANGE_EVENT)
data class AccountChangeEventDTO(
    override val accountId: Int,
    override val timestamp: LocalDateTime,
) : AccountEventDTO
