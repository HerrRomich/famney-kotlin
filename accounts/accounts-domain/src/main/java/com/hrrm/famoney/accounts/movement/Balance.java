package com.hrrm.famoney.accounts.movement;

import lombok.*;
import lombok.experimental.Accessors;

import javax.persistence.DiscriminatorValue;
import javax.persistence.Entity;

@Entity
@DiscriminatorValue("balance")
@Accessors(chain = true)
@Getter
@Setter
@ToString(callSuper = true)
@EqualsAndHashCode(
        onlyExplicitlyIncluded = true,
        callSuper = true
)
public class Balance extends Movement {
}
