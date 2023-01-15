package com.hrrm.famoneys.accounts.internal

import com.hrrm.famoney.accounts.api.EntryDataDTO
import com.hrrm.famoney.accounts.movement.Entry
import com.hrrm.famoney.accounts.movement.EntryItem
import com.hrrm.famoney.accounts.movement.Movement
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
        val entryItems = entry.entryItems
        var i = 0
        val entryItemDTOs = entryDataDTO.entryItems
        while (i < entryItemDTOs.size) {
            var entryItem: EntryItem
            if (i < entryItems.size) {
                entryItem = entryItems.get(i)
            } else {
                entryItem = EntryItem().setPosition(i)
                entryItems.add(entryItem)
            }
            val entryItemDTO = entryItemDTOs[i]
            entryItem.setCategoryId(entryItemDTO.categoryId)
                .setAmount(entryItemDTO.amount)
                .setComments(entryItemDTO.comments)
            i++
        }
        while (i < entryItems.size) {
            entryItems.removeAt(entryItems.size - 1)
        }
        return entry.setAmount(entryDataDTO.amount)
            .setTotal(BigDecimal.ZERO)
    }
}