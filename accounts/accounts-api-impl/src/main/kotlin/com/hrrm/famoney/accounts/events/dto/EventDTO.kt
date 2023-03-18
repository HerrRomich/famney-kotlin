package com.hrrm.famoney.accounts.events.dto

import io.swagger.v3.oas.annotations.media.Schema
import java.time.LocalDateTime

interface EventDTO {
    @get:Schema(required = true)
    val timestamp: LocalDateTime
}