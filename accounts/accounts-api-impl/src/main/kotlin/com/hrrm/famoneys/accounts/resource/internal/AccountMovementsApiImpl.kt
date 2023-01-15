package com.hrrm.famoneys.accounts.resource.internal

import com.hrrm.famoney.accounts.api.AccountsApiResource
import com.hrrm.famoney.accounts.api.resources.AccountMovementsApi
import com.hrrm.famoney.accounts.movement.MovementRepository
import com.hrrm.famoneys.accounts.api.AccountsApiResource
import com.hrrm.famoneys.accounts.events.MovementEventService
import com.hrrm.famoneys.accounts.internal.MovementApiService
import io.swagger.v3.oas.annotations.Hidden
import lombok.extern.log4j.Log4j2
import org.springframework.stereotype.Service
import java.util.*
import java.util.stream.Collectors
import javax.servlet.http.HttpServletResponse
import javax.ws.rs.core.Context
import javax.ws.rs.core.UriInfo

@Service
@Hidden
class AccountMovementsApiImpl( private val movementRepository: MovementRepository,
        private val accountsApiService: AccountsApiService,
        private val movementsApiService: MovementApiService,
        private val movementEventService: MovementEventService,
        private val operationTimestamProvider: OperationTimestampProvider) : AccountMovementsApi, AccountsApiResource {

    @Context
    private val httpServletResponse: HttpServletResponse? = null

    @Context
    private val uriInfo: UriInfo? = null

    @Context
    private val sse: Sse? = null

    @Context
    private val httpHeaders: HttpHeaders? = null
    @Transactional
    fun getMovements(@NotNull accountId: Int?, offset: Int?, limit: Int?): List<MovementDTO> {
        val offsetOptional = Optional.ofNullable(offset)
        val limitOptional = Optional.ofNullable(limit)
        AccountMovementsApiImpl.log.debug("Getting all movemnts of account with id: {}, offset: {} and count: {}.",
            accountId,
            offsetOptional
                .map { obj: Int -> obj.toString() }
                .orElse("\"from beginning\""),
            limitOptional.map { obj: Int -> obj.toString() }
                .orElse("\"all\""))
        val account: Account? = accountsApiService!!.getAccountByIdOrThrowNotFound(
            accountId,
            AccountsApiError.NO_ACCOUNT_ON_GET_ALL_ACCOUNT_MOVEMENTS
        )
        operationTimestamProvider.setTimestamp()
        val movements: Unit = movementRepository.getMovementsByAccountIdWithOffsetAndLimitOrderedByPos(
            account, offset,
            limit
        )
        val movementDTOs: List<MovementDTO> = movements.stream()
            .map { movement ->
                MovementDTOImpl.builder()
                    .id(movement.getId())
                    .data(convertMovement(movement))
                    .position(movement.getPosition())
                    .total(movement.getTotal())
                    .build()
            }
            .collect(Collectors.toList())
        AccountMovementsApiImpl.log.debug("Got {} movemnts of account with ID: {}", movementDTOs.size, accountId)
        AccountMovementsApiImpl.log.trace("Got movemnts of account with ID: {}. {}", accountId, movementDTOs)
        return movementDTOs
    }

    @Transactional
    fun getMovement(@NotNull accountId: Int?, @NotNull movementId: Int?): MovementDTO {
        AccountMovementsApiImpl.log.debug(
            "Geting movement info by id {} from account with id: {} with data: {}",
            movementId,
            accountId
        )
        val account: Account? = accountsApiService!!.getAccountByIdOrThrowNotFound(
            accountId,
            AccountsApiError.NO_ACCOUNT_ON_ADD_MOVEMENT
        )
        operationTimestamProvider.setTimestamp()
        val movement: Unit = movementRepository.findById(movementId)
            .filter { m -> account.equals(m.getAccount()) }
            .orElseThrow {
                val errorMessage: Unit = MessageFormat.format(
                    NO_ACCOUNT_MOVEMENT_IS_FOUND_MESSAGE, account.getId(),
                    movementId
                )
                val exception = ApiException(AccountsApiError.NO_MOVEMENT_ON_GET_MOVEMENT, errorMessage)
                AccountMovementsApiImpl.log.warn(errorMessage)
                AccountMovementsApiImpl.log.trace(errorMessage, exception)
                exception
            }
        val movementDTO: Unit = MovementDTOImpl.builder()
            .id(movement.getId())
            .data(convertMovement(movement))
            .build()
        AccountMovementsApiImpl.log.debug("Got movement info by id {} from account with id: {}.", movementId, accountId)
        AccountMovementsApiImpl.log.trace(
            "Got movement info by id {} from account with id: {} with data: {}", movementId, accountId,
            movementDTO
        )
        return movementDTO
    }

    fun changeMovement(
        @NotNull accountId: Int?, @NotNull movementId: Int?,
        movementDataDTO: MovementDataDTO?
    ): MovementDTO {
        AccountMovementsApiImpl.log.debug("Changing movement id: {} in account id: {}.", movementId, accountId)
        return try {
            val account: Account? = accountsApiService!!.getAccountByIdOrThrowNotFound(
                accountId,
                AccountsApiError.NO_ACCOUNT_ON_CHANGE_MOVEMENT
            )
            operationTimestamProvider.setTimestamp()
            val movementToChange: Unit = movementRepository.findById(movementId)
                .filter { movement -> account.equals(movement.getAccount()) }
                .orElseThrow {
                    val errorMessage: Unit = MessageFormat.format(
                        NO_ACCOUNT_MOVEMENT_IS_FOUND_MESSAGE, account.getId(),
                        movementId
                    )
                    val exception = ApiException(
                        AccountsApiError.NO_MOVEMENT_ON_CHANGE_MOVEMENT,
                        errorMessage
                    )
                    AccountMovementsApiImpl.log.warn(errorMessage)
                    AccountMovementsApiImpl.log.trace(errorMessage, exception)
                    exception
                }
            val position: Unit = movementToChange.getPosition()
            val resultMovement: Unit = movementsApiService.updateMovement(movementToChange, movementDataDTO)
            val positionAfter: Unit = resultMovement.getPosition()
            val data: MovementDataDTO = convertMovement(resultMovement)
            val eventData: Unit = ChangeEventDataImpl.builder()
                .accountId(accountId)
                .position(position)
                .positionAfter(positionAfter)
                .build()
            movementEventService.putEvent(eventData)
            val movementDTO: Unit = MovementDTOImpl.builder()
                .id(resultMovement.getId())
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
        private const val NO_ACCOUNT_MOVEMENT_IS_FOUND_MESSAGE =
            "No movement info for id: {1} is found in account with id: {0}."
    }
}