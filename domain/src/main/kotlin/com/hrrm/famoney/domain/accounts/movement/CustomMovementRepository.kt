package com.hrrm.famoney.domain.accounts.movement

import com.hrrm.famoney.domain.accounts.Account

interface CustomMovementRepository {

    fun getByAccountOrderByDatePosition(account: Account, offset: Int?, limit: Int?): List<Movement>

}
