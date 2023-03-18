package io.github.herrromich.famoney.launcher.config

import org.springframework.context.annotation.Configuration
import org.springframework.core.io.ClassPathResource
import org.springframework.core.io.Resource
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry
import org.springframework.web.servlet.config.annotation.ViewControllerRegistry
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer
import org.springframework.web.servlet.resource.PathResourceResolver
import java.io.IOException

@Configuration
class WebUiConfiguration : WebMvcConfigurer {
    override fun addResourceHandlers(registry: ResourceHandlerRegistry) {
        registry.addResourceHandler(
            "/ui/",
            "/ui/**"
        )
            .addResourceLocations("classpath:/static/web-ui/")
            .resourceChain(true)
            .addResolver(object : PathResourceResolver() {
                override fun getResource(
                    resourcePath: String,
                    location: Resource
                ): Resource {
                    val requestedResource = location.createRelative(resourcePath)
                    return if (requestedResource.exists() && requestedResource.isReadable) requestedResource else ClassPathResource(
                        "/static/web-ui/index.html"
                    )
                }
            })
        registry.addResourceHandler(
            "/spec/",
            "/spec/**"
        )
            .addResourceLocations("classpath:/static/swagger-ui/")
            .resourceChain(true)
            .addResolver(object : PathResourceResolver() {
                @Throws(IOException::class)
                override fun getResource(
                    resourcePath: String,
                    location: Resource
                ): Resource {
                    val requestedResource = location.createRelative(resourcePath)
                    return if (requestedResource.exists() && requestedResource.isReadable) requestedResource else ClassPathResource(
                        "/static/swagger-ui/index.html"
                    )
                }
            })
    }

    override fun addViewControllers(registry: ViewControllerRegistry) {
        registry.addViewController("/ui")
            .setViewName("forward:/web-ui/index.html")
        registry.addViewController("/spec")
            .setViewName("forward:/spec/index.html")
    }
}