package com.demo.place.repository;

import java.util.List;
import java.util.Optional;

import com.demo.place.entity.Place;
import com.demo.place.records.DayIntervalRecord;
import com.demo.place.records.PageRequest;

import io.quarkus.hibernate.orm.panache.PanacheRepositoryBase;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.persistence.EntityManager;

/**
 * Overrides the Panache defaults instead of adding new method names, so the
 * service keeps calling listAll()/findById() and there is no fetch-less path
 * left for someone to pick up by accident.
 */
@ApplicationScoped
public class PlaceRepository implements PanacheRepositoryBase<Place, Long> {

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

    public long countAll() {
        return getEntityManager()
                .createQuery("SELECT COUNT(p) FROM Place p", Long.class)
                .getSingleResult();
    }

    /**
     * Page over the ids alone.
     *
     * <p>Deliberately without the fetch graph: a join against day_opening would
     * make the LIMIT count join rows instead of places, so a page of 20 would
     * come back with three places that happen to have many opening slots.
     * Hibernate works around that by paging the whole result in memory and
     * logs HHH90003004 — correct, but it reads the entire table to do it.
     */
    public List<Long> findPageOfIds(PageRequest page) {
        return getEntityManager()
                .createQuery("SELECT p.id FROM Place p" + page.orderByClause("p"), Long.class)
                .setFirstResult(page.offset())
                .setMaxResults(page.size())
                .getResultList();
    }

    /**
     * Second step: load that page's places with their days in one query.
     *
     * <p>The ORDER BY is repeated here on purpose — an IN clause carries no
     * ordering, so without it the page comes back shuffled even though the
     * first query was sorted.
     */
    public List<Place> findAllByIdIn(List<Long> ids, PageRequest page) {
        if (ids.isEmpty()) {
            return List.of();
        }
        EntityManager em = getEntityManager();
        return em.createQuery(
                        "SELECT p FROM Place p WHERE p.id IN :ids" + page.orderByClause("p"),
                        Place.class)
                .setHint(FETCH_GRAPH, em.getEntityGraph(PLACE_WITH_DAYS))
                .setParameter("ids", ids)
                .getResultList();
    }
}