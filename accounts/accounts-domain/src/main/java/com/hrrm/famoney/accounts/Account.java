package com.hrrm.famoney.accounts;

import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;
import lombok.experimental.Accessors;

import javax.persistence.*;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Set;

@Entity
@Table(
        schema = AccountsDomainEntity.SCHEMA_NAME,
        name = "account"
)
@Accessors(chain = true)
@Getter
@Setter
@ToString(callSuper = true)
@EqualsAndHashCode(
        onlyExplicitlyIncluded = true,
        callSuper = true
)
public class Account extends AccountsDomainEntity {

    @Column(name = "name")
    private String name;

    @Column(name = "open_date")
    private LocalDateTime openDate;

    @ElementCollection(fetch = FetchType.EAGER)
    @Column(name = "tag")
    @CollectionTable(
            schema = AccountsDomainEntity.SCHEMA_NAME,
            name = "account_tag",
            joinColumns = { @JoinColumn(name = "account_id") }
    )
    private Set<String> tags;

    @Column(name = "movement_count")
    private Integer movementCount;

    @Column(name = "movement_total")
    private BigDecimal movementTotal;

}
