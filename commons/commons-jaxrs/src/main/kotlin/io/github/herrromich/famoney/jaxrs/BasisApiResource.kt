package io.github.herrromich.famoney.jaxrs

import jakarta.servlet.http.HttpServletResponse
import jakarta.ws.rs.core.Context
import jakarta.ws.rs.core.UriInfo
import jakarta.ws.rs.sse.Sse
import mu.KotlinLogging

abstract class BasisApiResource: ApiResource {
    protected val logger = KotlinLogging.logger { }

    @Context
    protected lateinit var httpServletResponse: HttpServletResponse

    @Context
    protected lateinit var uriInfo: UriInfo

    @Context
    protected lateinit var sse: Sse
}
