package com.demo.place.repository;

import java.util.List;
import java.util.Optional;

import org.hibernate.jpa.HibernateHints;

import com.demo.place.entity.Place;
import com.demo.place.records.DayIntervalRecord;
import com.demo.place.records.PageRequest;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.persistence.EntityManager;

/**
 * Read side of the Place aggregate.
 *
 * <p>Deliberately not a {@code PanacheRepositoryBase}. Panache ships persist and
 * delete as default methods on its base interface, and unlike Spring Data —
 * where extending the bare {@code Repository<T, ID>} exposes only what the
 * interface declares — there is no way to hide them. Working on the
 * EntityManager directly is what makes "read-only" hold here.
 *
 * <p>Every entity query carries {@link HibernateHints#HINT_READ_ONLY}. It is the
 * closest thing to Spring's {@code @Transactional(readOnly = true)}, which
 * {@code jakarta.transaction.Transactional} does not have: Hibernate loads the
 * entities without keeping a snapshot for dirty checking, so a change made to
 * one by mistake is never flushed.
 */
@ApplicationScoped
public class PlaceReadOnlyRepository {

    private static final String PLACE_WITH_DAYS = "Place.withDays";

    /**
     * fetchgraph, not loadgraph: it loads exactly what the graph declares and
     * treats everything else as LAZY, regardless of the mapping.
     */
    private static final String FETCH_GRAPH = "jakarta.persistence.fetchgraph";

    @Inject
    private EntityManager em;

    /**
     * Lists every place with its opening hours.
     *
     * <p>The fetch graph is what removes the N+1: without it, listing N places
     * issued one select for the places and one per place for its days.
     */
    public List<Place> listAll() {
        return this.em.createQuery("SELECT p FROM Place p", Place.class)
                .setHint(FETCH_GRAPH, this.em.getEntityGraph(PLACE_WITH_DAYS))
                .setHint(HibernateHints.HINT_READ_ONLY, true)
                .getResultList();
    }

    public Optional<Place> findById(Long id) {
        return this.em.createQuery("SELECT p FROM Place p WHERE p.id = :id", Place.class)
                .setHint(FETCH_GRAPH, this.em.getEntityGraph(PLACE_WITH_DAYS))
                .setHint(HibernateHints.HINT_READ_ONLY, true)
                .setParameter("id", id)
                .getResultStream()
                .findFirst();
    }

    public boolean existsById(Long id) {
        return this.em.createQuery("SELECT COUNT(p) FROM Place p WHERE p.id = :id", Long.class)
                .setParameter("id", id)
                .getSingleResult() > 0;
    }

    public long countAll() {
        return this.em.createQuery("SELECT COUNT(p) FROM Place p", Long.class)
                .getSingleResult();
    }

    /**
     * Pages over the ids alone.
     *
     * <p>Deliberately without the fetch graph: a join against day_opening would
     * make the LIMIT count join rows instead of places, so a page of 20 would
     * come back with three places that happen to have many opening slots.
     * Hibernate works around that by paging the whole result in memory and logs
     * HHH90003004 — correct, but it reads the entire table to do it.
     */
    public List<Long> findPageOfIds(PageRequest page) {
        return this.em
                .createQuery("SELECT p.id FROM Place p" + page.orderByClause("p"), Long.class)
                .setFirstResult(page.offset())
                .setMaxResults(page.size())
                .getResultList();
    }

    /**
     * Second step: loads that page's places with their days in one query.
     *
     * <p>The ORDER BY is repeated here on purpose — an IN clause carries no
     * ordering, so without it the page comes back shuffled even though the
     * first query was sorted.
     */
    public List<Place> findAllByIdIn(List<Long> ids, PageRequest page) {
        if (ids.isEmpty()) {
            return List.of();
        }
        return this.em.createQuery(
                        "SELECT p FROM Place p WHERE p.id IN :ids" + page.orderByClause("p"),
                        Place.class)
                .setHint(FETCH_GRAPH, this.em.getEntityGraph(PLACE_WITH_DAYS))
                .setHint(HibernateHints.HINT_READ_ONLY, true)
                .setParameter("ids", ids)
                .getResultList();
    }

    /**
     * The intervals of a place, ordered by day and start time.
     *
     * <p>A constructor projection, so no entity is managed and neither the graph
     * nor the read-only hint applies. The Spring branch dropped its equivalent:
     * findById already brings the days along, which makes this a second round
     * trip for data already in hand. Remove it once GroupPlaceServiceImpl reads
     * the days from the Place the same way.
     */
    public List<DayIntervalRecord> findGroupedOpeningsByPlaceId(Long placeId) {
        return this.em.createQuery("""
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