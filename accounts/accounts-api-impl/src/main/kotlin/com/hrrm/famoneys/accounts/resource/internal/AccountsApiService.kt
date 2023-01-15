package com.hrrm.famoneys.accounts.resource.internal

import com.hrrm.famoney.accounts.Account
import com.hrrm.famoneys.accounts.internalexceptions.AccountsApiError

interface AccountsApiService {
    fun getAccountByIdOrThrowNotFound(accountId: Int, error: AccountsApiError): Account
}
