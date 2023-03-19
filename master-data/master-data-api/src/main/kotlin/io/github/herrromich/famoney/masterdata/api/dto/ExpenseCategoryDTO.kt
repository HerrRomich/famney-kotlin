package io.github.herrromich.famoney.masterdata.api.dto

import com.fasterxml.jackson.annotation.JsonTypeName
import io.github.herrromich.famoney.jaxrs.IdDTO
import io.swagger.v3.oas.annotations.extensions.Extension
import io.swagger.v3.oas.annotations.extensions.ExtensionProperty
import io.swagger.v3.oas.annotations.media.Schema

@Schema(
    name = "ExpenseCategory",
    allOf = [EntryCategoryDTO::class],
    extensions = [Extension(properties = [ExtensionProperty(name = "x-discriminator-value", value = "expense")])]
)
@JsonTypeName("expense")
data class ExpenseCategoryDTO(
    override val id: Int,
    override val name: String,

    @get:Schema(name = "children", hidden = false)
    override val children: List<ExpenseCategoryDTO>,
) : IdDTO, EntryCategoryDTO<ExpenseCategoryDTO>
