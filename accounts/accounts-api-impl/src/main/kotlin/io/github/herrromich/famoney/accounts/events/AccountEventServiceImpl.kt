package io.github.herrromich.famoney.accounts.events

import io.reactivex.rxjava3.subjects.PublishSubject
import io.reactivex.rxjava3.subjects.Subject
import mu.KotlinLogging
import org.springframework.stereotype.Service

@Service
class AccountEventServiceImpl : io.github.herrromich.famoney.accounts.events.AccountEventService {
    private val logger = KotlinLogging.logger { }

    val eventSubject: Subject<io.github.herrromich.famoney.accounts.events.AccountEventService.EventData> = PublishSubject.create()

    override fun putChangeEvent(accountId: Int) {
        logger.debug { "An event for account adding will be put." }
        logger.trace { "An event for account adding to account id: $accountId into position: {} will be put." }
        eventSubject.onNext(
            AccountEventService.EventData(
                accountId
            )
        )
        logger.debug("An event for account adding is successfully put.")
        logger.trace { "An event for account adding to account id: $accountId into position: {} is successfully put." }
    }
}