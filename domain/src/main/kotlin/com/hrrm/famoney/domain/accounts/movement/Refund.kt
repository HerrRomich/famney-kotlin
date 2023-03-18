package com.hrrm.famoney.domain.accounts.movement

import javax.persistence.Column
import javax.persistence.DiscriminatorValue
import javax.persistence.Entity

@Entity
@DiscriminatorValue(Refund.TYPE)
class Refund : Movement() {
    @Column(name = "category_id")
    var categoryId: Int = 0

    @Column(name = "comments")
    var comments: String? = null

    companion object {
        const val TYPE = "REFUND"
    }
}