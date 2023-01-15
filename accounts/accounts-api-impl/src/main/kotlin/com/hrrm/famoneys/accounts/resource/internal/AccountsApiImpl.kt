package com.hrrm.famoneys.accounts.resource.internal

import com.hrrm.famoney.accounts.Account
import com.hrrm.famoney.accounts.AccountRepository
import com.hrrm.famoney.accounts.api.*
import com.hrrm.famoney.accounts.api.resources.AccountsApi
import com.hrrm.famoney.jaxrs.ApiException
import com.hrrm.famoneys.accounts.events.AccountEventService
import com.hrrm.famoneys.accounts.internalexceptions.AccountsApiError
import com.hrrm.famoneys.commons.events.EventBusService
import io.swagger.v3.oas.annotations.Hidden
import mu.KotlinLogging
import org.springframework.stereotype.Service
import java.util.*
import javax.servlet.http.HttpServletResponse
import javax.transaction.Transactional
import javax.transaction.Transactional.TxType
import javax.ws.rs.GET
import javax.ws.rs.Produces
import javax.ws.rs.core.Context
import javax.ws.rs.core.HttpHeaders
import javax.ws.rs.core.MediaType
import javax.ws.rs.core.UriInfo
import javax.ws.rs.sse.Sse
import javax.ws.rs.sse.SseEventSink

@Service
@Hidden
class AccountsApiImpl(
    private val accountEventService: AccountEventService,
    private val accountRepository: AccountRepository,
    private val eventBusService: EventBusService
) : AccountsApi, AccountsApiService, AccountsApiResource {
    private val logger = KotlinLogging.logger { }

    @Context
    private val httpServletResponse: HttpServletResponse? = null

    @Context
    private val uriInfo: UriInfo? = null

    @Context
    private val sse: Sse? = null

    @Transactional(value = TxType.REQUIRES_NEW)
    override fun getAllAccounts(tags: Set<String>): List<AccountDTO> {
        logger.debug { "Getting all accounts by ${tags.size} tag(s)." }
        logger.trace { "Getting all accounts by tag(s): $tags." }
        val tagFilterCondition =
            { account: Account -> tags.let { if (it.isEmpty()) true else (it intersect account.tags).isNotEmpty() } }
        val accountsSequence = accountRepository.findAllOrderedByName()
            .asSequence()
            .filter(tagFilterCondition)
        val result = accountsSequence.map { account: Account -> mapAccountToAccountDTO(account) }
            .toList()
        logger.debug { "Got ${result.size} accounts." }
        logger.trace { "Got accounts: $result" }
        return result
    }

    private fun mapAccountToAccountDTO(account: Account): AccountDTO {
        return AccountDTOImpl.builder()
            .id(account.id)
            .from(mapAccountToAccountDataDTO(account))
            .movementCount(account.movementCount)
            .total(account.movementTotal)
            .build()
    }

    private fun mapAccountToAccountDataDTO(account: Account): AccountDataDTO {
        return AccountDataDTOImpl.builder()
            .name(account.name)
            .openDate(account.openDate)
            .tags(account.tags)
            .build()
    }

    @Transactional
    override fun addAccount(accountData: AccountDataDTO) {
        logger.info("Creating new account.")
        logger.debug { "Creating new account with name: ${accountData.name}." }
        val account = Account().setName(accountData.name)
            .setOpenDate(accountData.openDate)
            .setTags(accountData.tags)
        val accountId = accountRepository.save(account)
            .id
        val location = uriInfo!!.getAbsolutePathBuilder()
            .path(AccountsApi::class.java, "getAccount")
            .build(accountId)
        httpServletResponse!!.addHeader(HttpHeaders.LOCATION, location.toString())
        logger.info("New account is successfully created.")
        logger.debug { "New account is successfully created with id: $accountId." }
    }

    @Transactional
    override fun changeAccount(accountId: Int, accountData: AccountDataDTO): AccountDataDTO {
        val account: Account = getAccountByIdOrThrowNotFound(accountId, AccountsApiError.NO_ACCOUNT_BY_CHANGE)
        account.setName(accountData.getName())
            .setOpenDate(accountData.getOpenDate())
            .setTags(accountData.getTags())
        accountRepository.save(account)
        return accountData
    }

    fun getAccountByIdOrThrowNotFound(accountId: Int, error: AccountsApiError): Account {
        return accountRepository.findById(accountId)
            .orElseThrow {
                val errorMessage: String = NO_ACCOUNT_IS_FOUND_MESSAGE(accountId)
                val exception = ApiException(error, errorMessage)
                logger.warn(errorMessage)
                logger.trace(errorMessage, exception)
                exception
            }
    }

    fun deleteAccount(accountId: Int?) {
        // TODO Auto-generated method stub
        //
        throw UnsupportedOperationException()
    }

    @GET
    @Produces(MediaType.SERVER_SENT_EVENTS)
    fun sendChangeAccount(@Context sink: SseEventSink) {
        val eventBuilder = sse!!.newEventBuilder()
        sse.eventBusService.subscribeToEvents("" /* !!! Hit visitElement for element type: class org.jetbrains.kotlin.nj2k.tree.JKErrorExpression !!! */)
        accountEventService.registerEventListener()
            .map { changeAccountEvent ->
                eventBuilder.mediaType(MediaType.APPLICATION_JSON_TYPE)
                    .data(changeAccountEvent)
                    .build()
            }
            .forEach { t -> sink.send(t) }
            .onResolve(sink::close)
    }

    fun getAccountaccountId: Int): AccountDTO {
        logger.debug{ "Getting account info with ID: $accountId" }
        val account = getAccountByIdOrThrowNotFound(accountId, AccountsApiError.NO_ACCOUNT_ON_GET_ACCOUNT)
        AccountsApiImpl.log.debug("Got account info with ID: {}", account.getId())
        return mapAccountToAccountDTO(account)
    }

    companion object {
        private fun NO_ACCOUNT_IS_FOUND_MESSAGE(id: Int) = "No account is found for id: $id."
    }
}