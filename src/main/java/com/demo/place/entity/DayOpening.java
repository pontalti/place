package com.demo.place.entity;

import java.time.DayOfWeek;
import java.util.Objects;

import org.hibernate.annotations.DynamicInsert;
import org.hibernate.annotations.DynamicUpdate;
import org.hibernate.Hibernate;

import com.demo.place.entity.Deserializer.DayOfWeekDeserializer;
import com.demo.place.entity.Serializer.DayOfWeekSerializer;
import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;

/**
 * The back-reference to {@code Place} is excluded from both toString and JSON:
 * Place -> days -> place -> days would recurse until the stack runs out.
 * @JsonIgnore covers serialisation, @ToString(exclude) covers logging.
 */
@Getter
@Setter
@Builder
@AllArgsConstructor
@NoArgsConstructor
@ToString(exclude = "place")
@Entity
@Table
@DynamicInsert
@DynamicUpdate
public class DayOpening {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @JsonFormat(shape = JsonFormat.Shape.NUMBER_INT)
    @JsonDeserialize(using = DayOfWeekDeserializer.class)
    @JsonSerialize(using = DayOfWeekSerializer.class)
    @Column(name = "day_of_week", nullable = false)
    private DayOfWeek dayOfWeek;

    @JsonIgnore
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "place_id", nullable = false)
    private Place place;

    @Column(name = "start_time", nullable = false)
    private String startTime;

    @Column(name = "end_time", nullable = false)
    private String endTime;

    @Column(name = "opening_type")
    private String type;

    /** See the note on Place#equals — same reasoning, same trade-off. */
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
        DayOpening that = (DayOpening) other;
        return this.id != null && Objects.equals(this.id, that.id);
    }

    @Override
    public final int hashCode() {
        return Hibernate.getClass(this).hashCode();
    }

}