package io.github.herrromich.famoney.accounts.api.resources

import io.github.herrromich.famoney.accounts.api.AccountsApiResource
import io.github.herrromich.famoney.accounts.api.dto.AccountDTO
import io.github.herrromich.famoney.accounts.api.dto.AccountDataDTO
import io.github.herrromich.famoney.jaxrs.ApiErrorDTO
import io.swagger.v3.oas.annotations.Operation
import io.swagger.v3.oas.annotations.media.Content
import io.swagger.v3.oas.annotations.media.Schema
import io.swagger.v3.oas.annotations.responses.ApiResponse
import io.swagger.v3.oas.annotations.tags.Tag
import jakarta.ws.rs.*
import jakarta.ws.rs.core.MediaType

@Path("accounts")
@Tag(name = "accounts")
interface AccountsResource: AccountsApiResource {
    @GET
    @Produces(MediaType.APPLICATION_JSON)
    @Operation(description = "Gets a list of accounts.")
    @ApiResponse(description = "A list of all accounts")
    fun getAllAccounts(): List<AccountDTO>

    @POST
    @Consumes(MediaType.APPLICATION_JSON)
    @Operation(description = "Adds a new account.")
    @ApiResponse(description = "Account is created.", responseCode = "204")
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
