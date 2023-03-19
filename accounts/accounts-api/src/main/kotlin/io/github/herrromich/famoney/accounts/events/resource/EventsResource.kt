package io.github.herrromich.famoney.accounts.events.resource

import io.github.herrromich.famoney.accounts.events.dto.AccountEventDTO
import io.swagger.v3.oas.annotations.OpenAPIDefinition
import io.swagger.v3.oas.annotations.info.Info
import io.swagger.v3.oas.annotations.tags.Tag
import jakarta.ws.rs.GET
import jakarta.ws.rs.Path

@OpenAPIDefinition(
    info = Info(title = "AccountEvents", version = "1.0.0"),
    tags = [Tag(name = "account-events", description = "Account events related requests.")]
)
@Path("accounts/events")
@Tag(name = "Events")
interface EventsResource {
    @GET
    fun getAccountEvent(): AccountEventDTO
}
