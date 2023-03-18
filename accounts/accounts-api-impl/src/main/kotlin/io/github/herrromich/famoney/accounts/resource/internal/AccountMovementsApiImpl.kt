package io.github.herrromich.famoney.accounts.resource.internal

import com.fasterxml.jackson.databind.ObjectMapper
import io.github.herrromich.famoney.accounts.api.*
import io.github.herrromich.famoney.accounts.api.resources.AccountMovementsApi
import io.github.herrromich.famoney.accounts.api.resources.BalanceDataDTO
import io.github.herrromich.famoney.accounts.events.MovementEventService
import io.github.herrromich.famoney.accounts.internal.IncompatibleMovementType
import io.github.herrromich.famoney.accounts.internal.MovementApiService
import io.github.herrromich.famoney.accounts.internal.UnknownMovementType
import io.github.herrromich.famoney.accounts.internalexceptions.AccountsApiError
import io.github.herrromich.famoney.domain.accounts.movement.*
import io.github.herrromich.famoney.jaxrs.ApiException
import io.swagger.v3.oas.annotations.Hidden
import jakarta.servlet.http.HttpServletResponse
import jakarta.transaction.Transactional
import jakarta.ws.rs.core.Context
import jakarta.ws.rs.core.HttpHeaders
import jakarta.ws.rs.core.Response.Status
import jakarta.ws.rs.core.UriInfo
import jakarta.ws.rs.sse.Sse
import mu.KotlinLogging
import org.hibernate.TransactionException
import org.springframework.stereotype.Service
import unwrap

@Service
@Hidden
class AccountMovementsApiImpl(
    private val movementRepository: MovementRepository,
    private val accountsApiService: AccountsApiService,
    private val movementsApiService: MovementApiService,
    private val movementEventService: MovementEventService,
    private val objectMapper: ObjectMapper,
) : AccountMovementsApi, AccountsApiResource {
    private val logger = KotlinLogging.logger { }

    @Context
    private lateinit var httpServletResponse: HttpServletResponse

    @Context
    private lateinit var uriInfo: UriInfo

    @Context
    private lateinit var sse: Sse

    @Context
    private lateinit var httpHeaders: HttpHeaders

    @Transactional
    override fun getMovements(accountId: Int, offset: Int?, limit: Int?): List<MovementDTO> {
        logger.debug { "Getting all movemnts of account by id: $accountId, offset: ${offset ?: "\"from beginning\""} and count: ${limit ?: "\"all\""}." }
        val account = accountsApiService.getAccountByIdOrThrowNotFound(
            accountId,
            AccountsApiError.NO_ACCOUNT_ON_GET_ALL_ACCOUNT_MOVEMENTS
        )
        val movements = movementRepository.getByAccountOrderByDatePosition(
            account, offset, limit
        )
        val movementDTOs: List<MovementDTO> = movements.map { movement ->
            MovementDTO(
                id = movement.id!!,
                data = toMovementDataDTO(movement),
                position = movement.position,
                total = movement.total
            )
        }
        logger.debug { "Got ${movementDTOs.size} movemnts of account by id: $accountId" }
        logger.trace {
            """Got movemnts of account by id: $accountId.
              |${objectMapper.writeValueAsString(movementDTOs)}""".trimMargin()
        }
        return movementDTOs
    }

    @Transactional
    override fun getMovement(accountId: Int, movementId: Int): MovementDTO {
        logger.debug { "Geting movement info by id $movementId from account by id: $accountId." }
        val account = accountsApiService.getAccountByIdOrThrowNotFound(
            accountId,
            AccountsApiError.NO_ACCOUNT_ON_ADD_MOVEMENT
        )
        val movement = movementRepository.findById(movementId).unwrap()
            ?.takeIf { m -> account == m.account }
            ?: run {
                val errorMessage = getNoAccountMovementIsFoundMessage(movementId, accountId)
                val exception = ApiException(AccountsApiError.NO_MOVEMENT_ON_GET_MOVEMENT, errorMessage)
                logger.warn { errorMessage }
                logger.trace(exception) { errorMessage }
                throw exception
            }
        val movementDTO = MovementDTO(
            id = movement.id!!,
            data = toMovementDataDTO(movement),
            position = movement.position,
            total = movement.total
        )
        logger.debug { "Got movement info by id: $movementId from account by id: accountId." }
        logger.trace {
            """Got movement info by id: $movementId from account by id: $accountId.
              |${objectMapper.writeValueAsString(movementDTO)}""".trimMargin()
        }
        return movementDTO
    }

    @Transactional
    override fun changeMovement(
        accountId: Int, movementId: Int,
        movementDataDTO: MovementDataDTO
    ): MovementDTO {
        logger.debug { "Changing movement by id: $movementId in account id: accountId." }
        return try {
            val account = accountsApiService.getAccountByIdOrThrowNotFound(
                accountId,
                AccountsApiError.NO_ACCOUNT_ON_CHANGE_MOVEMENT
            )
            val movementToChange = movementRepository.findById(movementId).unwrap()
                ?.takeIf { movement -> account == movement.account }
                ?: run {
                    val errorMessage = getNoAccountMovementIsFoundMessage(movementId, accountId)
                    val exception = ApiException(
                        AccountsApiError.NO_MOVEMENT_ON_CHANGE_MOVEMENT,
                        errorMessage
                    )
                    logger.warn { errorMessage }
                    logger.trace(exception) { errorMessage }
                    throw exception
                }
            val position = movementToChange.position
            val resultMovement = movementsApiService.updateMovement(movementToChange, movementDataDTO)
            val positionAfter = resultMovement.position
            val data: MovementDataDTO = toMovementDataDTO(resultMovement)
            val eventData = MovementEventService.ChangeEventData(
                accountId = accountId,
                position = position,
                positionAfter = positionAfter,
            )
            movementEventService.putEvent(eventData)
            val movementDTO = MovementDTO(
                id = resultMovement.id!!,
                data = data,
                position = positionAfter,
                total = data.amount
            )
            logger.debug { "A movement was added to account." }
            logger.trace { "A movement was added to account by id : $accountId. A new id: ${movementDTO.id} was generated." }
            movementDTO
        } catch (e: TransactionException) {
            val message = "Problem during adding a movement to account."
            logger.error(e) { message }
            throw ApiException(message)
        } catch (e: IncompatibleMovementType) {
            val message = "Problem during adding a movement to account."
            logger.error(e) { message }
            throw ApiException(message)
        }
    }

    override fun addMovement(accountId: Int, movementDataDTO: MovementDataDTO): MovementDTO {
        logger.debug { "Adding movement." }
        logger.trace("Adding movement to account id: {} with data: {}", accountId, movementDataDTO)
        return try {
            val account = accountsApiService.getAccountByIdOrThrowNotFound(
                accountId,
                AccountsApiError.NO_ACCOUNT_ON_ADD_MOVEMENT
            )
            val resultMovement = movementsApiService.addMovement(account, movementDataDTO)
            val position = resultMovement.position
            val eventData = MovementEventService.AddEventData(
                accountId = accountId,
                position = position
            )
            val movementWithEventData = MovementWihEventData(
                movement = resultMovement,
                eventData = eventData
            )
            httpServletResponse.setStatus(Status.CREATED.statusCode)
            val location = uriInfo.absolutePathBuilder
                .path(AccountMovementsApi::class.java, "getMovement")
                .build(accountId, resultMovement.id)
            httpServletResponse.setHeader(HttpHeaders.LOCATION, location.toString())
            val data: MovementDataDTO = toMovementDataDTO(resultMovement)
            movementEventService.putEvent(eventData)
            val movementDTO = MovementDTO(
                id = resultMovement.id!!,
                data = data,
                position = resultMovement.position,
                total = data.amount
            )
            logger.debug { "A movement was added to account." }
            logger.trace { "A movement was added to account id : $accountId. A new id: ${movementDTO.id} was generated." }
            movementDTO
        } catch (e: TransactionException) {
            val message = "Problem during adding a movement to account."
            logger.error(e) { message }
            throw ApiException(message)
        } catch (e: UnknownMovementType) {
            val message = "Problem during adding a movement to account."
            logger.error(e) { message }
            throw ApiException(message)
        }
    }

    private fun toMovementDataDTO(movement: Movement): MovementDataDTO {
        return when (movement) {
            is Entry -> {
                EntryDataDTO(
                    date = movement.date,
                    bookingDate = movement.bookingDate,
                    budgetPeriod = movement.budgetPeriod,
                    amount = movement.amount,
                    entryItems = movement.entryItems
                        .map {
                            BasicEntryItemDataDTO(
                                categoryId = it.categoryId,
                                amount = it.amount,
                                comments = it.comments
                            )
                        }
                )
            }

            is Refund -> {
                RefundDataDTO(
                    date = movement.date,
                    bookingDate = movement.bookingDate,
                    budgetPeriod = movement.budgetPeriod,
                    amount = movement.amount,
                    categoryId = movement.categoryId,
                    comments = movement.comments
                )
            }

            is Transfer -> {
                TransferDataDTO(
                    date = movement.date,
                    bookingDate = movement.bookingDate,
                    budgetPeriod = movement.budgetPeriod,
                    amount = movement.amount,
                    oppositAccountId = movement.oppositAccountId,
                    comments = movement.comments
                )
            }

            is Balance -> {
                BalanceDataDTO(
                    date = movement.date,
                    bookingDate = movement.bookingDate,
                    budgetPeriod = movement.budgetPeriod,
                    amount = movement.amount,
                )
            }

            else -> throw UnknownMovementType("Cannot convert ${movement::class.qualifiedName} to DTO.")
        }
    }

    override fun deleteMovement(accountId: Int, movementId: Int) {
        TODO("Not implemented yet")
    }

    /*@GET
    @Path("events")
    @Produces(MediaType.SERVER_SENT_EVENTS)
    fun sendChangeMovement(@PathParam("accountId") accountId: Int, @Context sink: SseEventSink) {
        val eventBuilder = sse.newEventBuilder()
        movementEventService.registerEventListener(accountId)
            .map { changeMovementEvent ->
                eventBuilder.id(
                    changeMovementEvent.getTimestamp()
                        .toString()
                )
                    .mediaType(MediaType.APPLICATION_JSON_TYPE)
                    .data(changeMovementEvent)
                    .build()
            }
            .forEach { t -> sink.send(t) }
            .onResolve(sink::close)
    }*/

    data class MovementWihEventData(
        val movement: Movement,
        val eventData: MovementEventService.EventData
    )

    companion object {
        private fun getNoAccountMovementIsFoundMessage(movementId: Int, accountId: Int) =
            "No movement info by id: $movementId is found in account by id: $accountId."
    }
}