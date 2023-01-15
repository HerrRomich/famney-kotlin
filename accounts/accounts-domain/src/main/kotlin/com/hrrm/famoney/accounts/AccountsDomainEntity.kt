package com.hrrm.famoney.accounts

import lombok.EqualsAndHashCode
import lombok.Getter
import lombok.ToString
import org.springframework.data.domain.Persistable
import javax.persistence.*
import kotlin.properties.Delegates

@MappedSuperclass
abstract class AccountsDomainEntity {
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

        other as AccountsDomainEntity

        if (id == null || other.id == null || id != other.id) return false

        return true
    }

    override fun hashCode(): Int {
        return id ?: 0
    }
}