package com.hrrm.famoney.accounts.api;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.math.BigDecimal;
import java.time.LocalDate;

public interface MovementDataDTOBuilder<T extends MovementDataDTO> {

    public MovementDataDTOBuilder<T> date(@NotNull LocalDate date);

    public MovementDataDTOBuilder<T> bookingDate(@Nullable LocalDate bookingDate);

    public MovementDataDTOBuilder<T> budgetPeriod(@Nullable  LocalDate budgetPeriod);

    public MovementDataDTOBuilder<T> amount(@NotNull BigDecimal amount);

    public MovementDataDTO build();

}
