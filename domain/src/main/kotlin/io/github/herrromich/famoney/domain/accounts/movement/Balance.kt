package io.github.herrromich.famoney.domain.accounts.movement

import jakarta.persistence.DiscriminatorValue
import jakarta.persistence.Entity

@Entity
@DiscriminatorValue(Balance.TYPE)
class Balance : Movement() {
    companion object {
        const val TYPE = "BALANCE"
    }

}