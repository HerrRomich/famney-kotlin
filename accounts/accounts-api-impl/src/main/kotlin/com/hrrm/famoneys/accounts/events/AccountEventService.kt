package com.hrrm.famoneys.accounts.events

import io.reactivex.Observable

interface AccountEventService {
    fun registerEventListener(): Observable<EventData?>
    interface EventData {
        val accountId: Int?
    }

    fun putChangeEvent(accountId: Int?)

    companion object {
        const val ACCOUNTS_CHANGE_TOPIC = "com/hrrm/famoney/event/accounts"
    }
}