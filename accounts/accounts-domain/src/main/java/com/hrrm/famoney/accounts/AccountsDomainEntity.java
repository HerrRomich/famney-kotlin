package com.hrrm.famoney.accounts;

import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.ToString;
import org.jetbrains.annotations.NotNull;
import org.springframework.data.domain.Persistable;

import javax.persistence.*;

@MappedSuperclass
@EqualsAndHashCode
@Getter
@ToString
public abstract class AccountsDomainEntity implements Persistable<Integer> {

    public static final String SCHEMA_NAME = "accounts";

    @Id
    @Column(name = "id")
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @NotNull
    private Integer id;

    @Override
    public boolean isNew() {
        return id == null;
    }

}
