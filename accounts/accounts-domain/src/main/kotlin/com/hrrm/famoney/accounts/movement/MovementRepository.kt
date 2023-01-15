package com.hrrm.famoney.accounts.movement

import com.hrrm.famoney.accounts.Account
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Query
import org.springframework.stereotype.Repository
import java.math.BigDecimal

@Repository
interface MovementRepository : JpaRepository<Movement, Int> {
    fun countByAccount(account: Account): Long

    fun findFirstByAccountAndPositionLessThanOrderByPosition(account: Account, position: Int): Movement?

    @Query("""update Movement m set m.total = m.total + :amount, m.position = m.position + 1 where m.account = :account and m.position >= :positionAfter""")
    fun adjustMovementPositionsAndSumsByAccountAfterPosition(account: Account, positionAfter: Int, amount: BigDecimal)
    /*fun getMovementsByAccountIdWithOffsetAndLimitOrderedByPos(
        account: Account, offset: Int?,
        limit: Int?
    ): List<Movement>

    fun getLastPositionByAccountOnDate(account: Account, date: LocalDate?): Int*/

}
