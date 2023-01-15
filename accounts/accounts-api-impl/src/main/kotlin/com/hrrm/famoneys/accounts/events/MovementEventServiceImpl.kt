package com.hrrm.famoneys.accounts.events

import com.hrrm.famoneys.jaxrs.OperationTimestampProvider
import lombok.extern.log4j.Log4j2
import org.springframework.stereotype.Service

@RequiredArgsConstructor
@Log4j2
@Service
class MovementEventServiceImpl : MovementEventService {
    private val operationTimestamProvider: OperationTimestampProvider? = null
    private val eventSubject: Subject<MovementEventService.EventData> = AsyncSubject.create()
    override fun registerEventListener(accountId: Int?): Observable<MovementEventService.EventData?> {
        return eventSubject.filter { event ->
            event.getAccountId()
                .equals(accountId)
        }
    }

    override fun putEvent(eventData: MovementEventService.EventData) {
        if (eventData is AddEventData) {
            putAddEvent(eventData as AddEventData)
        } else if (eventData is ChangeEventData) {
            putChangeEvent(eventData as ChangeEventData)
        } else if (eventData is DeleteEventData) {
            putDeleteEvent(eventData as DeleteEventData)
        }
    }

    private fun putAddEvent(eventData: AddEventData) {
        MovementEventServiceImpl.log.debug("An event for movement adding will be put.")
        MovementEventServiceImpl.log.trace(
            "An event for movement adding to account id: {} into position: {} will be put.", eventData
                .getAccountId(), eventData.getPosition()
        )
        eventSubject.onNext(eventData)
        MovementEventServiceImpl.log.debug("An event for movement adding is successfully put.")
        MovementEventServiceImpl.log.trace(
            "An event for movement adding to account id: {} into position: {} is successfully put.", eventData
                .getAccountId(), eventData.getPosition()
        )
    }

    private fun putChangeEvent(eventData: ChangeEventData) {
        MovementEventServiceImpl.log.debug("An event for movement changing will be put.")
        MovementEventServiceImpl.log.trace(
            "An event for movement changing in account id: {} from position: {} to position: {} will be put.",
            eventData.getAccountId(), eventData.getPosition(), eventData.getPositionAfter()
        )
        eventSubject.onNext(eventData)
        MovementEventServiceImpl.log.debug("An event for movement changing is successfully put.")
        MovementEventServiceImpl.log.trace(
            "An event for movement changing in account id: {} from position: {} to position: {}"
                    + " is successfully put.", eventData.getAccountId(), eventData.getPosition(), eventData
                .getPositionAfter()
        )
    }

    private fun putDeleteEvent(eventData: DeleteEventData) {
        MovementEventServiceImpl.log.debug("An event for movement deletion will be put.")
        MovementEventServiceImpl.log.trace(
            "An event for movement deletion in account id: {} from position: {} will be put.", eventData
                .getAccountId(), eventData.getPosition()
        )
        eventSubject.onNext(eventData)
        MovementEventServiceImpl.log.debug("An event for movement deletion is successfully put.")
        MovementEventServiceImpl.log.trace(
            "An event for movement deletion in account id: {} from position: {} is successfully put.", eventData
                .getAccountId(), eventData.getPosition()
        )
    }
}