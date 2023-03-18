package io.github.herrromich.famoney.launcher.config

import io.github.herrromich.famoney.accounts.api.AccountsApiResource
import io.github.herrromich.famoney.accounts.api.AccountsApiSpecification
import io.github.herrromich.famoney.jaxrs.ObjectMapperContextResolver
import org.glassfish.jersey.server.ResourceConfig
import org.glassfish.jersey.servlet.ServletContainer
import org.springframework.boot.web.servlet.ServletRegistrationBean
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration

@Configuration
class JerseyRestConfig {
    @Bean
    fun accountsResourcesRegisration(
        accountsSpec: AccountsApiSpecification,
        accountsApiResources: List<AccountsApiResource>,
        objectMapperContextResolver: ObjectMapperContextResolver
    ): ServletRegistrationBean<ServletContainer> {
        val accountsApiResourcesConfig = ResourceConfig()
        accountsApiResourcesConfig.register(objectMapperContextResolver)
        accountsApiResources.forEach(accountsApiResourcesConfig::registerInstances)
        val accountsApis = ServletContainer(accountsApiResourcesConfig)
        return ServletRegistrationBean<ServletContainer>(accountsApis, "/${accountsSpec.apiPath}/*")
    }
}