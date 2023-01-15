package com.hrrm.famoney.accounts.migration.v01;

import java.time.LocalDate;
import java.util.List;

import org.immutables.value.Value;
import org.jetbrains.annotations.NotNull;

@Value.Immutable
public interface AccountData {

    @NotNull
    int getBudgetId();

    @NotNull
    String getName();

    @NotNull
    LocalDate getOpenDate();

    @NotNull
    List<String> getTags();

}
