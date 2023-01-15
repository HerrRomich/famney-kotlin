package com.hrrm.famoney.accounts.movement;

import com.hrrm.famoney.accounts.Account;
import com.hrrm.famoney.accounts.AccountsDomainEntity;
import lombok.*;
import lombok.experimental.Accessors;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import javax.persistence.*;
import java.math.BigDecimal;
import java.time.LocalDate;

@Entity
@Inheritance(strategy = InheritanceType.SINGLE_TABLE)
@Table(
        schema = AccountsDomainEntity.SCHEMA_NAME,
        name = "movement"
)
@DiscriminatorColumn(name = "type")
@Accessors(chain = true)
@Getter
@Setter
@ToString(callSuper = true)
@EqualsAndHashCode(
        onlyExplicitlyIncluded = true,
        callSuper = true
)
public abstract class Movement extends AccountsDomainEntity {

    public static final String FIND_MOVEMENTS_WITH_PAGINATION = "com.hrrm.famoney.domain.accounts.movement.AccountMovement#finMovenetsWithPagination";
    public static final String ACCOUNT_ID_PARAMETER_NAME = "accountId";
    public static final String MOVEMENT_DATE_PARAMETER_NAME = "movementDateId";

    @ManyToOne
    @JoinColumn(name = "account_id")
    @NotNull
    private Account account;

    @Column(name = "date")
    @NotNull
    private LocalDate date;

    @Column(name = "pos")
    @NotNull
    private Integer position;

    @Column(name = "booking_date")
    @Nullable
    private LocalDate bookingDate;

    @Column(name = "budget_period")
    @Nullable
    private LocalDate budgetPeriod;

    @Column(name = "amount")
    @Nullable
    private BigDecimal amount;

    @Column(name = "total")
    @Nullable
    private BigDecimal total;

}
