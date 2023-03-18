package io.github.herrromich.famoney.accounts.events

interface AccountEventService {
    data class EventData(val accountId: Int)

    fun putChangeEvent(accountId: Int)

    companion object {
        const val ACCOUNTS_CHANGE_TOPIC = "io/github/herrromich/famoney/event/accounts"
    }
}