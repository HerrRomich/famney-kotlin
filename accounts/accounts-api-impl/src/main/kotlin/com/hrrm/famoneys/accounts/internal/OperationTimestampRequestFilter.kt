package com.hrrm.famoneys.accounts.internal

import com.hrrm.famoneys.jaxrs.OperationTimestampProvider
import org.springframework.stereotype.Component

@RequiredArgsConstructor
@Component
class OperationTimestampRequestFilter : ContainerRequestFilter {
    private val operationTimestampProvider: OperationTimestampProvider? = null
    @Throws(IOException::class)
    override fun filter(requestContext: ContainerRequestContext) {
        operationTimestampProvider.setTimestamp()
    }
}