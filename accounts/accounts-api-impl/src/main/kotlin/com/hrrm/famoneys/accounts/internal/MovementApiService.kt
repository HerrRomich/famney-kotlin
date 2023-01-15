package com.hrrm.famoneys.accounts.internal

import com.hrrm.famoneys.accounts.Account
import lombok.extern.log4j.Log4j2
import org.apache.logging.log4j.util.Supplier
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import javax.persistence.EntityManager
import javax.persistence.LockModeType

@RequiredArgsConstructor
@Log4j2
@Service
class MovementApiService {
    private val accountRepository: AccountRepository? = null
    private val movementRepository: MovementRepository? = null
    private val entryApiService: EntryApiService? = null
    private val entityManager: EntityManager? = null
    @Transactional
    @Throws(UnknownMovementType::class)
    fun addMovement(account: Account, movementDataDTO: MovementDataDTO): Movement {
        MovementApiService.log.debug("Creating movement.")
        MovementApiService.log.trace("A new movement will be created with DTO data {}.") { movementDataDTO }
        val movement: Movement = callAddMovement(account, movementDataDTO)
        MovementApiService.log.debug("A new movement is created.")
        MovementApiService.log.trace("A new movement {} is created.") { movement }
        return movement
    }

    @Throws(UnknownMovementType::class)
    private fun callAddMovement(account: Account, movementDataDTO: MovementDataDTO): Movement {
        var movement: Movement?
        movement = if (movementDataDTO is EntryDataDTO) {
            entryApiService!!.createMovement(movementDataDTO as EntryDataDTO)
        } else {
            val message: String =
                MessageFormat.format(UNKNOWN_DTO_MESSAGE, movementDataDTO.getClass())
            MovementApiService.log.warn(message)
            throw UnknownMovementType(message)
        }
        entityManager!!.lock(account, LockModeType.PESSIMISTIC_WRITE)
        val newPosition: Unit = movementRepository.getLastPositionByAccountOnDate(account, movementDataDTO.getDate())
        movement = setMovementAttributes(account, movement, movementDataDTO).setPosition(newPosition)
        adjustAccountMovements(movement, newPosition)
        val addedMovement: Unit = movementRepository.save(movement)
        movementRepository.flush()
        return addedMovement
    }

    private fun setMovementAttributes(
        account: Account,
        movement: Movement?,
        movementDataDTO: MovementDataDTO
    ): Movement {
        return movement.setAccount(account)
            .setDate(movementDataDTO.getDate())
            .setBookingDate(
                movementDataDTO.getBookingDate()
                    .orElse(null)
            )
            .setBudgetPeriod(
                movementDataDTO.getBudgetPeriod()
                    .orElse(null)
            )
    }

    private fun adjustAccountMovements(movement: Movement?, positionAfter: Int) {
        val account: Unit = movement.getAccount()
        account.setMovementCount(account.getMovementCount() + 1)
        account.setMovementTotal(
            account.getMovementTotal()
                .add(movement.getAmount())
        )
        movementRepository.adjustMovementPositionsAndSumsByAccountAfterPosition(movement, positionAfter)
        val totalBeforeMovement: Unit = movementRepository.findNextMovementByAccountIdBeforePosition(
            account,
            positionAfter
        )
            .map(Movement::getTotal)
            .orElse(BigDecimal.ZERO)
        movement.setTotal(totalBeforeMovement.add(movement.getAmount()))
            .setPosition(positionAfter)
    }

    @Throws(IncompatibleMovementType::class)
    fun updateMovement(movement: Movement, movementDataDTO: MovementDataDTO): Movement? {
        MovementApiService.log.debug("Updateing movement.")
        MovementApiService.log.trace(
            "Movement {} will be updated with DTO data {}.",
            Supplier<Any> { movement },
            Supplier<Any> { movementDataDTO })
        entityManager!!.lock(movement.getAccount(), LockModeType.PESSIMISTIC_WRITE)
        val positionBefore: Unit = movement.getPosition()
        rollbackAccount(movement)
        var positionAfter = positionBefore
        if (!movement.getDate()
                .equals(movementDataDTO.getDate())
        ) {
            positionAfter = movementRepository.getLastPositionByAccountOnDate(
                movement.getAccount(), movementDataDTO
                    .getDate()
            )
        }
        var updatedMovement: Movement?
        updatedMovement = if (movementDataDTO is EntryDataDTO && movement is Entry) {
            entryApiService!!.updateMovement(movement as Entry, movementDataDTO as EntryDataDTO)
        } else {
            val message: String = MessageFormat.format(
                INCOMPATIBLE_ENTITY_AND_DTO_MESSAGE, movement.getClass(), movement
                    .getAccount()
                    .getId(), movement.getId(), movementDataDTO.getClass()
            )
            MovementApiService.log.warn(message)
            throw IncompatibleMovementType(message)
        }
        updatedMovement = setMovementAttributes(movement.getAccount(), updatedMovement, movementDataDTO)
        adjustAccountMovements(updatedMovement, positionAfter)
        MovementApiService.log.debug("Movement is updated.")
        MovementApiService.log.trace("Movement {} is updated.") { movement }
        return updatedMovement
    }

    private fun rollbackAccount(movement: Movement) {
        val positionBefore: Unit = movement.getPosition()
        movement.setPosition(-1)
        val account: Unit = movement.getAccount()
        account.setMovementCount(account.getMovementCount() - 1)
        account.setMovementTotal(
            account.getMovementTotal()
                .subtract(movement.getAmount())
        )
        movementRepository.rollbackMovementPositionsAndSumsByAccountAfterPosition(movement, positionBefore)
    }

    companion object {
        private const val INCOMPATIBLE_ENTITY_AND_DTO_MESSAGE =
            "Movement entity class [{0}] for account id: {1} and movement id: {2] are incompatible with DTO class [{3}]."
        private const val UNKNOWN_DTO_MESSAGE = "Unknown DTO class [{0}]."
    }
}