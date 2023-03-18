package io.github.herrromich.famoney.domain.accounts

import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Query
import org.springframework.stereotype.Repository

@Repository
interface AccountRepository : JpaRepository<Account, Int> {

    @Query("select distinct t from Account a join a.tags t")
    fun findDistinctTags(): List<String>

    fun findByOrderByName(): List<Account>

}