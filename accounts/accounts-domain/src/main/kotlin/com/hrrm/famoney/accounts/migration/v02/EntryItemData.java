package com.hrrm.famoney.accounts.migration.v02;

import java.math.BigDecimal;

import org.immutables.value.Value;

@Value.Immutable
public interface EntryItemData {

    Integer getCategoryId();

    BigDecimal getAmount();

    String getComments();

}
