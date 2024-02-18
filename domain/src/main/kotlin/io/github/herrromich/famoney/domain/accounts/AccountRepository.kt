package io.github.herrromich.famoney.domain.accounts

import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Query
import org.springframework.stereotype.Repository

@Repository
interface AccountRepository : JpaRepository<Account, Int> {

    @Query(
        nativeQuery = true,
        value = """
select distinct t.tag
 from tag t
""")
    fun findDistinctTags(): List<String>

}
