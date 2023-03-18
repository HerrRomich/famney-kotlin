package com.hrrm.famoney.accounts.api

import com.hrrm.famoney.jaxrs.DTO
import io.swagger.v3.oas.annotations.media.Schema
import java.time.LocalDate

@Schema(name = "AccountData", subTypes = [AccountDTO::class])
interface AccountDataDTO: DTO {
    @get:Schema(required = true)
    val name: String

    @get:Schema(required = true)
    val openDate: LocalDate
    val tags: Set<String>
}