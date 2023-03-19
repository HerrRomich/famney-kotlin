package io.github.herrromich.famoney.domain.master

import org.springframework.data.jpa.repository.Query
import org.springframework.stereotype.Repository

@Repository
interface IncomeCategoryRepository : EntryCategoryRepository<IncomeCategory> {
    @Query("select c from IncomeCategory c where c.parent is null")
    override fun getTopLevelCategories(): List<IncomeCategory>
}