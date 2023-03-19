package io.github.herrromich.famoney.masterdata.internalexceptions

import io.github.herrromich.famoney.jaxrs.ApiError
import jakarta.ws.rs.core.Response

enum class EntryCategoryApiError(override val message: String, override val status: Response.Status) : ApiError {
    NO_ENTRY_CATEGORY_BY_CHANGE(
        "No entry category was found for request on entry category change.",
        Response.Status.NOT_FOUND
    );

    override val code: String
        get() = toString()
    override val prefix: String
        get() = "data-dictionary"
}