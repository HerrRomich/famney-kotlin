package io.github.herrromich.famoney.jaxrs

import io.swagger.v3.parser.OpenAPIV3Parser
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration

@Configuration
class JaxRsConfiguration {
    @Bean
    fun openApiParser() = OpenAPIV3Parser()
}