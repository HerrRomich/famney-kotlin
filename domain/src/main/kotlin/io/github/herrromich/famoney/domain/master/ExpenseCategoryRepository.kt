package io.github.herrromich.famoney.domain.master

import org.springframework.data.jpa.repository.Query
import org.springframework.stereotype.Repository

@Repository
interface ExpenseCategoryRepository : EntryCategoryRepository<ExpenseCategory> {
    @Query("select c from ExpenseCategory c where c.parent is null")
    override fun getTopLevelCategories(): List<ExpenseCategory>
}
