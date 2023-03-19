package io.github.herrromich.famoney.domain

import jakarta.persistence.*

@MappedSuperclass
abstract class DomainEntity {
    @Id
    @Column(name = "id")
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    val id: Int? = null

    companion object {
        const val SCHEMA_NAME = "accounts"
    }

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as DomainEntity

        if (id == null || other.id == null || id != other.id) return false

        return true
    }

    override fun hashCode(): Int {
        return id ?: 0
    }
}