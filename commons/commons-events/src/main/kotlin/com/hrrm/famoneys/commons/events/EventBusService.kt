package com.hrrm.famoneys.commons.events

import io.reactivex.Flowable

interface EventBusService {
    fun <T: Event> postEvent(eventName: String, event: T)
    fun <T : Event> subscribeToEvents(eventName: String, eventType: Class<T>): Flowable<T>
}