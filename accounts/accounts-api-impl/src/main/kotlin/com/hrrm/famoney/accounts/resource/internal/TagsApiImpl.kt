package com.hrrm.famoney.accounts.resource.internal

import com.hrrm.famoney.accounts.api.AccountsApiResource
import com.hrrm.famoney.accounts.api.resources.TagsApi
import com.hrrm.famoney.domain.accounts.AccountRepository
import io.swagger.v3.oas.annotations.Hidden
import mu.KotlinLogging
import org.springframework.stereotype.Service

@Service
@Hidden
class TagsApiImpl(private val accountRepository: AccountRepository) : TagsApi, AccountsApiResource {
    private val logger = KotlinLogging.logger {  }

    override fun allAccountTags(): List<String> {
            logger.debug { "Getting account tags." }
            val allTags = accountRepository.findDistinctTags().sorted()
            logger.debug { "Successfully got account tags." }
            logger.trace { "Sucessfully got ${allTags.size} account tags." }
            return allTags
        }
}