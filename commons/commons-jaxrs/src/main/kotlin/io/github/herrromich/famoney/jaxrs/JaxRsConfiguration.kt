package io.github.herrromich.famoney.jaxrs

import io.github.herrromich.famoney.jaxrs.converter.LocalDateParamConverter
import io.swagger.v3.parser.OpenAPIV3Parser
import jakarta.ws.rs.ext.ParamConverter
import jakarta.ws.rs.ext.ParamConverterProvider
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import java.lang.reflect.Type
import java.time.LocalDate

@Configuration
class JaxRsConfiguration {
    @Bean
    fun openApiParser() = OpenAPIV3Parser()

    @Bean
    fun paramConverterProvider() = object: ParamConverterProvider {
        override fun <T : Any?> getConverter(
            rawType: Class<T>?,
            genericType: Type?,
            annotations: Array<out Annotation>?
        ): ParamConverter<T>? {
            return when(rawType) {
                LocalDate::class.java -> LocalDateParamConverter() as ParamConverter<T>
                else -> null
            }
        }
    }

}
