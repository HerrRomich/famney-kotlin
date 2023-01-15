package com.hrrm.famoneys.accounts.resource.internal

import com.fasterxml.jackson.databind.ObjectMapper
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
import unwrap
import java.util.*
import javax.servlet.http.HttpServletResponse
import javax.transaction.Transactional
import javax.transaction.Transactional.TxType
import javax.ws.rs.GET
import javax.ws.rs.Produces
import javax.ws.rs.core.*
import javax.ws.rs.sse.Sse
import javax.ws.rs.sse.SseEventSink

@Service
@Hidden
class AccountsApiImpl(
    private val accountEventService: AccountEventService,
    private val accountRepository: AccountRepository,
    private val eventBusService: EventBusService,
    private val objectMapper: ObjectMapper,
) : AccountsApi, AccountsApiService, AccountsApiResource {
    private val logger = KotlinLogging.logger { }

    @Context
    private lateinit var httpServletResponse: HttpServletResponse

    @Context
    private lateinit var uriInfo: UriInfo

    @Context
    private lateinit var sse: Sse

    @Transactional(value = TxType.REQUIRES_NEW)
    override fun getAllAccounts(tags: Set<String>): List<AccountDTO> {
        logger.debug { "Getting all accounts by ${tags.size} tag(s)." }
        logger.trace {
            """Getting all accounts by tag(s):
              |${objectMapper.writeValueAsString(tags)}""".trimMargin()
        }
        val tagFilterCondition =
            { account: Account -> tags.let { if (it.isEmpty()) true else (it intersect account.tags).isNotEmpty() } }
        val accountsSequence = accountRepository.findAllOrderedByName()
            .asSequence()
            .filter(tagFilterCondition)
        val result = accountsSequence.map { account: Account -> mapAccountToAccountDTO(account) }
            .toList()
        logger.debug { "Got ${result.size} accounts." }
        logger.trace {
            """Got accounts: 
              |${objectMapper.writeValueAsString(result)}""".trimMargin()
        }
        return result
    }

    private fun mapAccountToAccountDTO(account: Account): AccountDTO {
        return AccountDTO(
            id = account.id!!,
            name = account.name,
            openDate = account.openDate,
            tags = account.tags,
            movementCount = account.movementCount,
            total = account.movementTotal
        )
    }

    @Transactional
    override fun addAccount(accountData: AccountDataDTO) {
        logger.debug { "Creating new account with name: ${accountData.name}." }
        logger.trace {
            """Creating new account
              |${objectMapper.writeValueAsString(accountData)}""".trimMargin()
        }
        val account = Account().apply {
            name = accountData.name
            openDate = accountData.openDate
            tags = accountData.tags
        }
        val accountId = accountRepository.save(account)
            .id
        val location = uriInfo.getAbsolutePathBuilder()
            .path(AccountsApi::class.java, "getAccount")
            .build(accountId)
        httpServletResponse.addHeader(HttpHeaders.LOCATION, location.toString())
        httpServletResponse.status = Response.Status.CREATED.statusCode
        logger.debug { "New account is successfully created with id: $accountId." }
    }

    @Transactional
    override fun changeAccount(accountId: Int, accountData: AccountDataDTO): AccountDTO {
        logger.debug { "Changing account by id: ${accountId}." }
        logger.trace {
            """Changing account by id: ${accountId}. 
              |${objectMapper.writeValueAsString(accountData)}""".trimMargin()
        }
        val account: Account = getAccountByIdOrThrowNotFound(accountId, AccountsApiError.NO_ACCOUNT_ON_CHANGE)
        account.apply {
            name = accountData.name
            openDate = accountData.openDate
            tags = accountData.tags
        }
        accountRepository.save(account)
        val accountDTO = mapAccountToAccountDTO(account)
        logger.debug { "Changed account by id: ${accountId}." }
        logger.trace {
            """Cchanged account: 
              |${objectMapper.writeValueAsString(accountDTO)}""".trimMargin()
        }
        return accountDTO
    }

    override fun getAccountByIdOrThrowNotFound(accountId: Int, error: AccountsApiError): Account {
        return accountRepository.findById(accountId).unwrap()
            ?: run {
                val errorMessage: String = NO_ACCOUNT_IS_FOUND_MESSAGE(accountId)
                val exception = ApiException(error, errorMessage)
                logger.warn(errorMessage)
                logger.trace(errorMessage, exception)
                throw exception
            }
    }

    /*@GET
    @Produces(MediaType.SERVER_SENT_EVENTS)
    fun sendChangeAccount(@Context sink: SseEventSink) {
        val eventBuilder = sse.newEventBuilder()
        sse.

        sse.eventBusService.subscribeToEvents("" /* !!! Hit visitElement for element type: class org.jetbrains.kotlin.nj2k.tree.JKErrorExpression !!! */)
        accountEventService.registerEventListener()
            .map { changeAccountEvent ->
                eventBuilder.mediaType(MediaType.APPLICATION_JSON_TYPE)
                    .data(changeAccountEvent)
                    .build()
            }
            .forEach { t -> sink.send(t) }
            .onResolve(sink::close)
    }*/

    override fun getAccount(accountId: Int): AccountDTO {
        logger.debug { "Getting account info by id: $accountId" }
        val account = getAccountByIdOrThrowNotFound(accountId, AccountsApiError.NO_ACCOUNT_ON_GET_ACCOUNT)
        val accountDTO = mapAccountToAccountDTO(account)
        logger.debug { "Got account info with by id: $accountId" }
        logger.trace {
            """Got account info
              |${objectMapper.writeValueAsString(accountDTO)}""".trimMargin()
        }
        return accountDTO
    }

    companion object {
        private fun NO_ACCOUNT_IS_FOUND_MESSAGE(id: Int) = "No account is found for id: $id."
    }
}