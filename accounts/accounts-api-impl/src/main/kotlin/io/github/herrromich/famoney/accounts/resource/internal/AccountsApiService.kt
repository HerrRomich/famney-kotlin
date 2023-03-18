package io.github.herrromich.famoney.accounts.resource.internal

import io.github.herrromich.famoney.accounts.internalexceptions.AccountsApiError
import io.github.herrromich.famoney.domain.accounts.Account

interface AccountsApiService {
    fun getAccountByIdOrThrowNotFound(accountId: Int, error: AccountsApiError): Account
}
