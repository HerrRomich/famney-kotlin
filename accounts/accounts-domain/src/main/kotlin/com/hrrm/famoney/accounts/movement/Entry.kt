package com.hrrm.famoney.accounts.movement

import org.eclipse.persistence.annotations.JoinFetch
import org.eclipse.persistence.annotations.JoinFetchType
import javax.persistence.*

@Entity
@DiscriminatorValue("entry")
class Entry : Movement() {
    @ElementCollection
    @CollectionTable(name = "entry_item", joinColumns = [JoinColumn(name = "entry_id")])
    @JoinFetch(JoinFetchType.INNER)
    val entryItems: List<EntryItem> = mutableListOf()
}