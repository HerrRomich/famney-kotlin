package io.github.herrromich.famoney.accounts.internal

import com.fasterxml.jackson.databind.ObjectMapper
import io.github.herrromich.famoney.accounts.api.dto.EntryDataDTO
import io.github.herrromich.famoney.accounts.api.dto.MovementDataDTO
import io.github.herrromich.famoney.domain.accounts.Account
import io.github.herrromich.famoney.domain.accounts.AccountRepository
import io.github.herrromich.famoney.domain.accounts.movement.Entry
import io.github.herrromich.famoney.domain.accounts.movement.Movement
import io.github.herrromich.famoney.domain.accounts.movement.MovementRepository
import jakarta.persistence.EntityManager
import jakarta.persistence.LockModeType
import mu.KotlinLogging
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.math.BigDecimal

private val logger = KotlinLogging.logger { }

@Service
class MovementApiService(
    private val accountRepository: AccountRepository,
    private val movementRepository: MovementRepository,
    private val entryApiService: EntryApiService,
    private val entityManager: EntityManager,
    private val objectMapper: ObjectMapper,
) {
    @Transactional
    fun createMovement(
        account: Account,
        movementDataDTO: MovementDataDTO,
    ): Movement {
        // region logging before
        logger.debug { "Creating movement in account by id: ${account.id}." }
        logger.trace {
            """Creating movement in account by id: ${account.id}..
              |${objectMapper.writeValueAsString(movementDataDTO)}"""
                .trimMargin()
        }
        // endregion

        var movement = if (movementDataDTO is EntryDataDTO) {
            entryApiService.createMovement(movementDataDTO)
        } else {
            throw UnsupportedMovementTypeException(movementDataDTO)
        }
        entityManager.lock(account, LockModeType.PESSIMISTIC_WRITE)
        movement.account = account
        movement.position = Float.MAX_VALUE
        val addedMovement = movementRepository.save(movement)
        val startTotal = movementRepository.getTotalBeforeDate(
            account.id!!,
            movement.date
        ) ?: BigDecimal.ZERO
        movementRepository.updateTotalAndPos(account.id!!, movement.date, startTotal)
        entityManager.refresh(addedMovement)

        // region logging after
        logger.debug { "Movement with id: ${addedMovement.id} is created in account by id: ${account.id}." }
        logger.trace {
            """Movement is created in account by id: ${account.id}.
              |${objectMapper.writeValueAsString(addedMovement)}""".trimMargin()
        }
        // endregion

        return addedMovement
    }

    @Transactional
    fun updateMovement(movement: Movement, movementDataDTO: MovementDataDTO): Movement {
        // region logging before
        logger.debug { "Updating movement by id: ${movement.id}." }
        logger.trace {
            """Updating movement by id: ${movement.id}.
                       |${objectMapper.writeValueAsString(movementDataDTO)}""".trimMargin()
        }
        // endregion

        entityManager.lock(movement.account, LockModeType.PESSIMISTIC_WRITE)
        var fromDate = movement.date
        var updatedMovement = if (movementDataDTO is EntryDataDTO && movement is Entry) {
            entryApiService.updateMovement(movement, movementDataDTO)
        } else {
            throw IncompatibleMovementTypeException(movement, movementDataDTO)
        }
        updatedMovement.position -= 0.5f
        updatedMovement = movementRepository.save(movement)
        fromDate = if (fromDate.isBefore(updatedMovement.date)) fromDate else updatedMovement.date
        val startTotal = movementRepository.getTotalBeforeDate(
            movement.account.id!!,
            fromDate
        ) ?: BigDecimal.ZERO
        movementRepository.updateTotalAndPos(movement.account.id!!, fromDate, startTotal)
        entityManager.refresh(updatedMovement)

        logger.debug { "Movement is updated by id ${updatedMovement.id}." }
        logger.trace {
            """Movement is updated.
              |${objectMapper.writeValueAsString(movement)}""".trimMargin()
        }
        return updatedMovement;
    }

    companion object {
        private fun <T : Movement, P : MovementDataDTO> IncompatibleMovementTypeException(
            movement: T,
            movementDTO: P
        ): IncompatibleMovementTypeException {
            val message =
                "Movement entity class [${movement::class.qualifiedName}] for account by id: ${movement.account.id} " +
                        "and movement id: ${movement.id}] are incompatible with DTO class [${movementDTO::class.qualifiedName}]."
            logger.warn(message)
            return IncompatibleMovementTypeException(message)
        }

        private fun <T : MovementDataDTO> UnsupportedMovementTypeException(movementDTO: T): UnsupportedMovementTypeException {
            val message = "Unknown DTO class [${movementDTO::class.qualifiedName}]."
            logger.warn { message }
            return UnsupportedMovementTypeException(message)
        }
    }
}
