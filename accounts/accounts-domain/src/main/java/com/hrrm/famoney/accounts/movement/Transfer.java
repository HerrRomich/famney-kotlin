package com.hrrm.famoney.accounts.movement;

import lombok.*;
import lombok.experimental.Accessors;
import org.jetbrains.annotations.NotNull;

import javax.persistence.Column;
import javax.persistence.DiscriminatorValue;
import javax.persistence.Entity;

@Entity
@DiscriminatorValue("transfer")
@Accessors(chain = true)
@Getter
@Setter
@ToString(callSuper = true)
@EqualsAndHashCode(
        onlyExplicitlyIncluded = true,
        callSuper = true
)
public class Transfer extends Movement {

    @Column(name = "opposit_account_id")
    @NotNull
    private Integer oppositAccountId;

}
