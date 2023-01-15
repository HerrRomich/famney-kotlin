package com.hrrm.famoneys.commons.events

import io.reactivex.Flowable

interface EventBusService {
    fun postEvent(eventName: String?, event: Event?)
    fun <T : Event?> subscribeToEvents(eventName: String?, eventType: Class<T>?): Flowable<T>?
}