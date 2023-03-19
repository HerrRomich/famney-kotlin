package io.github.herrromich.famoney.domain.accounts.movement

import org.eclipse.persistence.annotations.JoinFetch
import org.eclipse.persistence.annotations.JoinFetchType
import jakarta.persistence.*

@Entity
@DiscriminatorValue(Entry.TYPE)
class Entry : Movement() {
    @ElementCollection
    @CollectionTable(name = "entry_item", joinColumns = [JoinColumn(name = "entry_id")])
    @JoinFetch(JoinFetchType.INNER)
    val entryItems: List<EntryItem> = mutableListOf()

    companion object {
        const val TYPE = "ENTRY"
    }
}