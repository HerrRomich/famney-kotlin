package com.hrrm.famoneys.accounts.resource.internal

import com.hrrm.famoneys.accounts.Account

interface AccountsApiService {
    fun getAccountByIdOrThrowNotFound(@NotNull accountId: Int?, error: AccountsApiError?): Account
}