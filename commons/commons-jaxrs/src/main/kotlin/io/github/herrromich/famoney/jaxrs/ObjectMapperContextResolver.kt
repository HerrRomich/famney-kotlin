package io.github.herrromich.famoney.jaxrs

import com.fasterxml.jackson.databind.ObjectMapper
import jakarta.ws.rs.ext.ContextResolver
import jakarta.ws.rs.ext.Provider
import org.springframework.stereotype.Service

@Provider
@Service
class ObjectMapperContextResolver(private val objectMapper: ObjectMapper): ContextResolver<ObjectMapper> {
    override fun getContext(type: Class<*>?): ObjectMapper {
        return objectMapper
    }
}