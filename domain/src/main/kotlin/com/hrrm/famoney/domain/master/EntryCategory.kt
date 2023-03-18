package com.hrrm.famoney.domain.master

import com.hrrm.famoney.domain.DomainEntity
import org.eclipse.persistence.annotations.BatchFetch
import org.eclipse.persistence.annotations.BatchFetchType
import javax.persistence.*

@Entity
@Table(name = "entry_category")
@Inheritance(strategy = InheritanceType.SINGLE_TABLE)
@DiscriminatorColumn(name = "type")
abstract class EntryCategory<T : EntryCategory<T>?> : DomainEntity() {
    @Column(name = "name")
    lateinit var name: String

    @get:ManyToOne(fetch = FetchType.EAGER)
    @get:JoinColumn(name = "parent_id")
    @get:BatchFetch(value = BatchFetchType.IN)
    var parent: T? = null

    @get:OneToMany(mappedBy = "parent", fetch = FetchType.LAZY)
    @get:BatchFetch(value = BatchFetchType.IN)
    lateinit var children: List<T>
}