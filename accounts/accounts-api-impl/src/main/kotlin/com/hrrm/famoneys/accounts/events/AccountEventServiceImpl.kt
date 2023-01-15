package com.hrrm.famoneys.accounts.events

import io.reactivex.Observable
import lombok.extern.log4j.Log4j2
import org.springframework.stereotype.Service

@RequiredArgsConstructor
@Log4j2
@Service
class AccountEventServiceImpl : AccountEventService {
    private val eventSubject: Subject<AccountEventService.EventData> = AsyncSubject.create()
    override fun registerEventListener(): Observable<AccountEventService.EventData?> {
        return eventSubject.toSerialized()
    }

    override fun putChangeEvent(accountId: Int?) {
        AccountEventServiceImpl.log.debug("An event for account adding will be put.")
        AccountEventServiceImpl.log.trace(
            "An event for account adding to account id: {} into position: {} will be put.",
            accountId
        )
        eventSubject.onNext(null)
        AccountEventServiceImpl.log.debug("An event for account adding is successfully put.")
        AccountEventServiceImpl.log.trace(
            "An event for account adding to account id: {} into position: {} is successfully put.",
            accountId
        )
    }
}