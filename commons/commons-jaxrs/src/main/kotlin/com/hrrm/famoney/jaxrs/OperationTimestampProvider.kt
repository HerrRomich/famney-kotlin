package com.hrrm.famoney.jaxrs

import java.time.LocalDateTime

interface OperationTimestampProvider {
    val timestamp: LocalDateTime
    fun setTimestamp()
}