package io.github.herrromich.famoney.commons.events

interface InternalEvent {
    val eventName: String
    val event: Event
}