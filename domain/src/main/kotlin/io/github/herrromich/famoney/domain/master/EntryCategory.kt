package io.github.herrromich.famoney.domain.master

import io.github.herrromich.famoney.domain.DomainEntity
import jakarta.persistence.*
import org.eclipse.persistence.annotations.BatchFetch
import org.eclipse.persistence.annotations.BatchFetchType

@Entity
@Table(name = "entry_category")
@Inheritance(strategy = InheritanceType.SINGLE_TABLE)
@DiscriminatorColumn(name = "type")
abstract class EntryCategory<T : EntryCategory<T>> : DomainEntity() {
    @Column(name = "name")
    lateinit var name: String

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "parent_id")
    @BatchFetch(value = BatchFetchType.IN)
    var parent: T? = null

    @OneToMany(mappedBy = "parent", fetch = FetchType.LAZY)
    @BatchFetch(value = BatchFetchType.IN)
    lateinit var children: List<T>
}
