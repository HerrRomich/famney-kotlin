package io.github.herrromich.famoney.accounts.api.resources

import io.swagger.v3.oas.annotations.responses.ApiResponse
import io.swagger.v3.oas.annotations.tags.Tag
import jakarta.ws.rs.GET
import jakarta.ws.rs.Path
import jakarta.ws.rs.Produces
import jakarta.ws.rs.core.MediaType

@Path("tags")
@Tag(name = "accounts")
interface TagsApi {
    @ApiResponse(description = "A list of all account tags")
    @Produces(MediaType.APPLICATION_JSON)
    @GET
    fun allAccountTags(): List<String>
}