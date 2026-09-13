package com.demo.place.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.demo.place.entity.Place;
import com.demo.place.records.DayIntervalRecord;

public interface PlaceRepository extends JpaRepository<Place, Long> {
	
	@Query("""
		    SELECT new com.demo.place.records.DayIntervalRecord(
		        d.dayOfWeek, d.startTime, d.endTime
		    )
		    FROM DayOpening d
		    WHERE d.place.id = :placeId
		    ORDER BY d.dayOfWeek, d.startTime
		""")
	public List<DayIntervalRecord> findGroupedOpeningsByPlaceId(@Param("placeId") Long placeId);
	
	@Override
	@EntityGraph(value = "Place.withDays")
	public List<Place> findAll();

	@Override
	@EntityGraph(value = "Place.withDays")
	public Optional<Place> findById(Long id);
	
}