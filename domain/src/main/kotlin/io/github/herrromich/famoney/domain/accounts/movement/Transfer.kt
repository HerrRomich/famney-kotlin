package io.github.herrromich.famoney.domain.accounts.movement

import jakarta.persistence.Column
import jakarta.persistence.DiscriminatorValue
import jakarta.persistence.Entity

@Entity
@DiscriminatorValue(Transfer.TYPE)
open class Transfer : Movement() {
    @Column(name = "opposite_account_id")
    var oppositeAccountId: Int = 0

    @Column(name = "comments")
    var comments: String? = null

    companion object {
        const val TYPE = "TRANSFER"
    }
}