package com.demo.place.entity;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

import org.hibernate.annotations.DynamicInsert;
import org.hibernate.annotations.DynamicUpdate;
import org.hibernate.Hibernate;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.NamedAttributeNode;
import jakarta.persistence.NamedEntityGraph;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;

/**
 * The named graph is what {@code PlaceRepository} applies to avoid the N+1:
 * without it, listing N places issued one select for the places and one per
 * place for its opening hours.
 */
@Getter
@Setter
@Builder
@AllArgsConstructor
@NoArgsConstructor
@ToString(exclude = "days")
@Entity
@Table
@DynamicInsert
@DynamicUpdate
@NamedEntityGraph(
        name = "Place.withDays",
        attributeNodes = @NamedAttributeNode("days")
)
public class Place {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String label;
    private String location;

    @OneToMany(
            mappedBy = "place",
            cascade = CascadeType.ALL,
            orphanRemoval = true,
            fetch = FetchType.LAZY
    )
    @Builder.Default
    private List<DayOpening> days = new ArrayList<>();

    /** Keeps both sides of the association in sync. */
    public void replaceDays(List<DayOpening> newDays) {
        this.days.clear();
        if (newDays != null) {
            newDays.forEach(day -> {
                day.setPlace(this);
                this.days.add(day);
            });
        }
    }

    /**
     * Identity based on the id alone, with a constant hashCode.
     *
     * <p>Lombok's generated equals/hashCode would include {@code days}, which
     * forces a lazy collection to initialise on every comparison and changes
     * the hash once the id is assigned on persist — enough to lose the entity
     * inside a HashSet. The constant hash is the standard workaround: correct
     * before and after persist, at the cost of degrading hash buckets for the
     * handful of entities a single session holds.
     */
    @Override
    public final boolean equals(Object other) {
        if (this == other) {
            return true;
        }
        if (other == null) {
            return false;
        }
        if (!Hibernate.getClass(this).equals(Hibernate.getClass(other))) {
            return false;
        }
        Place that = (Place) other;
        return this.id != null && Objects.equals(this.id, that.id);
    }

    @Override
    public final int hashCode() {
        return Hibernate.getClass(this).hashCode();
    }

}