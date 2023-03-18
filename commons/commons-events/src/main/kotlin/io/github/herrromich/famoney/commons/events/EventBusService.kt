package io.github.herrromich.famoney.commons.events

import io.reactivex.rxjava3.core.Flowable

interface EventBusService {
    fun <T: Event> postEvent(eventName: String, event: T)
    fun <T : Event> subscribeToEvents(eventName: String, eventType: Class<T>): Flowable<T>
}