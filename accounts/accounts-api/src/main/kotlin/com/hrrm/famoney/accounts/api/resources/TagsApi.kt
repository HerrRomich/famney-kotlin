package com.hrrm.famoney.accounts.api.resources

import io.swagger.v3.oas.annotations.responses.ApiResponse
import io.swagger.v3.oas.annotations.tags.Tag
import javax.ws.rs.GET
import javax.ws.rs.Path
import javax.ws.rs.Produces
import javax.ws.rs.core.MediaType

@Path("tags")
@Tag(name = "accounts")
interface TagsApi {
    @get:ApiResponse(description = "A list of all account tags")
    @get:Produces(MediaType.APPLICATION_JSON)
    @get:GET
    val allAccountTags: List<String>
}