package io.github.herrromich.famoney.domain.master

import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.repository.NoRepositoryBean

@NoRepositoryBean
interface EntryCategoryRepository<T : EntryCategory<T>> : JpaRepository<T, Int> {
    fun getTopLevelCategories(): List<T>
}
