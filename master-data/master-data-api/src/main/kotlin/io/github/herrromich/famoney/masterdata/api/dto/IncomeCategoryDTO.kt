package io.github.herrromich.famoney.masterdata.api.dto

import com.fasterxml.jackson.annotation.JsonTypeName
import io.swagger.v3.oas.annotations.extensions.Extension
import io.swagger.v3.oas.annotations.extensions.ExtensionProperty
import io.swagger.v3.oas.annotations.media.Schema

@Schema(
    name = "IncomeCategory",
    allOf = [EntryCategoryDTO::class],
    extensions = [Extension(
        properties = [ExtensionProperty(
            name = "x-discriminator-value",
            value = IncomeCategoryDTO.TYPE_NAME
        )]
    )]
)
@JsonTypeName(IncomeCategoryDTO.TYPE_NAME)
data class IncomeCategoryDTO(
    override val id: Int,
    override val name: String,

    @get:Schema(name = "children", hidden = false)
    override val children: List<IncomeCategoryDTO>,
) : EntryCategoryDTO<IncomeCategoryDTO> {
    companion object {
        const val TYPE_NAME = "INCOME"
    }

}
