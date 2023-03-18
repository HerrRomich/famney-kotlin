package com.hrrm.famoney.accounts.internal

import com.fasterxml.jackson.databind.ObjectMapper
import com.hrrm.famoney.accounts.api.EntryDataDTO
import com.hrrm.famoney.accounts.api.MovementDataDTO
import com.hrrm.famoney.domain.accounts.Account
import com.hrrm.famoney.domain.accounts.AccountRepository
import com.hrrm.famoney.domain.accounts.movement.Entry
import com.hrrm.famoney.domain.accounts.movement.Movement
import com.hrrm.famoney.domain.accounts.movement.MovementRepository
import mu.KotlinLogging
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.math.BigDecimal
import javax.persistence.EntityManager
import javax.persistence.LockModeType

@Service
class MovementApiService(
    private val accountRepository: AccountRepository,
    private val movementRepository: MovementRepository,
    private val entryApiService: EntryApiService,
    private val entityManager: EntityManager,
    private val objectMapper: ObjectMapper,
) {
    private val logger = KotlinLogging.logger { }

    @Transactional
    fun addMovement(account: Account, movementDataDTO: MovementDataDTO): Movement {
        logger.debug { "Creating movement in account by id: ${account.id}." }
        logger.trace {
            """Creating movement in account by id: ${account.id}..
              |${objectMapper.writeValueAsString(movementDataDTO)}""".trimMargin()
        }
        val movement: Movement = callAddMovement(account, movementDataDTO)
        logger.debug { "Movement with id: ${movement.id} is created in account by id: ${account.id}." }
        logger.trace {
            """Movement is created in account by id: ${account.id}.
              |${objectMapper.writeValueAsString(movement)}""".trimMargin()
        }
        return movement
    }

    private fun callAddMovement(account: Account, movementDataDTO: MovementDataDTO): Movement {
        var movement = if (movementDataDTO is EntryDataDTO) {
            entryApiService.createMovement(movementDataDTO)
        } else {
            val message: String = provideUnknownDtoMessage(movementDataDTO)
            logger.warn { message }
            throw UnknownMovementType(message)
        }
        entityManager.lock(account, LockModeType.PESSIMISTIC_WRITE)
        val newPosition = movementRepository.getLastPositionByAccountOnDate(movement, movementDataDTO.date)
        movement = movement.let { setMovementAttributes(it, movementDataDTO) }.apply {
            this.account = account
            position = newPosition
        }
        val addedMovement = adjustAccountMovements(movement)
        return addedMovement
    }

    private fun setMovementAttributes(
        movement: Movement,
        movementDataDTO: MovementDataDTO
    ) = movement.apply {
        date = movementDataDTO.date
        bookingDate = movementDataDTO.bookingDate
        budgetPeriod = movementDataDTO.budgetPeriod
    }

    private fun adjustAccountMovements(movement: Movement): Movement {
        val account = movement.account.apply {
            movementCount++
            movementTotal += movement.amount
        }
        accountRepository.save(account)
        movementRepository.adjustMovementPositionsAndSumsByAccountAfterPosition(movement)
        val totalBeforeMovement = movementRepository.findNextMovementByAccountIdBeforePosition(movement)
            ?.let(Movement::total)
            ?: BigDecimal.ZERO
        return movement.apply {
            this.account = account
            total = totalBeforeMovement + amount
        }.let(movementRepository::save)
            .also { movementRepository.flush() }
    }

    @Transactional
    fun updateMovement(movement: Movement, movementDataDTO: MovementDataDTO): Movement {
        logger.debug { "Updateing movement by id: ${movement.id}." }
        logger.trace {
            """Updateing movement by id: ${movement.id}.
                       |${objectMapper.writeValueAsString(movementDataDTO)}""".trimMargin()
        }
        entityManager.lock(movement.account, LockModeType.PESSIMISTIC_WRITE)
        val positionBefore = movement.position
        rollbackAccount(movement)
        val positionAfter =
            if (movement.date != movementDataDTO.date) {
                movementRepository.getLastPositionByAccountOnDate(movement, movementDataDTO.date)
            } else positionBefore
        var updatedMovement = if (movementDataDTO is EntryDataDTO && movement is Entry) {
            entryApiService.updateMovement(movement, movementDataDTO)
        } else {
            val message = provideIncompatibleEntityAndDtoMessage(movement, movementDataDTO)
            logger.warn(message)
            throw IncompatibleMovementType(message)
        }
        updatedMovement = setMovementAttributes(updatedMovement, movementDataDTO).apply { position = positionAfter }
            .let(::adjustAccountMovements)
        logger.debug { "Movement is updated by id ${updatedMovement.id}." }
        logger.trace {
            """Movement is updated.
              |${objectMapper.writeValueAsString(movement)}""".trimMargin()
        }
        return updatedMovement
    }

    private fun rollbackAccount(movement: Movement) {
        val positionBefore = movement.position
        movement.position = -1
        val account = movement.account
            .apply {
                movementCount--
                movementTotal -= movement.amount
            }
        movement.account = accountRepository.save(account)
        movementRepository.rollbackMovementPositionsAndSumsByAccountAfterPosition(movement)
    }

    companion object {
        private fun <T : Movement, P : MovementDataDTO> provideIncompatibleEntityAndDtoMessage(
            movement: T,
            movementDTO: P
        ) =
            "Movement entity class [${movement::class.qualifiedName}] for account by id: ${movement.account.id} " +
                    "and movement id: ${movement.id}] are incompatible with DTO class [${movementDTO::class.qualifiedName}]."

        private fun <T : MovementDataDTO> provideUnknownDtoMessage(movementDTO: T) =
            "Unknown DTO class [${movementDTO::class.qualifiedName}]."
    }
}