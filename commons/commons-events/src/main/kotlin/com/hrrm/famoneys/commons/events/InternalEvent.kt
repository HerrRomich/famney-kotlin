package com.hrrm.famoneys.commons.events

interface InternalEvent {
    val eventName: String
    val event: Event
}