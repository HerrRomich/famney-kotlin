package io.github.herrromich.famoney.jaxrs

import java.time.LocalDateTime

interface OperationTimestampProvider {
    val timestamp: LocalDateTime
    fun setTimestamp()
}