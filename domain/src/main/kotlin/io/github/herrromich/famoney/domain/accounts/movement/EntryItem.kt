package io.github.herrromich.famoney.domain.accounts.movement

import jakarta.persistence.Column
import jakarta.persistence.Embeddable
import java.math.BigDecimal

@Embeddable
open class EntryItem {
    @Column(name = "pos")
    var position: Int = 0

    @Column(name = "category_id")
    var categoryId: Int = 0

    @Column(name = "amount")
    var amount: BigDecimal = BigDecimal.ZERO

    @Column(name = "comments")
    var comments: String? = null
}
