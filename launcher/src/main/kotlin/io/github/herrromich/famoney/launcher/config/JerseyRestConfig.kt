package io.github.herrromich.famoney.launcher.config

import io.github.herrromich.famoney.accounts.api.AccountsApi
import io.github.herrromich.famoney.jaxrs.ObjectMapperContextResolver
import io.github.herrromich.famoney.masterdata.api.MasterDataApi
import org.glassfish.jersey.server.ResourceConfig
import org.glassfish.jersey.servlet.ServletContainer
import org.springframework.boot.web.servlet.ServletRegistrationBean
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration

@Configuration
class JerseyRestConfig(val objectMapperContextResolver: ObjectMapperContextResolver) {
    @Bean
    fun masterDataApiRegisration(
        spec: MasterDataApi,
    ): ServletRegistrationBean<ServletContainer> {
        val resourcesConfig = ResourceConfig()
        resourcesConfig.register(objectMapperContextResolver)
        spec.resources.forEach(resourcesConfig::registerInstances)
        val api = ServletContainer(resourcesConfig)
        return ServletRegistrationBean(api, "/apis/${spec.apiPath}/*").apply { setName("masterDataApiRegistration") }
    }

    @Bean
    fun accountsApiRegisration(
        spec: AccountsApi,
    ): ServletRegistrationBean<ServletContainer> {
        val resourcesConfig = ResourceConfig()
        resourcesConfig.register(objectMapperContextResolver)
        spec.resources.forEach(resourcesConfig::registerInstances)
        val api = ServletContainer(resourcesConfig)
        return ServletRegistrationBean(api, "/apis/${spec.apiPath}/*").apply { setName("accountsApiRegistration") }
    }}
