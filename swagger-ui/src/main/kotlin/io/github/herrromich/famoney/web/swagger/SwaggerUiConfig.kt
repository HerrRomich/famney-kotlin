package io.github.herrromich.famoney.web.swagger

import org.springframework.context.annotation.Configuration
import org.springframework.core.io.ClassPathResource
import org.springframework.core.io.Resource
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry
import org.springframework.web.servlet.config.annotation.ViewControllerRegistry
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer
import org.springframework.web.servlet.resource.PathResourceResolver

@Configuration
class SwaggerUiConfig : WebMvcConfigurer {
    override fun addResourceHandlers(registry: ResourceHandlerRegistry) {
        registry.addResourceHandler("/apis/spec/", "/apis/spec/**")
            .addResourceLocations("classpath:/static/api/swagger-ui/")
            .resourceChain(true)
            .addResolver(object : PathResourceResolver() {
                override fun getResource(resourcePath: String, location: Resource): Resource {
                    val requestedResource = location.createRelative(resourcePath)
                    return if (requestedResource.exists() && requestedResource.isReadable) requestedResource else ClassPathResource(
                        "/static/api/swagger-ui/index.html"
                    )
                }
            })
    }

    override fun addViewControllers(registry: ViewControllerRegistry) {
        registry.addViewController("/apis/spec")
            .setViewName("redirect:/apis/spec/")
    }
}
