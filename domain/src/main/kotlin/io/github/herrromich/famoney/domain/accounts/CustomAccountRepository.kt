package io.github.herrromich.famoney.domain.accounts

interface CustomAccountRepository {
    fun readAllAccounts(): List<Account>;
}
