package io.github.herrromich.famoney.web.swagger.apis

import com.fasterxml.jackson.core.JsonProcessingException
import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.databind.SerializationFeature
import io.github.herrromich.famoney.jaxrs.ApiSpecification
import io.swagger.v3.core.util.Json
import io.swagger.v3.oas.models.OpenAPI
import io.swagger.v3.oas.models.servers.Server
import io.swagger.v3.parser.OpenAPIV3Parser
import jakarta.servlet.http.HttpServletRequest
import mu.KotlinLogging
import org.apache.commons.io.IOUtils
import org.springframework.http.HttpStatus
import org.springframework.http.MediaType
import org.springframework.http.ResponseEntity
import org.springframework.http.server.ServletServerHttpRequest
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController
import org.springframework.web.util.UriComponentsBuilder
import java.io.IOException
import java.nio.charset.StandardCharsets

@RestController
@RequestMapping(ApisController.CONTROLLER_SPEC)
class ApisController(
    val openApiParser: OpenAPIV3Parser,
    val objectMapper: ObjectMapper,
    specifications: List<ApiSpecification>
) {
    private val logger = KotlinLogging.logger { }

    private val specifications: Map<String, Pair<ApiSpecification, OpenAPI>>

    init {
        this.specifications = specifications.map { it.apiPath to (it to parseSwagger(it)) }.toMap()
    }

    private fun parseSwagger(specification: ApiSpecification): OpenAPI {
        try {
            specification.specificationResource
                .inputStream.use { specStream ->
                    val specString = IOUtils.toString(specStream, StandardCharsets.UTF_8)
                    return openApiParser.readContents(specString)
                        .openAPI
                }
        } catch (e: IOException) {
            logger.error(e) { "Es ist ein Fehler in abarbeitung von OpenApi Spezifikation: \"${specification.name}\"" }
            throw e
        }
    }

    @GetMapping(path = ["apis.js"], produces = [MediaType.APPLICATION_JSON_VALUE])
    fun getApis(request: HttpServletRequest): String {
        logger.debug("APIS specifications are requested as JS.")
        val api = specifications.values
            .map { (spec) ->
                Api(
                    url = "${request.contextPath}$CONTROLLER_SPEC/${spec.apiPath}.json",
                    name = spec.description
                )
            }
        val specificationsScript = """
var apis =
${objectMapper.writeValueAsString(api)};"""
        logger.debug("APIS specifications prepared as JSS.")
        return specificationsScript
    }

    @GetMapping(path = ["{api}.json"], produces = [MediaType.APPLICATION_JSON_VALUE])
    fun getApi(@PathVariable api: String, request: HttpServletRequest): ResponseEntity<String> {
        logger.debug { "Requesting OpenAPI Spezifikation für REST-Schnittstelle \"$api\"." }
        return try {
            if (specifications.containsKey(api)) {
                val specificationTuple = specifications.getValue(api)
                val openApiSpec = specificationTuple.second
                val url = UriComponentsBuilder.fromHttpRequest(
                    ServletServerHttpRequest(request)
                ).replacePath(
                    "apis/${specificationTuple.first.apiPath}"
                )
                    .build()
                openApiSpec.setServers(java.util.List.of(Server().url(url.toString())))
                val spec = Json.mapper()
                    .enable(SerializationFeature.INDENT_OUTPUT)
                    .writeValueAsString(openApiSpec)
                logger.debug { "OpenAPI specification for REST \"$api\" is sent" }
                ResponseEntity.ok(spec)
            } else {
                val errorMessage: String = "Unbenkante REST-Schnittstelle: \"$api\""
                logger.warn(errorMessage)
                ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(errorMessage)
            }
        } catch (e: JsonProcessingException) {
            val message = "OpenAPI specification cannot be sent."
            logger.error(message, e)
            ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(message)
        }
    }

    private data class Api(
        val url: String,
        val name: String
    )

    companion object {
        const val CONTROLLER_SPEC = "/apis/spec"
    }
}
