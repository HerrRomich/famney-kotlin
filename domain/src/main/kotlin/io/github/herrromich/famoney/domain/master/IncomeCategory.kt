package io.github.herrromich.famoney.domain.master

import jakarta.persistence.*

@Entity
@DiscriminatorValue(IncomeCategory.TYPE)
class IncomeCategory : EntryCategory<IncomeCategory>() {

    companion object {
        const val TYPE = "INCOME"
    }
}