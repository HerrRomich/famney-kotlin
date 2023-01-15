package com.hrrm.famoney.jaxrs

import io.swagger.v3.oas.annotations.media.Schema

interface IdDTO : DTO {
    @get:Schema(required = true)
    val id: Int
}