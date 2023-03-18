package com.hrrm.famoney.accounts.internal

import com.hrrm.famoney.accounts.api.EntryDataDTO
import com.hrrm.famoney.domain.accounts.movement.Entry
import com.hrrm.famoney.domain.accounts.movement.EntryItem
import com.hrrm.famoney.domain.accounts.movement.Movement
import org.springframework.stereotype.Component
import java.math.BigDecimal

@Component
class EntryApiService {
    fun updateMovement(entry: Entry, entryDataDTO: EntryDataDTO): Movement {
        return fillEntryAttributes(entryDataDTO, ::Entry)
    }

    fun createMovement(entryDataDTO: EntryDataDTO): Movement {
        return fillEntryAttributes(entryDataDTO, ::Entry)
    }

    private fun fillEntryAttributes(entryDataDTO: EntryDataDTO, getEntry: () -> Entry): Movement {
        val entry = getEntry()
        val entryItems = entry.entryItems.toMutableList()
        var i = 0
        val entryItemDTOs = entryDataDTO.entryItems
        while (i < entryItemDTOs.size) {
            var entryItem: EntryItem
            if (i < entryItems.size) {
                entryItem = entryItems[i]
            } else {
                entryItem = EntryItem().apply { position = i }
                entryItems.add(entryItem)
            }
            val entryItemDTO = entryItemDTOs[i]
            entryItem.apply {
                categoryId = entryItemDTO.categoryId
                amount = entryItemDTO.amount
                comments = entryItemDTO.comments
            }
            i++
        }
        while (i < entryItems.size) {
            entryItems.removeAt(entryItems.size - 1)
        }
        return entry.apply {
            amount = entryDataDTO.amount
            total = BigDecimal.ZERO
        }
    }
}