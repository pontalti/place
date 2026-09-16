package com.demo.place.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
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

	/** Page over ids only — no join, so the database can apply LIMIT correctly. */
	@Query("SELECT p.id FROM Place p")
	public Page<Long> findPlaceIds(Pageable pageable);

	/** Second step: load the page's places with their days in one query. */
	@EntityGraph(value = "Place.withDays")
	@Query("SELECT p FROM Place p WHERE p.id IN :ids")
	public List<Place> findAllByIdIn(@Param("ids") List<Long> ids, Sort sort);

}