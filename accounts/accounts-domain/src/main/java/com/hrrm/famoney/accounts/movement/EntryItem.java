package com.hrrm.famoney.accounts.movement;

import lombok.*;
import lombok.experimental.Accessors;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import javax.persistence.Column;
import javax.persistence.Embeddable;
import java.math.BigDecimal;

@Embeddable
@Accessors(chain = true)
@Getter
@Setter
@ToString(callSuper = true)
@EqualsAndHashCode(onlyExplicitlyIncluded = true)
public class EntryItem {

    @Column(name = "pos")
    @NotNull
    private Integer position;

    @Column(name = "category_id")
    @NotNull
    private Integer categoryId;

    @Column(name = "amount")
    @NotNull
    private BigDecimal amount;

    @Column(name = "comments")
    @Nullable
    private String comments;

}
