package com.hrrm.famoneys.accounts.events

import com.hrrm.famoneys.commons.immutables.ImmutableStyle
import org.immutables.value.Value

interface MovementEventService {
    fun registerEventListener(accountId: Int?): Observable<EventData?>
    fun putEvent(eventData: EventData)
    interface EventData {
        val accountId: Int?
        val position: Int?
    }

    @Value.Immutable
    @ImmutableStyle
    interface AddEventData : EventData

    @Value.Immutable
    @ImmutableStyle
    interface ChangeEventData : EventData {
        val positionAfter: Int?
    }

    @Value.Immutable
    @ImmutableStyle
    interface DeleteEventData : EventData
    companion object {
        const val MOVEMENTS_TOPIC = "com/hrrm/famoney/event/accounts/movements"
        const val MOVEMENTS_ADD_TOPIC = (MOVEMENTS_TOPIC
                + "/add")
        const val MOVEMENTS_CHANGE_TOPIC = (MOVEMENTS_TOPIC
                + "/change")
        const val MOVEMENTS_DELETE_TOPIC = (MOVEMENTS_TOPIC
                + "/delete")
    }
}