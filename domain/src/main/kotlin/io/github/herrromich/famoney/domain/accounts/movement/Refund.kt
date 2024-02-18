package io.github.herrromich.famoney.domain.accounts.movement

import jakarta.persistence.Column
import jakarta.persistence.DiscriminatorValue
import jakarta.persistence.Entity

@Entity
@DiscriminatorValue(Refund.TYPE)
open class Refund : Movement() {
    @Column(name = "category_id")
    var categoryId: Int = 0

    @Column(name = "comments")
    var comments: String? = null

    companion object {
        const val TYPE = "REFUND"
    }
}