package com.hrrm.famoney.domain.accounts.movement

import javax.persistence.DiscriminatorValue
import javax.persistence.Entity

@Entity
@DiscriminatorValue(Balance.TYPE)
class Balance : Movement() {
    companion object {
        const val TYPE = "BALANCE"
    }

}