package com.hrrm.famoney.accounts.migration.v02;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.OptionalInt;

import org.immutables.value.Value;

@Value.Immutable
public interface MovementData {

    Integer getAccountId();

    String getType();

    LocalDateTime getDate();

    Optional<LocalDateTime> getBookingDate();

    Optional<LocalDate> getBudgetPeriod();

    BigDecimal getAmount();

    List<EntryItemData> getEntryItems();

    OptionalInt getCategoryId();

    Optional<String> getComments();

    OptionalInt getOppositAccountId();

}
