package com.hrrm.famoney.accounts.movement

import com.hrrm.famoney.accounts.Account
import com.hrrm.famoney.accounts.AccountsDomainEntity
import java.math.BigDecimal
import java.time.LocalDate
import javax.persistence.*

@Entity
@Inheritance(strategy = InheritanceType.SINGLE_TABLE)
@Table(schema = AccountsDomainEntity.SCHEMA_NAME, name = "movement")
@DiscriminatorColumn(name = "type")
abstract class Movement : AccountsDomainEntity() {
    @ManyToOne
    @JoinColumn(name = "account_id")
    lateinit var account: Account

    @Column(name = "date")
    lateinit var date: LocalDate

    @Column(name = "pos")
    var position: Int = 0

    @Column(name = "booking_date")
    var bookingDate: LocalDate? = null

    @Column(name = "budget_period")
    var budgetPeriod: LocalDate? = null

    @Column(name = "amount")
    var amount: BigDecimal = BigDecimal.ZERO

    @Column(name = "total")
    var total: BigDecimal = BigDecimal.ZERO

    companion object {
        const val FIND_MOVEMENTS_WITH_PAGINATION =
            "com.hrrm.famoney.domain.accounts.movement.AccountMovement#finMovenetsWithPagination"
        const val ACCOUNT_ID_PARAMETER_NAME = "accountId"
        const val MOVEMENT_DATE_PARAMETER_NAME = "movementDateId"
    }
}