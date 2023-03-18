package com.hrrm.famoney.domain.accounts.movement

import javax.persistence.Column
import javax.persistence.DiscriminatorValue
import javax.persistence.Entity

@Entity
@DiscriminatorValue(Transfer.TYPE)
class Transfer : Movement() {
    @Column(name = "opposit_account_id")
    var oppositAccountId: Int = 0

    @Column(name = "comments")
    var comments: String? = null

    companion object {
        const val TYPE = "TRANSFER"
    }
}