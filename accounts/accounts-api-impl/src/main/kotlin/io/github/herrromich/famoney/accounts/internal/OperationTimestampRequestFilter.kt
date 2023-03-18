package io.github.herrromich.famoney.accounts.internal

import io.github.herrromich.famoney.jaxrs.OperationTimestampProvider
import org.springframework.stereotype.Component
import jakarta.ws.rs.container.ContainerRequestContext
import jakarta.ws.rs.container.ContainerRequestFilter

@Component
class OperationTimestampRequestFilter : ContainerRequestFilter {
    private lateinit  var operationTimestampProvider: OperationTimestampProvider
    override fun filter(requestContext: ContainerRequestContext) {
        operationTimestampProvider.setTimestamp()
    }
}