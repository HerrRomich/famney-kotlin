package com.hrrm.famoney.accounts

import lombok.EqualsAndHashCode
import lombok.Getter
import lombok.Setter
import lombok.ToString
import lombok.experimental.Accessors
import java.math.BigDecimal
import java.time.LocalDate
import java.time.LocalDateTime
import javax.persistence.*
import kotlin.properties.Delegates

@Entity
@Table(schema = AccountsDomainEntity.SCHEMA_NAME, name = "account")
class Account : AccountsDomainEntity() {
    @Column(name = "name")
    lateinit var name: String

    @Column(name = "open_date")
    lateinit var openDate: LocalDate

    @ElementCollection(fetch = FetchType.EAGER)
    @Column(name = "tag")
    @CollectionTable(schema = SCHEMA_NAME, name = "account_tag", joinColumns = [JoinColumn(name = "account_id")])
    var tags: Set<String> = mutableSetOf()

    @get:Column(name = "movement_count")
    var movementCount: Int = 0

    @Column(name = "movement_total")
    var movementTotal: BigDecimal = BigDecimal.ZERO
}