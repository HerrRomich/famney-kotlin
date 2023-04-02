package io.github.herrromich.famoney.domain.accounts.movement

import io.github.herrromich.famoney.domain.accounts.Account
import jakarta.persistence.EntityManager
import org.springframework.beans.factory.annotation.Autowired
import java.time.LocalDate

class CustomMovementRepositoryImpl(private @Autowired val entityManager: EntityManager) : CustomMovementRepository {
    override fun getByAccountAndDateRangeOrderByDatePosition(
        account: Account,
        fromDate: LocalDate?,
        toDate: LocalDate?,
        offset: Int?,
        limit: Int?
    ): MutableList<Movement> {
        val fromDateCondition = if (fromDate != null) " and m.date >= :fromDate" else ""
        val toDateCondition = if (toDate != null) " m.date <= :toDate" else ""
        return entityManager.createQuery(
            "select m from Movement m where m.account = :account" +
                    fromDateCondition +
                    toDateCondition +
                    " order by m.date desc, m.position desc",
            Movement::class.java
        ).apply {
            setParameter("account", account)
            if (fromDate != null) {
                setParameter("fromDate", fromDate)
            }
            if (toDate != null) {
                setParameter("toDate", toDate)
            }
            firstResult = offset ?: 0
            maxResults = limit ?: 100
        }.resultList
    }

    override fun getCountByAccountAndDateRange(
        account: Account,
        fromDate: LocalDate?,
        toDate: LocalDate?
    ): Int {
        val fromDateCondition = if (fromDate != null) " and m.date >= :fromDate" else ""
        val toDateCondition = if (toDate != null) " m.date <= :toDate" else ""
        return entityManager.createQuery(
            "select count(m) from Movement m where m.account = :account" +
                    fromDateCondition +
                    toDateCondition,
            Int::class.java
        )
            .apply {
                setParameter("account", account)
                if (fromDate != null) {
                    setParameter("fromDate", fromDate)
                }
                if (toDate != null) {
                    setParameter("toDate", toDate)
                }
            }.singleResult
    }
}
