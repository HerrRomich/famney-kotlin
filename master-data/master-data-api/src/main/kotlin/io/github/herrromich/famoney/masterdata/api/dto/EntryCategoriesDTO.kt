package io.github.herrromich.famoney.masterdata.api.dto

import io.swagger.v3.oas.annotations.media.Schema

@Schema(name = "EntryCategories")
data class EntryCategoriesDTO(
    @get:Schema(required = true)
    val incomes: List<IncomeCategoryDTO>,

    @get:Schema(required = true)
    val expenses: List<ExpenseCategoryDTO>,
)
