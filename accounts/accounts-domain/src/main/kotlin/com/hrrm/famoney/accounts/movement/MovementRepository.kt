package com.hrrm.famoney.accounts.movement

import com.hrrm.famoney.accounts.Account
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Modifying
import org.springframework.data.jpa.repository.Query
import org.springframework.data.repository.query.Param
import org.springframework.stereotype.Repository
import java.math.BigDecimal
import java.time.LocalDate

@Repository
interface MovementRepository : JpaRepository<Movement, Int> {
    fun countByAccount(account: Account): Long

    fun findFirstByAccountAndPositionLessThanOrderByPosition(account: Account, position: Int): Movement?

    @Modifying
    @Query(
        """update Movement m set m." 
             |   set m.total = m.total + :movement.amount
             |     , m.position = m.position + 1 
             | where m.account = :movement.account 
             |   and m.position >= :movement.position"""
    )
    fun adjustMovementPositionsAndSumsByAccountAfterPosition(@Param("movement") movement: Movement)


    @Query(
        """select count from Movement m 
             | where m.account = :movement.account
             |   and m.date = :movement.date
             |   and mp.osition < :movement.position"""
    )
    fun findNextMovementByAccountIdBeforePosition(
        @Param("movement") movement: Movement
    ): Movement?

    fun getMovementsByAccountIdWithOffsetAndLimitOrderedByPos(accountId: Int, offset: Int?, limit: Int?): List<Movement>

    @Query(
        """select count from Movement m 
             | where m.account = :movement.account
             |   and m.date = :movementDate"""
    )
    fun getLastPositionByAccountOnDate(
        @Param("movement") movement: Movement,
        @Param("movementDate") movementDate: LocalDate
    ): Int

    @Modifying
    @Query("""update Movement m set m." 
             |   set m.total = m.total - :movement.amount
             |     , m.position = m.position - 1 
             | where m.account = :movement.account 
             |   and m.position > :movement.position""")
    fun rollbackMovementPositionsAndSumsByAccountAfterPosition(
        @Param("movement") movement: Movement
    )

}
