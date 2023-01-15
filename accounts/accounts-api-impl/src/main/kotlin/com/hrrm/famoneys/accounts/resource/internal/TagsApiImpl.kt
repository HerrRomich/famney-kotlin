package com.hrrm.famoneys.accounts.resource.internal

import com.hrrm.famoney.accounts.AccountRepository
import com.hrrm.famoney.accounts.api.resources.TagsApi
import io.swagger.v3.oas.annotations.Hidden
import mu.KotlinLogging
import org.springframework.stereotype.Service

@Service
@Hidden
class TagsApiImpl(private val accountRepository: AccountRepository) : TagsApi {
    private val logger = KotlinLogging.logger {  }

    override fun allAccountTags(): List<String> {
            logger.debug { "Getting account tags." }
            val allTags = accountRepository.findDistinctTags()
            logger.debug { "Successfully got account tags." }
            logger.trace { "Sucessfully got ${allTags.size} account tags." }
            return allTags
        }
}