package io.github.herrromich.famoney.jaxrs.converter

import jakarta.ws.rs.ext.ParamConverter
import java.time.LocalDate

class LocalDateParamConverter : ParamConverter<LocalDate> {
    override fun toString(value: LocalDate?) = value?.toString()

    override fun fromString(value: String?) = value?.let(LocalDate::parse)
}
