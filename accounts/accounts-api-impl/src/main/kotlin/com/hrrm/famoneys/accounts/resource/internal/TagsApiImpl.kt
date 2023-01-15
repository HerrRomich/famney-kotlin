package com.hrrm.famoneys.accounts.resource.internal

import com.hrrm.famoneys.accounts.api.resource.TagsApi
import lombok.extern.log4j.Log4j2

@Log4j2
@RequiredArgsConstructor
@Service
@Hidden
class TagsApiImpl : TagsApi {
    private val accountRepository: AccountRepositoryCust? = null
    val allAccountTags: List<String>
        get() {
            TagsApiImpl.log.debug("Getting account tags.")
            val allTags: Unit = accountRepository.findAllTags()
            TagsApiImpl.log.debug("Successfully got account tags.")
            TagsApiImpl.log.trace("Sucessfully got {} account tags.", allTags.size())
            return allTags
        }
}