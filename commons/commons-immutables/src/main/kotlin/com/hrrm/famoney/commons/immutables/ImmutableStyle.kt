package com.hrrm.famoney.commons.immutables

import com.fasterxml.jackson.databind.annotation.JsonSerialize
import org.immutables.value.Value
import org.jetbrains.annotations.NotNull

@JsonSerialize
@Value.Style(
    typeImmutable = "*Impl",
    overshadowImplementation = true,
    get = ["get*", "is*"],
    passAnnotations = [NotNull::class]
)
annotation class ImmutableStyle 