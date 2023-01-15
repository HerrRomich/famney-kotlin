package com.hrrm.famoneys.accounts.events.resource

import com.hrrm.famoneys.accounts.events.dto.AccountEventDTO
import io.swagger.v3.oas.annotations.OpenAPIDefinition
import io.swagger.v3.oas.annotations.info.Info
import io.swagger.v3.oas.annotations.tags.Tag
import javax.ws.rs.GET
import javax.ws.rs.Path

@OpenAPIDefinition(
    info = Info(title = "AccountEvents", version = "1.0.0"),
    tags = [Tag(name = "account-events", description = "Account eventss related requests.")]
)
@Path("accounts/events")
@Tag(name = "Events")
interface EventsResource {
    @get:GET
    val accountEvent: AccountEventDTO?
}