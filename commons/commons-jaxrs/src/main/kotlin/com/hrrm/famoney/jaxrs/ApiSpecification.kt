package com.hrrm.famoney.jaxrs

import org.springframework.core.io.Resource

interface ApiSpecification {
    val name: String
    val apiPath: String
    val description: String
    val specificationResource: Resource
}