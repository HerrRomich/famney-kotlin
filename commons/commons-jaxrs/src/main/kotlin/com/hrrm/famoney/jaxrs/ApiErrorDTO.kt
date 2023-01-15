package com.hrrm.famoney.jaxrs

import io.swagger.v3.oas.annotations.media.Schema

@Schema(name = "ApiError")
data class ApiErrorDTO(
    val code: String?,
    val message: String?,
    val description: String?,
) : DTO
