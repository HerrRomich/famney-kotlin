package com.hrrm.famoney.accounts.movement

import javax.persistence.Column
import javax.persistence.DiscriminatorValue
import javax.persistence.Entity

@Entity
@DiscriminatorValue("refund")
class Refund : Movement() {
    @Column(name = "category_id")
    var categoryId: Int = 0

    @Column(name = "comments")
    var comments: String? = null
}