package io.github.herrromich.famoney.accounts.api.resources

import io.github.herrromich.famoney.accounts.api.MovementDTO
import io.github.herrromich.famoney.accounts.api.MovementDataDTO
import io.github.herrromich.famoney.jaxrs.ApiErrorDTO
import io.swagger.v3.oas.annotations.Operation
import io.swagger.v3.oas.annotations.Parameter
import io.swagger.v3.oas.annotations.enums.ParameterIn
import io.swagger.v3.oas.annotations.media.Content
import io.swagger.v3.oas.annotations.media.Schema
import io.swagger.v3.oas.annotations.responses.ApiResponse
import io.swagger.v3.oas.annotations.tags.Tag
import jakarta.ws.rs.*
import jakarta.ws.rs.core.MediaType

@Path("{accountId}/movements")
@Tag(name = "accounts")
interface AccountMovementsApi {
    @GET
    @Produces(MediaType.APPLICATION_JSON)
    @Operation(description = "Gets a sorted list of account movements.")
    @ApiResponse(description = "A list of account movements of specified account.")
    @ApiResponse(
        responseCode = "404",
        description = "No account was found for specified id.",
        content = [Content(
            mediaType = MediaType.APPLICATION_JSON,
            schema = Schema(implementation = ApiErrorDTO::class)
        )]
    )
    fun getMovements(
        @Parameter(
            name = "accountId",
            `in` = ParameterIn.PATH,
            description = "Identifier of account, for which the movements will be searched."
        ) @PathParam("accountId") accountId: Int,
        @Parameter(
            name = "offset",
            `in` = ParameterIn.QUERY,
            description = "Offset in the ordered list of movements. If omited, then from first movement."
        ) @QueryParam("offset") offset: Int?,
        @Parameter(
            name = "limit",
            `in` = ParameterIn.QUERY,
            description = "Count of movements starting from offset. If omitted, then all from offset."
        ) @QueryParam("limit") limit: Int?
    ): List<MovementDTO?>?

    @GET
    @Path("{movementId}")
    @Produces(MediaType.APPLICATION_JSON)
    @Operation(description = "Gets a movement of account, specified by id.")
    @ApiResponse(description = "A movement of account specified by id is returned.")
    @ApiResponse(
        responseCode = "404",
        description = "No account was found for specified id.",
        content = [Content(
            mediaType = MediaType.APPLICATION_JSON,
            schema = Schema(implementation = ApiErrorDTO::class)
        )]
    )
    @ApiResponse(
        responseCode = "404",
        description = "No movement was found in an account for specified id.",
        content = [Content(
            mediaType = MediaType.APPLICATION_JSON,
            schema = Schema(implementation = ApiErrorDTO::class)
        )]
    )
    fun getMovement(
        @Parameter(
            name = "accountId",
            `in` = ParameterIn.PATH,
            description = "Identifier of account, for which the movement will be searched."
        ) @PathParam("accountId") accountId: Int,
        @Parameter(
            name = "movementId",
            `in` = ParameterIn.PATH,
            description = "Identifier of movement that will be searched."
        ) @PathParam("movementId") movementId: Int
    ): MovementDTO?

    @PUT
    @Path("{movementId}")
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    @Operation(description = "Changes an account movement.")
    @ApiResponse(
        responseCode = "201",
        description = "An existed account movement is changed.",
        content = [Content(
            mediaType = MediaType.APPLICATION_JSON,
            schema = Schema(implementation = MovementDTO::class)
        )]
    )
    @ApiResponse(
        responseCode = "404",
        description = "No account was found for specified id.",
        content = [Content(
            mediaType = MediaType.APPLICATION_JSON,
            schema = Schema(implementation = ApiErrorDTO::class)
        )]
    )
    @ApiResponse(
        responseCode = "404",
        description = "No account movement was found for specified id.",
        content = [Content(
            mediaType = MediaType.APPLICATION_JSON,
            schema = Schema(implementation = ApiErrorDTO::class)
        )]
    )
    fun changeMovement(
        @Parameter(
            name = "accountId",
            `in` = ParameterIn.PATH,
            description = "Identifier of account, for which the movements will be searched."
        ) @PathParam("accountId") accountId: Int,
        @Parameter(
            name = "movementId",
            `in` = ParameterIn.PATH,
            description = "Identifier of movement that will be searched."
        ) @PathParam("movementId") movementId: Int,
        movementDataDTO: MovementDataDTO
    ): MovementDTO

    @POST
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    @Operation(description = "Creates a new account movement.")
    @ApiResponse(description = "New account movement is created.")
    @ApiResponse(
        responseCode = "404",
        description = "No account was found for specified id.",
        content = [Content(
            mediaType = MediaType.APPLICATION_JSON,
            schema = Schema(implementation = ApiErrorDTO::class)
        )]
    )
    fun addMovement(
        @Parameter(
            name = "accountId",
            `in` = ParameterIn.PATH,
            description = "Identifier of account, for which the movements will be searched."
        ) @PathParam("accountId") accountId: Int, movementDataDTO: MovementDataDTO
    ): MovementDTO

    @DELETE
    @Path("{movementId}")
    @Operation(description = "Deletes an existing account movement.")
    @ApiResponse(responseCode = "204", description = "An account movement is deleted.")
    @ApiResponse(
        responseCode = "404",
        description = "No account movement was found for specified id.",
        content = [Content(
            mediaType = MediaType.APPLICATION_JSON,
            schema = Schema(implementation = ApiErrorDTO::class)
        )]
    )
    @ApiResponse(
        responseCode = "404",
        description = "No account movement was found for specified id.",
        content = [Content(
            mediaType = MediaType.APPLICATION_JSON,
            schema = Schema(implementation = ApiErrorDTO::class)
        )]
    )
    fun deleteMovement(
        @Parameter(
            name = "accountId",
            `in` = ParameterIn.PATH,
            description = "Identifier of account, for which the movements will be searched."
        ) @PathParam("accountId") accountId: Int,
        @Parameter(
            name = "movementId",
            `in` = ParameterIn.PATH,
            description = "Identifier of movement that will be searched."
        ) @PathParam("movementId") movementId: Int
    )
}