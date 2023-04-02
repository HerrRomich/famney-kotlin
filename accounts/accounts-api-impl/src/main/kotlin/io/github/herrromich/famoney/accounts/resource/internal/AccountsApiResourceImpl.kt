package io.github.herrromich.famoney.accounts.resource.internal

import com.fasterxml.jackson.databind.ObjectMapper
import io.github.herrromich.famoney.accounts.api.dto.AccountDTO
import io.github.herrromich.famoney.accounts.api.dto.AccountDataDTO
import io.github.herrromich.famoney.accounts.api.resources.AccountsResource
import io.github.herrromich.famoney.accounts.internalexceptions.AccountsApiError
import io.github.herrromich.famoney.domain.accounts.Account
import io.github.herrromich.famoney.domain.accounts.AccountRepository
import io.github.herrromich.famoney.jaxrs.ApiException
import io.github.herrromich.famoney.jaxrs.BasisApiResource
import io.swagger.v3.oas.annotations.Hidden
import jakarta.ws.rs.core.HttpHeaders
import jakarta.ws.rs.core.Response
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import unwrap

@Service
@Hidden
class AccountsApiResourceImpl(
    private val accountRepository: AccountRepository,
    private val objectMapper: ObjectMapper,
) : BasisApiResource(), AccountsResource, AccountsApiService {
    @Transactional
    override fun getAllAccounts(): List<AccountDTO> {
        logger.debug { "Getting all accounts." }
        val accountsSequence = accountRepository.findAll()
            .asSequence()
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
            openingDate = account.openingDate,
            tags = account.tags,
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
            openingDate = accountData.openingDate
            tags = accountData.tags
        }
        val accountId = accountRepository.save(account)
            .id
        val location = uriInfo.getAbsolutePathBuilder()
            .path(AccountsResource::class.java, "getAccount")
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
            openingDate = accountData.openingDate
            tags = accountData.tags
        }
        accountRepository.save(account)
        val accountDTO = mapAccountToAccountDTO(account)
        logger.debug { "Changed account by id: ${accountId}." }
        logger.trace {
            """Changed account: 
              |${objectMapper.writeValueAsString(accountDTO)}""".trimMargin()
        }
        return accountDTO
    }

    override fun getAccountByIdOrThrowNotFound(accountId: Int, error: AccountsApiError): Account {
        return accountRepository.findById(accountId).unwrap()
            ?: run {
                val errorMessage: String = getNoAccountIsFoundMessage(accountId)
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
        private fun getNoAccountIsFoundMessage(id: Int) = "No account is found for id: $id."
    }
}
