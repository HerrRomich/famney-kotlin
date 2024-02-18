package io.github.herrromich.famoney.accounts.resource.internal

import com.fasterxml.jackson.databind.ObjectMapper
import io.github.herrromich.famoney.accounts.api.dto.*
import io.github.herrromich.famoney.accounts.api.resources.AccountMovementsApiResource
import io.github.herrromich.famoney.accounts.events.MovementEventService
import io.github.herrromich.famoney.accounts.internal.IncompatibleMovementTypeException
import io.github.herrromich.famoney.accounts.internal.MovementApiService
import io.github.herrromich.famoney.accounts.internal.UnsupportedMovementTypeException
import io.github.herrromich.famoney.accounts.internalexceptions.AccountsApiError
import io.github.herrromich.famoney.domain.accounts.movement.*
import io.github.herrromich.famoney.jaxrs.ApiException
import io.swagger.v3.oas.annotations.Hidden
import jakarta.servlet.http.HttpServletResponse
import jakarta.ws.rs.core.Context
import jakarta.ws.rs.core.HttpHeaders
import jakarta.ws.rs.core.Response.Status
import jakarta.ws.rs.core.UriInfo
import jakarta.ws.rs.sse.Sse
import mu.KotlinLogging
import org.springframework.stereotype.Service
import org.springframework.transaction.TransactionException
import org.springframework.transaction.annotation.Transactional
import unwrap
import java.time.LocalDate

private val logger = KotlinLogging.logger { }

@Service
@Hidden
class AccountMovementsApiImpl(
    private val movementRepository: MovementRepository,
    private val accountsApiService: AccountsApiService,
    private val movementsApiService: MovementApiService,
    private val movementEventService: MovementEventService,
    private val objectMapper: ObjectMapper,
) : AccountMovementsApiResource {

    @Context
    private lateinit var httpServletResponse: HttpServletResponse

    @Context
    private lateinit var uriInfo: UriInfo

    @Context
    private lateinit var sse: Sse

    @Context
    private lateinit var httpHeaders: HttpHeaders

    @Transactional
    override fun readMovements(
        accountId: Int,
        dateFrom: LocalDate?,
        dateTo: LocalDate?,
        offset: Int?,
        limit: Int?
    ): List<MovementDTO> {
        // region logging before
        logger.debug {
            "Getting all movemnts of account by id: $accountId" +
                    "${dateFrom?.let { " ,since: ${it}" } ?: ""}" +
                    "${dateTo?.let { " ,until: ${it}" } ?: ""}" +
                    ", offset: ${offset ?: "\"from beginning\""} and count: ${limit ?: "\"all\""}."
        }
        // endregion
        val account = accountsApiService.getAccountByIdOrThrowNotFound(
            accountId,
            AccountsApiError.NO_ACCOUNT_ON_GET_ALL_ACCOUNT_MOVEMENTS
        )
        val movements = movementRepository.getByAccountAndDateRangeOrderByDatePosition(
            account, dateFrom, dateTo, offset, limit
        )
        val movementDTOs: List<MovementDTO> = movements.map { movement ->
            MovementDTO(
                id = movement.id!!,
                data = toMovementDataDTO(movement),
                position = movement.position.toInt(),
                total = movement.total
            )
        }
        // region logging after
        logger.debug {
            "Got ${movementDTOs.size} movements of account by id: $accountId" +
                    "${dateFrom?.let { " ,since: ${it}" } ?: ""}" +
                    "${dateTo?.let { " ,until: ${it}" } ?: ""}" +
                    ", offset: ${offset ?: "\"from beginning\""} and count: ${limit ?: "\"all\""}."
        }
        logger.trace {
            """Got movements of account by id: $accountId.
              |${objectMapper.writeValueAsString(movementDTOs)}""".trimMargin()
        }
        // endregion
        return movementDTOs
    }

    @Transactional
    override fun getMovementsCount(accountId: Int, dateFrom: LocalDate?, dateTo: LocalDate?): Int {
        // region logging before
        logger.debug {
            "Getting movements count of account by id: $accountId" +
                    "${dateFrom?.let { " ,since: ${it}" } ?: ""}" +
                    "${dateTo?.let { " ,until: ${it}" } ?: ""}"
        }
        // endregion
        val account = accountsApiService.getAccountByIdOrThrowNotFound(
            accountId,
            AccountsApiError.NO_ACCOUNT_ON_GET_ALL_ACCOUNT_MOVEMENTS
        )
        val count = movementRepository.getCountByAccountAndDateRange(
            account, dateFrom, dateTo
        )
        // region logging after
        logger.debug { "Got $count movements of account by id: $accountId" }
        // endregion
        return count
    }

    @Transactional
    override fun readMovement(accountId: Int, movementId: Int): MovementDTO {
        // region logging before
        logger.debug { "Getting movement info by id $movementId from account by id: $accountId." }
        // endregion
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
            position = movement.position.toInt(),
            total = movement.total
        )
        logger.debug { "Got movement info by id: $movementId from account by id: accountId." }
        logger.trace {
            """Got movement info by id: $movementId from account by id: $accountId.
              |${objectMapper.writeValueAsString(movementDTO)}""".trimMargin()
        }
        // endregion
        return movementDTO
    }

    @Transactional
    override fun updateMovement(
        accountId: Int, movementId: Int,
        movementDataDTO: MovementDataDTO
    ): MovementDTO {
        // region logging before
        logger.debug { "Updating movement by id: $movementId in account id: accountId." }
        // endregion
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
                position = position.toInt(),
                positionAfter = positionAfter.toInt(),
            )
            movementEventService.putEvent(eventData)
            val movementDTO = MovementDTO(
                id = resultMovement.id!!,
                data = data,
                position = positionAfter.toInt(),
                total = data.amount
            )
            // region logging after
            logger.debug { "A movement was updated." }
            logger.trace { "A movement was updated in account by id : $accountId." }
            // endregion
            movementDTO
        } catch (e: TransactionException) {
            throw ApiException(e, "Problem during update of movement.")
        } catch (e: IncompatibleMovementTypeException) {
            throw ApiException(e, "Problem during update of movement.")
        }
    }

    override fun createMovement(accountId: Int, movementDataDTO: MovementDataDTO,): MovementDTO {
        logger.debug { "Adding movement." }
        logger.trace("Adding movement to account id: {} with data: {}", accountId, movementDataDTO)
        return try {
            val account = accountsApiService.getAccountByIdOrThrowNotFound(
                accountId,
                AccountsApiError.NO_ACCOUNT_ON_ADD_MOVEMENT
            )
            val resultMovement = movementsApiService.createMovement(account, movementDataDTO)
            val position = resultMovement.position
            val eventData = MovementEventService.AddEventData(
                accountId = accountId,
                position = position.toInt()
            )
            val movementWithEventData = MovementWihEventData(
                movement = resultMovement,
                eventData = eventData
            )
            httpServletResponse.setStatus(Status.CREATED.statusCode)
            val location = uriInfo.absolutePathBuilder
                .path(AccountMovementsApiResource::class.java, "getMovement")
                .build(accountId, resultMovement.id)
            httpServletResponse.setHeader(HttpHeaders.LOCATION, location.toString())
            val data: MovementDataDTO = toMovementDataDTO(resultMovement)
            movementEventService.putEvent(eventData)
            val movementDTO = MovementDTO(
                id = resultMovement.id!!,
                data = data,
                position = resultMovement.position.toInt(),
                total = data.amount
            )
            logger.debug { "A movement was added to account." }
            logger.trace { "A movement was added to account id : $accountId. A new id: ${movementDTO.id} was generated." }
            movementDTO
        } catch (e: TransactionException) {
            throw ApiException(e, "Problem during adding a movement to account.")
        } catch (e: UnsupportedMovementTypeException) {
            val message = "Problem during adding a movement to account."
            throw ApiException(e, "Problem during adding a movement to account.")
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
                    oppositAccountId = movement.oppositeAccountId,
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

            else -> throw UnsupportedMovementTypeException("Cannot convert ${movement::class.qualifiedName} to DTO.")
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

        private fun ApiException(exception: Exception, message: String): ApiException {
            logger.error(exception) { message }
            return ApiException(message)
        }
    }
}
