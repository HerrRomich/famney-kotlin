package io.github.herrromich.famoney.domain.master

import jakarta.persistence.*

@Entity
@DiscriminatorValue(ExpenseCategory.TYPE)
class ExpenseCategory : EntryCategory<ExpenseCategory>() {
    companion object {
        const val TYPE = "EXPENSE"
    }
}