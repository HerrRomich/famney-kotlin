package com.hrrm.famoney.accounts.api

import com.hrrm.famoney.jaxrs.ApiSpecification
import io.swagger.v3.oas.annotations.OpenAPIDefinition
import io.swagger.v3.oas.annotations.info.Info
import io.swagger.v3.oas.annotations.tags.Tag
import lombok.ToString
import org.springframework.core.io.Resource
import org.springframework.core.io.ResourceLoader

@ToString
@OpenAPIDefinition(
    info = Info(title = "Accounts", version = "1.0.0"),
    tags = [Tag(name = "accounts", description = "Accounts related requests.")]
)
abstract class AccountsApiSpecification : ApiSpecification {
    protected abstract val resourceLoader: ResourceLoader?
    override val specificationResource: Resource
        get() = resourceLoader!!.getResource("classpath:accounts-api.json")
}