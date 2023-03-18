package com.hrrm.famoney.jaxrs

import jakarta.ws.rs.core.Response

class ApiException : RuntimeException {
    val errorCode: String
    val errorDescription: String?
    val responseStatus: Response.Status

    constructor(message: String?) : super(message) {
        errorCode = COMMON_ERROR_PREFIX
        errorDescription = null
        responseStatus = Response.Status.INTERNAL_SERVER_ERROR
    }

    constructor(error: ApiError, description: String?, cause: Throwable?) : super(error.message, cause) {
        errorCode = (error.prefix
                + "-"
                + error.code)
        errorDescription = description
        responseStatus = error.status
    }

    @JvmOverloads
    constructor(error: ApiError, description: String? = null) : super(error.message) {
        errorCode = (error.prefix
                + "-"
                + error.code)
        errorDescription = description
        responseStatus = error.status
    }

    val errorMessage: String
        get() = message!!

    companion object {
        private const val COMMON_ERROR_PREFIX = "common"
    }
}