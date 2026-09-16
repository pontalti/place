package com.demo.place.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.Repository;
import org.springframework.data.repository.query.Param;
import org.springframework.transaction.annotation.Transactional;

import com.demo.place.entity.Place;

@Transactional(readOnly = true)
public interface PlaceReadOnlyRepository extends Repository<Place, Long> {

    @EntityGraph(value = "Place.withDays")
    public List<Place> findAll();

    @EntityGraph(value = "Place.withDays")
    public Optional<Place> findById(Long id);

    public boolean existsById(Long id);

    /**
     * Pages over the ids alone — no join, so the database can apply the LIMIT
     * correctly. With a fetch join the limit would count join rows instead of
     * places, and Hibernate would fall back to paging the whole table in
     * memory (HHH90003004).
     */
    @Query("SELECT p.id FROM Place p")
    public Page<Long> findPlaceIds(Pageable pageable);

    /**
     * Second step: loads that page's places with their days in one query.
     *
     * <p>The Sort has to be passed along — an IN clause carries no ordering, so
     * without it the page comes back shuffled even though the first query was
     * sorted.
     */
    @EntityGraph(value = "Place.withDays")
    @Query("SELECT p FROM Place p WHERE p.id IN :ids")
    public List<Place> findAllByIdIn(@Param("ids") List<Long> ids, Sort sort);
}