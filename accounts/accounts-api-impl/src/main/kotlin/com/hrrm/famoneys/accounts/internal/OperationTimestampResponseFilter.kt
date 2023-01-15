package com.hrrm.famoneys.accounts.internal

import com.hrrm.famoneys.jaxrs.OperationTimestampProvider
import org.springframework.stereotype.Component

@RequiredArgsConstructor
@Component
class OperationTimestampResponseFilter : ContainerResponseFilter {
    private val operationTimestampProvider: OperationTimestampProvider? = null
    @Throws(IOException::class)
    override fun filter(requestContext: ContainerRequestContext, responseContext: ContainerResponseContext) {
        responseContext.getHeaders()
            .add(
                "fm-operation-timestamp", operationTimestampProvider.getTimestamp()
                    .toString()
            )
    }
}