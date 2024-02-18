package io.github.herrromich.famoney.domain.accounts.movement

import jakarta.persistence.*
import org.eclipse.persistence.annotations.JoinFetch
import org.eclipse.persistence.annotations.JoinFetchType

@Entity
@DiscriminatorValue(Entry.TYPE)
open class Entry : Movement() {
    @ElementCollection
    @CollectionTable(name = "entry_item", joinColumns = [JoinColumn(name = "entry_id")])
    @JoinFetch(JoinFetchType.INNER)
    val entryItems: List<EntryItem> = mutableListOf()

    companion object {
        const val TYPE = "ENTRY"
    }
}
