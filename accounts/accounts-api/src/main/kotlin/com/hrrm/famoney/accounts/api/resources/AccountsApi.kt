package com.hrrm.famoney.accounts.api.resources

import com.hrrm.famoney.accounts.api.AccountDTO
import com.hrrm.famoney.accounts.api.AccountDataDTO
import com.hrrm.famoney.jaxrs.ApiErrorDTO
import io.swagger.v3.oas.annotations.Operation
import io.swagger.v3.oas.annotations.Parameter
import io.swagger.v3.oas.annotations.media.Content
import io.swagger.v3.oas.annotations.media.Schema
import io.swagger.v3.oas.annotations.responses.ApiResponse
import io.swagger.v3.oas.annotations.tags.Tag
import jakarta.ws.rs.*
import jakarta.ws.rs.core.MediaType

@Path("accounts")
@Tag(name = "accounts")
interface AccountsApi {
    @GET
    @Produces(MediaType.APPLICATION_JSON)
    @Operation(description = "Gets a list of accounts filtered by tags.")
    @ApiResponse(description = "A list of all accounts")
    fun getAllAccounts(
        @Parameter(
            name = "tags",
            description = "List of tags to filter accounts. If empty, all accounts will be provided"
        ) @QueryParam("tags") tags: Set<String>
    ): List<AccountDTO>

    @POST
    @Consumes(MediaType.APPLICATION_JSON)
    @Operation(description = "Adds a new account.")
    @ApiResponse(description="Account is created.", responseCode = "204")
    fun addAccount(accountData: AccountDataDTO)

    @PUT
    @Path("/{accountId}")
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    @Operation(description = "Changes a specified account.")
    @ApiResponse(description = "A changed account.")
    @ApiResponse(
        responseCode = "404",
        description = "No account was found for specified id.",
        content = [Content(
            mediaType = MediaType.APPLICATION_JSON,
            schema = Schema(implementation = ApiErrorDTO::class)
        )]
    )
    fun changeAccount(@PathParam("accountId") accountId: Int, accountData: AccountDataDTO): AccountDataDTO

    @GET
    @Path("{accountId}")
    @Produces(MediaType.APPLICATION_JSON)
    @Operation(description = "Gets a detailed account info.")
    @ApiResponse(description = "A detailed account info.")
    @ApiResponse(
        responseCode = "404",
        description = "No account was found for specified id.",
        content = [Content(
            mediaType = MediaType.APPLICATION_JSON,
            schema = Schema(implementation = ApiErrorDTO::class)
        )]
    )
    fun getAccount(@PathParam("accountId") accountId: Int): AccountDTO
}