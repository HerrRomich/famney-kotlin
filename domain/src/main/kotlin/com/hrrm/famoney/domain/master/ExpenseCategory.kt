package com.hrrm.famoney.domain.master

import javax.persistence.*

@Entity
@DiscriminatorValue(ExpenseCategory.TYPE)
class ExpenseCategory : EntryCategory<ExpenseCategory>() {
    companion object {
        const val TYPE = "EXPENSE"
    }
}