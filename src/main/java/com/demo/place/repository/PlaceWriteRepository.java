package com.demo.place.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.repository.Repository;
import org.springframework.transaction.annotation.Transactional;

import com.demo.place.entity.Place;

public interface PlaceWriteRepository extends Repository<Place, Long> {

	public <S extends Place> S save(S entity);

    public <S extends Place> List<S> saveAll(Iterable<S> entities);

    public void delete(Place entity);

    public void deleteById(Long id);

    /**
     * Existence check that precedes a delete.
     *
     * <p>{@code readOnly} applies when this runs on its own; called from a
     * service method that already opened a write transaction, the outer one
     * wins, which is exactly the shared transaction this method is here for.
     */
    @Transactional(readOnly = true)
    public boolean existsById(Long id);
    
    /**
     * Loads an aggregate that is about to be modified by an update or a patch.
     *
     * <p>The entity graph brings the opening hours along: both callers go on to
     * replace or merge that collection, and without the fetch the first access
     * would either issue a second query or fail outright once the session is
     * closed.
     */
    @Transactional(readOnly = true)
    @EntityGraph(value = "Place.withDays")
    public Optional<Place> findById(Long id);
}