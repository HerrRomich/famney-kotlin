package io.github.herrromich.famoney.accounts.internal

import io.github.herrromich.famoney.jaxrs.OperationTimestampProvider
import org.springframework.stereotype.Component
import jakarta.ws.rs.container.ContainerRequestContext
import jakarta.ws.rs.container.ContainerResponseContext
import jakarta.ws.rs.container.ContainerResponseFilter

@Component
class OperationTimestampResponseFilter : ContainerResponseFilter {
    private lateinit var operationTimestampProvider: OperationTimestampProvider
    override fun filter(requestContext: ContainerRequestContext, responseContext: ContainerResponseContext) {
        responseContext.getHeaders()
            .add(
                "fm-operation-timestamp", operationTimestampProvider.timestamp
                    .toString()
            )
    }
}