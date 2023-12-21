package io.github.herrromich.famoney.domain.accounts.movement

import io.github.herrromich.famoney.domain.accounts.Account
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Modifying
import org.springframework.data.jpa.repository.Query
import org.springframework.data.repository.query.Param
import org.springframework.stereotype.Repository
import java.time.LocalDate

@Repository
interface MovementRepository : JpaRepository<Movement, Int>, CustomMovementRepository {
    fun countByAccount(account: Account): Long

    fun findFirstByAccountAndPositionLessThanOrderByPosition(account: Account, position: Int): Movement?

    @Modifying
    @Query(
        "" +
                "update Movement m\n" +
                "   set m.total = m.total + :#{#movement.amount}\n" +
                "     , m.position = m.position + 1\n" +
                " where m.account = :#{#movement.account}\n" +
                "   and m.position >= :#{#movement.position}\n"
    )
    fun adjustMovementPositionsAndSumsByAccountAfterPosition(@Param("movement") movement: Movement)


    @Query(
        "" +
                "select m from Movement m\n" +
                " where m.account = :#{#movement.account}\n" +
                "   and m.date = :#{#movement.date}\n" +
                "   and m.position < :#{#movement.position}\n" +
                " order by m.position desc"
    )
    fun findNextMovementByAccountIdBeforePosition(
        @Param("movement") movement: Movement
    ): Movement?

    @Query(
        "" +
                "select count(m) from Movement m\n" +
                " where m.account = :#{#movement.account}\n" +
                "   and m.date = :movementDate"
    )
    fun getLastPositionByAccountOnDate(
        @Param("movement") movement: Movement,
        @Param("movementDate") movementDate: LocalDate
    ): Int

    @Modifying
    @Query(
        "" +
                "update Movement m\n" +
                "   set m.total = m.total - :#{#movement.amount}\n" +
                "     , m.position = m.position - 1\n" +
                " where m.account = :#{#movement.account}\n" +
                "   and m.position > :#{#movement.position}\n"
    )
    fun rollbackMovementPositionsAndSumsByAccountAfterPosition(
        @Param("movement") movement: Movement
    )

}
