package io.github.herrromich.famoney.masterdata.internal

import io.github.herrromich.famoney.domain.master.EntryCategory
import io.github.herrromich.famoney.domain.master.ExpenseCategoryRepository
import io.github.herrromich.famoney.domain.master.IncomeCategoryRepository
import io.github.herrromich.famoney.masterdata.api.dto.EntryCategoriesDTO
import io.github.herrromich.famoney.masterdata.api.dto.EntryCategoryDTO
import io.github.herrromich.famoney.masterdata.api.dto.ExpenseCategoryDTO
import io.github.herrromich.famoney.masterdata.api.dto.IncomeCategoryDTO
import io.github.herrromich.famoney.masterdata.api.resources.EntryCategoriesApi
import mu.KotlinLogging
import org.springframework.stereotype.Component

@Component
class EntryCategoriesApiImpl(
    private val incomeCategoryRepository: IncomeCategoryRepository,
    private val expenseCategoryRepository: ExpenseCategoryRepository
) : EntryCategoriesApi {
    private val logger = KotlinLogging.logger { }

    override fun getEntryCategories(): EntryCategoriesDTO {
        logger.debug("Getting the hierarchical structure of all entry categories.")
        val expenseCategories = expenseCategoryRepository.findAll()
        val incomeCategories = incomeCategoryRepository.findAll()
        val entryCategoriesDTO = EntryCategoriesDTO(
            expenses =
            mapEntryCategoriesToDTO(expenseCategoryRepository.getTopLevelCategories())
            { entry, children ->
                ExpenseCategoryDTO(
                    id = entry.id!!,
                    name = entry.name,
                    children = children
                )
            },
            incomes =
            mapEntryCategoriesToDTO(incomeCategoryRepository.getTopLevelCategories())
            { entry, children ->
                IncomeCategoryDTO(
                    id = entry.id!!,
                    name = entry.name,
                    children = children
                )
            },
        )
        logger.debug { "Found ${expenseCategories.size} expense categories and ${incomeCategories.size} income categories." }
        logger.trace {
            """Found ${expenseCategories.size} expense categories: $expenseCategories 
                    |and ${incomeCategories.size} income categories $incomeCategories.""".trimMargin()
        }
        return entryCategoriesDTO
    }

    private fun <T : EntryCategory<T>, P : EntryCategoryDTO<P>> mapEntryCategoriesToDTO(
        entryCategories: List<T>, categoryDtoProvider: (entry: T, children: List<P>) -> P
    ): List<P> = entryCategories.asSequence()
        .map {
            categoryDtoProvider(it, mapEntryCategoriesToDTO(it.children, categoryDtoProvider))
        }
        .sortedBy { it.name }
        .toList()

    override fun addEntryCategory(entryCategoryDto: EntryCategoryDTO<*>) = TODO()

    override fun <T : EntryCategoryDTO<*>> changeEntryCategory(id: Int, entryCategoryDto: T) = TODO()

    override fun deleteEntryCategory(id: Int) = TODO()
}