package io.github.herrromich.famoney.domain.accounts.movement

import io.github.herrromich.famoney.domain.accounts.Account
import jakarta.persistence.EntityManager
import org.springframework.beans.factory.annotation.Autowired

class CustomMovementRepositoryImpl(private @Autowired val entityManager: EntityManager) : CustomMovementRepository {
    override fun getByAccountOrderByDatePosition(account: Account, offset: Int?, limit: Int?) =
        entityManager.createQuery(
            "select m from Movement m where m.account = :account order by m.date, m.position",
            Movement::class.java
        )
            .setParameter("account", account)
            .apply {
                firstResult = offset ?: 0
                maxResults = limit ?: 100
            }
            .resultList
}
