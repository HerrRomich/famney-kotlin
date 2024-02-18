package io.github.herrromich.famoney.domain.accounts.movement

import io.github.herrromich.famoney.domain.accounts.Account
import jakarta.persistence.EntityManager
import org.springframework.beans.factory.annotation.Autowired
import java.time.LocalDate

class CustomMovementRepositoryImpl(@Autowired private val entityManager: EntityManager) : CustomMovementRepository {
    override fun getByAccountAndDateRangeOrderByDatePosition(
        account: Account,
        fromDate: LocalDate?,
        toDate: LocalDate?,
        offset: Int?,
        limit: Int?
    ): MutableList<Movement> {
        val fromDateCondition = if (fromDate != null) """
   and m.date >= :fromDate""" else ""
        val toDateCondition = if (toDate != null) """
   and m.date <= :toDate""".trimIndent() else ""
        return entityManager.createQuery(
            """
select m from Movement m 
 where m.account = :account$fromDateCondition$toDateCondition
 order by m.date desc, m.position desc
 """,
            Movement::class.java
        ).apply {
            setParameter("account", account)
            fromDate?.let { setParameter("fromDate", it) }
            toDate?.let { setParameter("toDate", it) }
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
                fromDate?.let { setParameter("fromDate", it) }
                toDate?.let { setParameter("toDate", it) }
            }.singleResult
    }
}
