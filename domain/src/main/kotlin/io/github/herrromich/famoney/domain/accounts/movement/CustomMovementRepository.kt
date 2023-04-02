package io.github.herrromich.famoney.domain.accounts.movement

import io.github.herrromich.famoney.domain.accounts.Account
import java.time.LocalDate

interface CustomMovementRepository {

    fun getByAccountAndDateRangeOrderByDatePosition(account: Account, fromDate: LocalDate?, toDate: LocalDate?, offset: Int?, limit: Int?): List<Movement>

    fun getCountByAccountAndDateRange(account: Account, fromDate: LocalDate?, toDate: LocalDate?): Int

}
