package io.github.herrromich.famoney.domain.accounts.movement

import java.math.BigDecimal
import jakarta.persistence.Column
import jakarta.persistence.Embeddable

@Embeddable
class EntryItem {
    @Column(name = "pos")
    var position: Int = 0

    @Column(name = "category_id")
    var categoryId: Int = 0

    @Column(name = "amount")
    var amount: BigDecimal = BigDecimal.ZERO

    @Column(name = "comments")
    var comments: String? = null
}