package com.hrrm.famoneys.accounts.resource.internal

import com.fasterxml.jackson.databind.ObjectMapper
import com.hrrm.famoney.accounts.Account
import com.hrrm.famoney.accounts.api.AccountsApiResource
import com.hrrm.famoney.accounts.api.MovementDTO
import com.hrrm.famoney.accounts.api.MovementDataDTO
import com.hrrm.famoney.accounts.api.resources.AccountMovementsApi
import com.hrrm.famoney.accounts.movement.MovementRepository
import com.hrrm.famoney.jaxrs.ApiException
import com.hrrm.famoneys.accounts.events.MovementEventService
import com.hrrm.famoneys.accounts.internal.MovementApiService
import com.hrrm.famoneys.accounts.internalexceptions.AccountsApiError
import io.swagger.v3.oas.annotations.Hidden
import mu.KotlinLogging
import org.springframework.stereotype.Service
import unwrap
import java.text.MessageFormat
import java.util.*
import javax.servlet.http.HttpServletResponse
import javax.transaction.Transactional
import javax.ws.rs.core.Context
import javax.ws.rs.core.HttpHeaders
import javax.ws.rs.core.UriInfo
import javax.ws.rs.sse.Sse

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
        accountsApiService.getAccountByIdOrThrowNotFound(
            accountId,
            AccountsApiError.NO_ACCOUNT_ON_GET_ALL_ACCOUNT_MOVEMENTS
        )
        val movements = movementRepository.getMovementsByAccountIdWithOffsetAndLimitOrderedByPos(
            accountId, offset,
            limit
        )
        val movementDTOs: List<MovementDTO> = movements.map { movement ->
            MovementDTO(
                id = movement.id!!,
                data = convertMovement(movement),
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
                val errorMessage = NO_ACCOUNT_MOVEMENT_IS_FOUND_MESSAGE(movementId, accountId)
                val exception = ApiException(AccountsApiError.NO_MOVEMENT_ON_GET_MOVEMENT, errorMessage)
                logger.warn { errorMessage }
                logger.trace(exception) { errorMessage }
                throw exception
            }
        val movementDTO = MovementDTO(
            id = movement.id!!,
            data = convertMovement(movement),
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
                    val errorMessage = NO_ACCOUNT_MOVEMENT_IS_FOUND_MESSAGE(movementId, accountId)
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
            val positionAfter = resultMovement.getPosition()
            val data: MovementDataDTO = convertMovement(resultMovement)
            val eventData = MovementEventService.ChangeEventData(
                accountId = accountId,
                position = position,
                positionAfter = positionAfter,
            )
            movementEventService.putEvent(eventData)
            val movementDTO = MovementDTO(
                id = resultMovement.
            )
                .data(data)
                .position(positionAfter)
                .total(data.getAmount())
                .build()
            AccountMovementsApiImpl.log.debug("A movement was added to account.")
            AccountMovementsApiImpl.log.trace(
                "A movement was added to account by id : {}. A new id: {} was generated.", accountId, movementDTO
                    .getId()
            )
            movementDTO
        } catch (e: TransactionException) {
            val message = "Problem during adding a movement to account."
            AccountMovementsApiImpl.log.error(message, e)
            throw ApiException(message)
        } catch (e: IncompatibleMovementType) {
            val message = "Problem during adding a movement to account."
            AccountMovementsApiImpl.log.error(message, e)
            throw ApiException(message)
        }
    }

    fun addMovement(@NotNull accountId: Int?, movementDataDTO: MovementDataDTO?): MovementDTO {
        AccountMovementsApiImpl.log.debug("Adding movement.")
        AccountMovementsApiImpl.log.trace("Adding movement to account id: {} with data: {}", accountId, movementDataDTO)
        return try {
            val account: Account? = accountsApiService!!.getAccountByIdOrThrowNotFound(
                accountId,
                AccountsApiError.NO_ACCOUNT_ON_ADD_MOVEMENT
            )
            operationTimestamProvider.setTimestamp()
            val resultMovement: Unit = movementsApiService.addMovement(account, movementDataDTO)
            val position: Unit = resultMovement.getPosition()
            val eventData: Unit = AddEventDataImpl.builder()
                .accountId(accountId)
                .position(position)
                .build()
            val movementWithEventData: Unit = MovementWihEventDataImpl.builder()
                .movement(resultMovement)
                .eventData(eventData)
                .build()
            httpServletResponse.setStatus(Status.CREATED.getStatusCode())
            val location: Unit = uriInfo.getAbsolutePathBuilder()
                .path(AccountMovementsApi::class.java, "getMovement")
                .build(accountId, resultMovement.getId())
            httpServletResponse.setHeader(HttpHeaders.LOCATION, location.toString())
            val data: MovementDataDTO = convertMovement(resultMovement)
            movementEventService.putEvent(eventData)
            val movementDTO: Unit = MovementDTOImpl.builder()
                .id(resultMovement.getId())
                .data(data)
                .position(resultMovement.getPosition())
                .total(data.getAmount())
                .build()
            AccountMovementsApiImpl.log.debug("A movement was added to account.")
            AccountMovementsApiImpl.log.trace(
                "A movement was added to account id : {}. A new id: {} was generated.", accountId, movementDTO
                    .getId()
            )
            movementDTO
        } catch (e: TransactionException) {
            val message = "Problem during adding a movement to account."
            AccountMovementsApiImpl.log.error(message, e)
            throw ApiException(message)
        } catch (e: UnknownMovementType) {
            val message = "Problem during adding a movement to account."
            AccountMovementsApiImpl.log.error(message, e)
            throw ApiException(message)
        }
    }

    private fun convertMovement(movement: Movement): MovementDataDTO {
        return getMovementDataDTOBuilder(movement).date(movement.getDate())
            .bookingDate(Optional.ofNullable(movement.getBookingDate()))
            .budgetPeriod(Optional.ofNullable(movement.getBudgetPeriod()))
            .amount(movement.getAmount())
            .build()
    }

    private fun getMovementDataDTOBuilder(movement: Movement): MovementDataDTOBuilder<out MovementDataDTO?> {
        return if (movement is Entry) {
            val entry: Entry = movement as Entry
            val iterable: Iterable<EntryItemDataDTO> = Iterable<EntryItemDataDTO> {
                entry.getEntryItems()
                    .stream()
                    .map { entryItem ->
                        Builder().categoryId(entryItem.getCategoryId())
                            .amount(entryItem.getAmount())
                            .comments(Optional.ofNullable(entryItem.getComments()))
                            .build()
                    }
                    .iterator()
            }
            Builder().addAllEntryItems(iterable)
        } else if (movement is Refund) {
            val refund: Refund = movement as Refund
            Builder().categoryId(refund.getCategoryId())
                .comments(refund.getComments())
        } else {
            val transfer: Transfer = movement as Transfer
            Builder().oppositAccountId(transfer.getOppositAccountId())
        }
    }

    fun deleteMovement(@NotNull accountId: Int?, @NotNull movementId: Int?) {
        // TODO Should be implemented.
        UnsupportedOperationException("Not implemented yet")
    }

    @GET
    @Path("events")
    @Produces(MediaType.SERVER_SENT_EVENTS)
    fun sendChangeMovement(@PathParam("accountId") accountId: Int?, @Context sink: SseEventSink) {
        val eventBuilder: Unit = sse.newEventBuilder()
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
    }

    @Value.Immutable
    @ImmutableStyle
    interface MovementWihEventData {
        val movement: Movement?
        val eventData: EventData?
    }

    companion object {
        private fun NO_ACCOUNT_MOVEMENT_IS_FOUND_MESSAGE(movementId: Int, accountId: Int) =
            "No movement info by id: $movementId is found in account by id: $accountId."
    }
}