package io.github.herrromich.famoney.accounts

import io.github.herrromich.famoney.accounts.api.AccountsApi
import io.github.herrromich.famoney.accounts.api.AccountsApiResource
import org.springframework.core.io.ResourceLoader
import org.springframework.stereotype.Service

@Service
class AccountsApiImpl(
    override val resourceLoader: ResourceLoader,
    override val resources: List<AccountsApiResource>
) : AccountsApi() {
    override val name = "Accounts"
    override val apiPath = "accounts-api"
    override val description = "API Specification regarding Accounts."
}
