package com.hrrm.famoney.accounts.movement;

import lombok.*;
import lombok.experimental.Accessors;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import javax.persistence.Column;
import javax.persistence.DiscriminatorValue;
import javax.persistence.Entity;

@Entity
@DiscriminatorValue("refund")
@Accessors(chain = true)
@Getter
@Setter
@ToString(callSuper = true)
@EqualsAndHashCode(
        onlyExplicitlyIncluded = true,
        callSuper = true
)
public class Refund extends Movement {

    @Column(name = "category_id")
    @NotNull
    private Integer categoryId;

    @Column(name = "comments")
    @Nullable
    private String comments;

}
