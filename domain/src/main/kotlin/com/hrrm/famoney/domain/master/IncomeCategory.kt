package com.hrrm.famoney.domain.master

import javax.persistence.*

@Entity
@DiscriminatorValue(IncomeCategory.TYPE)
class IncomeCategory : EntryCategory<IncomeCategory>() {

    companion object {
        const val TYPE = "INCOME"
    }
}