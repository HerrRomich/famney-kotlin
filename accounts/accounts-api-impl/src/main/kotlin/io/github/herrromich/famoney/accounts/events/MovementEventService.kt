package io.github.herrromich.famoney.accounts.events

interface MovementEventService {
    fun putEvent(eventData: EventData)

    interface EventData {
        val accountId: Int
        val position: Int
    }

    data class AddEventData(override val accountId: Int, override val position: Int) : EventData

    data class ChangeEventData(override val accountId: Int, override val position: Int, val positionAfter: Int) :
        EventData {

    }

    data class DeleteEventData(override val accountId: Int, override val position: Int) : EventData

    companion object {
        const val MOVEMENTS_TOPIC = "io/github/herrromich/famoney/event/accounts/movements"
        const val MOVEMENTS_ADD_TOPIC = (MOVEMENTS_TOPIC
                + "/add")
        const val MOVEMENTS_CHANGE_TOPIC = (MOVEMENTS_TOPIC
                + "/change")
        const val MOVEMENTS_DELETE_TOPIC = (MOVEMENTS_TOPIC
                + "/delete")
    }
}