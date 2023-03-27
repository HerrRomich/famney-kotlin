package io.github.herrromich.famoney.accounts.api

import io.github.herrromich.famoney.jaxrs.Api
import io.swagger.v3.oas.annotations.OpenAPIDefinition
import io.swagger.v3.oas.annotations.info.Info
import io.swagger.v3.oas.annotations.tags.Tag
import org.springframework.core.io.Resource
import org.springframework.core.io.ResourceLoader

@OpenAPIDefinition(
    info = Info(title = "Accounts", version = "1.0.0"),
    tags = [Tag(name = "accounts", description = "Accounts related requests.")]
)
abstract class AccountsApi : Api {
    protected abstract val resourceLoader: ResourceLoader
    override val specificationResource: Resource
        get() = resourceLoader.getResource("classpath:accounts-api.json")
}
