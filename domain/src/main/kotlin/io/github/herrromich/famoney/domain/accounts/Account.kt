package io.github.herrromich.famoney.domain.accounts

import io.github.herrromich.famoney.domain.DomainEntity
import jakarta.persistence.*
import java.math.BigDecimal
import java.time.LocalDate

@Entity
@Table(name = "account")
class Account : DomainEntity() {
    @Column(name = "name")
    lateinit var name: String

    @Column(name = "opening_date")
    lateinit var openingDate: LocalDate

    @ElementCollection(fetch = FetchType.EAGER)
    @Column(name = "tag")
    @CollectionTable(name = "account_tag", joinColumns = [JoinColumn(name = "account_id")])
    var tags: Set<String> = mutableSetOf()

    @Column(name = "movement_total")
    var movementTotal: BigDecimal = BigDecimal.ZERO
}
