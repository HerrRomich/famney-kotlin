package io.github.herrromich.famoney.masterdata

import io.github.herrromich.famoney.masterdata.api.MasterDataApiSpecification
import org.springframework.core.io.ResourceLoader
import org.springframework.stereotype.Service

@Service
class MasterDataApiSpecificationImpl(override val resourceLoader: ResourceLoader) : MasterDataApiSpecification() {
    override val name = "Master data"
    override val apiPath = "master-data-api"
    override val description = "API Specification regarding master data."
}
