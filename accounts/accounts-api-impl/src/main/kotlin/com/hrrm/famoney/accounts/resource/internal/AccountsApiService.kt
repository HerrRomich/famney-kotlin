package com.hrrm.famoney.accounts.resource.internal

import com.hrrm.famoney.accounts.internalexceptions.AccountsApiError
import com.hrrm.famoney.domain.accounts.Account

interface AccountsApiService {
    fun getAccountByIdOrThrowNotFound(accountId: Int, error: AccountsApiError): Account
}
