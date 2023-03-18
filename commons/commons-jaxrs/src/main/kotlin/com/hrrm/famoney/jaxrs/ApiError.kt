package com.hrrm.famoney.jaxrs

import jakarta.ws.rs.core.Response

interface ApiError {
    val code: String
    val message: String
    val prefix: String
    val status: Response.Status
}