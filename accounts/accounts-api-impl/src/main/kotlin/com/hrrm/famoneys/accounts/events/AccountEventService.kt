package com.hrrm.famoneys.accounts.events

interface AccountEventService {
    data class EventData(val accountId: Int)

    fun putChangeEvent(accountId: Int)

    companion object {
        const val ACCOUNTS_CHANGE_TOPIC = "com/hrrm/famoney/event/accounts"
    }
}