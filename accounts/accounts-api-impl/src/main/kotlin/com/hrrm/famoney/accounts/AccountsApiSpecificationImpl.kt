package com.hrrm.famoney.accounts

import com.hrrm.famoney.accounts.api.AccountsApiSpecification
import org.springframework.core.io.ResourceLoader
import org.springframework.stereotype.Service

@Service
class AccountsApiSpecificationImpl(override val resourceLoader: ResourceLoader) : AccountsApiSpecification() {
    override val name = "Accounts"
    override val apiPath = "accounts-api"
    override val description = "API Specification regarding Accounts."
}