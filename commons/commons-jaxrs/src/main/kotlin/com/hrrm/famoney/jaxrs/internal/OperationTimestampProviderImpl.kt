package com.hrrm.famoney.jaxrs.internal

import com.hrrm.famoney.jaxrs.OperationTimestampProvider
import mu.KotlinLogging
import org.springframework.stereotype.Component
import java.time.LocalDateTime
import java.util.*

@Component
class OperationTimestampProviderImpl(
) : OperationTimestampProvider {
    private val logger = KotlinLogging.logger { }
    private val operationTimestampHolder = ThreadLocal<LocalDateTime>()

    override fun setTimestamp() {
        logger.debug { "Setting operation timestamp to current database timestamp." }
        val operationTimestamp = LocalDateTime.now()
        logger.trace(
            "Setting operation timestamp to current database timestamp: {}.",
            operationTimestamp
        )
        operationTimestampHolder.set(operationTimestamp)
        logger.debug {
            "Successfully set operation timestamp to current database timestamp."
        }
        logger.trace { "Successfully set operation timestamp to current database timestamp: $operationTimestamp." }
    }

    override val timestamp: LocalDateTime
        get() {
            logger.debug("Getting current operation timestamp.")
            val operationTimestamp = Optional.ofNullable(operationTimestampHolder.get())
                .orElseGet {
                    val serverTimestamp = LocalDateTime.now()
                    logger.warn(
                        "Operation timestamp is not set. Getting current server timestamp: {}",
                        serverTimestamp
                    )
                    serverTimestamp
                }
            logger.debug("Got current operation timestamp.")
            logger.trace("Got current operation timestamp: {}.", operationTimestamp)
            return operationTimestamp
        }
}