package com.hrrm.famoney.accounts.movement

import javax.persistence.Column
import javax.persistence.DiscriminatorValue
import javax.persistence.Entity

@Entity
@DiscriminatorValue("transfer")
class Transfer : Movement() {
    @Column(name = "opposit_account_id")
    var oppositAccountId: Int = 0
}