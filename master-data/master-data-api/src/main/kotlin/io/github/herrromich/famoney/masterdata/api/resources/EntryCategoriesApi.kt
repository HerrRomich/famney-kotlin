package io.github.herrromich.famoney.masterdata.api.resources

import io.github.herrromich.famoney.jaxrs.ApiErrorDTO
import io.github.herrromich.famoney.masterdata.api.dto.EntryCategoriesDTO
import io.github.herrromich.famoney.masterdata.api.dto.EntryCategoryDTO
import io.swagger.v3.oas.annotations.Operation
import io.swagger.v3.oas.annotations.media.Content
import io.swagger.v3.oas.annotations.media.Schema
import io.swagger.v3.oas.annotations.responses.ApiResponse
import io.swagger.v3.oas.annotations.tags.Tag
import jakarta.ws.rs.*
import jakarta.ws.rs.core.MediaType

@Path("entry-categories")
@Tag(name = "master-data")
interface EntryCategoriesApi {
    @ApiResponse(description = "Entry categories.")
    @Operation(description = "Gets all entry categories.")
    @Produces(MediaType.APPLICATION_JSON)
    @GET
    fun getEntryCategories(): EntryCategoriesDTO

    @POST
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    @Operation(description = "Adds a new entry category.")
    @ApiResponse(
        responseCode = "404",
        description = "No parent entry category was found for specified id.",
        content = [Content(
            mediaType = MediaType.APPLICATION_JSON,
            schema = Schema(implementation = ApiErrorDTO::class)
        )]
    )
    fun addEntryCategory(entryCategoryDto: EntryCategoryDTO<*>)

    @PUT
    @Path("{id}")
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    @Operation(description = "Changes an existing entry category.")
    @ApiResponse(description = "A changed entry category.")
    @ApiResponse(
        responseCode = "404",
        description = "No entry category was found for specified id.",
        content = [Content(mediaType = MediaType.APPLICATION_JSON, schema = Schema(implementation = ApiErrorDTO::class))]
    )
    @ApiResponse(
        responseCode = "404",
        description = "No parent entry category was found for specified id.",
        content = [Content(mediaType = MediaType.APPLICATION_JSON, schema = Schema(implementation = ApiErrorDTO::class))]
    )
    fun <T : EntryCategoryDTO<*>> changeEntryCategory(@PathParam("id") id: Int, entryCategoryDto: T): T

    @DELETE
    @Path("{id}")
    @ApiResponse(
        responseCode = "404",
        description = "No entry category was found for specified id.",
        content = [Content(mediaType = MediaType.APPLICATION_JSON, schema = Schema(implementation = ApiErrorDTO::class))]
    )
    fun deleteEntryCategory(@PathParam("id") id: Int)
}