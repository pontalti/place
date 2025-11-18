package com.demo.place.repository;

import java.util.List;

import com.demo.place.entity.Place;
import com.demo.place.records.DayIntervalRecord;

import io.quarkus.hibernate.orm.panache.PanacheRepositoryBase;
import jakarta.enterprise.context.ApplicationScoped;

@ApplicationScoped
public class PlaceRepository implements PanacheRepositoryBase<Place, Long>{
	
	public List<DayIntervalRecord> findGroupedOpeningsByPlaceId(Long placeId) {
        return getEntityManager().createQuery("""
                SELECT new com.demo.place.records.DayIntervalRecord(
                    d.dayOfWeek, d.startTime, d.endTime
                )
                FROM DayOpening d
                WHERE d.place.id = :placeId
                ORDER BY d.dayOfWeek, d.startTime
            """, DayIntervalRecord.class)
            .setParameter("placeId", placeId)
            .getResultList();
    }
	
}