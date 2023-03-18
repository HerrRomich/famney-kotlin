package com.hrrm.famoney.commons.events

interface InternalEvent {
    val eventName: String
    val event: Event
}