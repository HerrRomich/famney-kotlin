package io.github.herrromich.famoney.jaxrs

interface Api:  ApiSpecification {
    val resources: List<ApiResource>
}
