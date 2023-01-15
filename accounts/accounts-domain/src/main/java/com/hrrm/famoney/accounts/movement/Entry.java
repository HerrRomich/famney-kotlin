package com.hrrm.famoney.accounts.movement;

import lombok.*;
import lombok.experimental.Accessors;
import org.eclipse.persistence.annotations.JoinFetch;
import org.eclipse.persistence.annotations.JoinFetchType;
import org.jetbrains.annotations.NotNull;

import javax.persistence.*;
import java.util.List;

@Entity
@DiscriminatorValue("entry")
@Accessors(chain = true)
@Getter
@Setter
@ToString(callSuper = true)
@EqualsAndHashCode(
        onlyExplicitlyIncluded = true,
        callSuper = true
)
public class Entry extends Movement {

    @ElementCollection
    @CollectionTable(
            name = "entry_item",
            joinColumns = @JoinColumn(name = "entry_id")
    )
    @JoinFetch(JoinFetchType.INNER)
    @NotNull
    private List<EntryItem> entryItems;

}
