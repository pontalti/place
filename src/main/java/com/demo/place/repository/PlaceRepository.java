package com.demo.place.repository;

import java.util.List;
import java.util.Optional;

import com.demo.place.entity.Place;
import com.demo.place.records.DayIntervalRecord;

import io.quarkus.hibernate.orm.panache.PanacheRepositoryBase;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.persistence.EntityManager;

@ApplicationScoped
public class PlaceRepository implements PanacheRepositoryBase<Place, Long>{
	
    private static final String PLACE_WITH_DAYS = "Place.withDays";
    
    /**
     * fetchgraph, not loadgraph: it loads exactly what the graph declares and
     * treats everything else as LAZY, regardless of the mapping.
     */
    private static final String FETCH_GRAPH = "jakarta.persistence.fetchgraph";
	
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
	
    @Override
    public List<Place> listAll() {
        EntityManager em = getEntityManager();
        return em.createQuery("SELECT p FROM Place p", Place.class)
                .setHint(FETCH_GRAPH, em.getEntityGraph(PLACE_WITH_DAYS))
                .getResultList();
    }
 
    @Override
    public Optional<Place> findByIdOptional(Long id) {
        EntityManager em = getEntityManager();
        return em.createQuery("SELECT p FROM Place p WHERE p.id = :id", Place.class)
                .setHint(FETCH_GRAPH, em.getEntityGraph(PLACE_WITH_DAYS))
                .setParameter("id", id)
                .getResultStream()
                .findFirst();
    }
 
    /** Kept in sync with the Optional variant so both entry points fetch the days. */
    @Override
    public Place findById(Long id) {
        return findByIdOptional(id).orElse(null);
    }
	
}