package io.github.herrromich.famoney.masterdata.api

import io.github.herrromich.famoney.jaxrs.Api
import io.swagger.v3.oas.annotations.OpenAPIDefinition
import io.swagger.v3.oas.annotations.info.Info
import io.swagger.v3.oas.annotations.tags.Tag
import org.springframework.core.io.Resource
import org.springframework.core.io.ResourceLoader

@OpenAPIDefinition(
    info = Info(title = "Master data", version = "1.0.0"),
    tags = [Tag(name = "master-data", description = "Master data requests.")]
)
abstract class MasterDataApi : Api {
    protected abstract val resourceLoader: ResourceLoader
    override val specificationResource: Resource
        get() = resourceLoader.getResource("classpath:master-data-api.json")
}
