package io.github.herrromich.famoney.masterdata

import io.github.herrromich.famoney.masterdata.api.MasterDataApi
import io.github.herrromich.famoney.masterdata.api.MasterDataApiResource
import org.springframework.core.io.ResourceLoader
import org.springframework.stereotype.Service

@Service
class MasterDataApiImpl(
    override val resourceLoader: ResourceLoader,
    override val resources: List<MasterDataApiResource>
) : MasterDataApi() {
    override val name = "Master data"
    override val apiPath = "master-data-api"
    override val description = "API Specification regarding master data."
}
