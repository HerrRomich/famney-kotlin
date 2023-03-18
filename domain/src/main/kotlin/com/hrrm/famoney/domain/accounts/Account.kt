package com.hrrm.famoney.domain.accounts

import com.hrrm.famoney.domain.DomainEntity
import java.math.BigDecimal
import java.time.LocalDate
import javax.persistence.*

@Entity
@Table(name = "account")
class Account : DomainEntity() {
    @Column(name = "name")
    lateinit var name: String

    @Column(name = "open_date")
    lateinit var openDate: LocalDate

    @ElementCollection(fetch = FetchType.EAGER)
    @Column(name = "tag")
    @CollectionTable(name = "account_tag", joinColumns = [JoinColumn(name = "account_id")])
    var tags: Set<String> = mutableSetOf()

    @Column(name = "movement_count")
    var movementCount: Int = 0

    @Column(name = "movement_total")
    var movementTotal: BigDecimal = BigDecimal.ZERO
}