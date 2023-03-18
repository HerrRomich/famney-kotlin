package io.github.herrromich.famoney.accounts.events

import io.reactivex.rxjava3.processors.ReplayProcessor
import mu.KotlinLogging
import org.springframework.stereotype.Service

@Service
class MovementEventServiceImpl : MovementEventService {
    private val logger = KotlinLogging.logger {  }

    private val eventSubject = ReplayProcessor.create<MovementEventService.EventData>()
    override fun putEvent(eventData: MovementEventService.EventData) {
        if (eventData is MovementEventService.AddEventData) {
            putAddEvent(eventData)
        } else if (eventData is MovementEventService.ChangeEventData) {
            putChangeEvent(eventData)
        } else if (eventData is MovementEventService.DeleteEventData) {
            putDeleteEvent(eventData)
        }
    }

    private fun putAddEvent(eventData: MovementEventService.AddEventData) {
        logger.debug{ "An event for movement adding will be put." }
        logger.trace{ "An event for movement adding to account id: ${eventData.accountId} " +
                "into position: ${eventData.position} will be put." }
        eventSubject.onNext(eventData)
        logger.debug("An event for movement adding is successfully put.")
        logger.trace { "An event for movement adding to account id: ${eventData.accountId} " +
                "into position: ${eventData.position} is successfully put."}
    }

    private fun putChangeEvent(eventData: MovementEventService.ChangeEventData) {
        logger.debug { "An event for movement changing will be put." }
        logger.trace { "An event for movement changing in account id: ${eventData.accountId} " +
                "from position: ${eventData.position} to position: ${eventData.positionAfter} will be put." }
        eventSubject.onNext(eventData)
        logger.debug("An event for movement changing is successfully put.")
        logger.trace { "An event for movement changing in account id: ${eventData.accountId} " +
                "from position: ${eventData.position} to position: ${eventData.positionAfter} is successfully put." }
    }

    private fun putDeleteEvent(eventData: MovementEventService.DeleteEventData) {
        logger.debug { "An event for movement deletion will be put." }
        logger.trace { "An event for movement deletion in account id: ${eventData.accountId} from position: ${eventData.position} will be put." }
        eventSubject.onNext(eventData)
        logger.debug { "An event for movement deletion is successfully put." }
        logger.trace { "An event for movement deletion in account id: ${eventData.accountId} from position: ${eventData.position} is successfully put." }
    }
}