package io.github.herrromich.famoney.masterdata.api.dto

import com.fasterxml.jackson.annotation.JsonSubTypes
import com.fasterxml.jackson.annotation.JsonTypeInfo
import io.github.herrromich.famoney.jaxrs.IdDTO
import io.swagger.v3.oas.annotations.media.DiscriminatorMapping
import io.swagger.v3.oas.annotations.media.Schema

@Schema(
    name = "EntryCategory",
    discriminatorProperty = "type",
    discriminatorMapping = [
        DiscriminatorMapping(value = ExpenseCategoryDTO.TYPE_NAME, schema = ExpenseCategoryDTO::class),
        DiscriminatorMapping(value = IncomeCategoryDTO.TYPE_NAME, schema = IncomeCategoryDTO::class)
    ]
)
@JsonTypeInfo(property = "type", use = JsonTypeInfo.Id.NAME, include = JsonTypeInfo.As.PROPERTY)
@JsonSubTypes(
    JsonSubTypes.Type(name = ExpenseCategoryDTO.TYPE_NAME, value = ExpenseCategoryDTO::class),
    JsonSubTypes.Type(name = IncomeCategoryDTO.TYPE_NAME, value = IncomeCategoryDTO::class)
)
interface EntryCategoryDTO<T : EntryCategoryDTO<T>> : IdDTO {
    @get:Schema(required = true)
    val name: String

    @get:Schema(hidden = true)
    val children: List<T>
}
