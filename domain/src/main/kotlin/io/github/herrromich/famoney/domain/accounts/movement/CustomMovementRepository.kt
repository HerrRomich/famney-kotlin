package io.github.herrromich.famoney.domain.accounts.movement

import io.github.herrromich.famoney.domain.accounts.Account

interface CustomMovementRepository {

    fun getByAccountOrderByDatePosition(account: Account, offset: Int?, limit: Int?): List<Movement>

}
