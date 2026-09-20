package com.demo.place.repository;

import java.util.List;
import java.util.Optional;

import com.demo.place.entity.Place;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.persistence.EntityManager;
import jakarta.transaction.Transactional;
import jakarta.transaction.Transactional.TxType;

/**
 * Write side of the Place aggregate.
 *
 * <p>Every method requires a transaction the caller has already opened
 * ({@link TxType#MANDATORY}). A service method that forgets its
 * {@code @Transactional} now fails at once with a message naming this class,
 * instead of reaching persist and surfacing as the TransactionRequiredException
 * the batch endpoint hit before.
 *
 * <p>The two read methods live here rather than on
 * {@link PlaceReadOnlyRepository} because each is the first half of a write:
 * the caller reads, decides and writes within one service transaction.
 * Reading through the other repository would split the operation across two of
 * them, and its read-only hint would stop the loaded entity from being flushed.
 */
@ApplicationScoped
@Transactional(TxType.MANDATORY)
public class PlaceWriteRepository {

    private static final String PLACE_WITH_DAYS = "Place.withDays";

    /** See PlaceReadOnlyRepository — same graph, same reasoning. */
    private static final String FETCH_GRAPH = "jakarta.persistence.fetchgraph";

    @Inject
    private EntityManager em;

    /**
     * Inserts a new place or writes the changes to an existing one, and
     * returns the managed instance.
     *
     * <p>Three cases, in this order:
     * <ul>
     *   <li>already managed (loaded by {@link #findById} in this transaction):
     *       nothing to attach, only a flush so the slots added by an update or
     *       a patch get their ids before the service maps the response;</li>
     *   <li>no id: a new aggregate, persisted — the cascade inserts its days;</li>
     *   <li>an id but detached: merged, which returns the managed copy.</li>
     * </ul>
     *
     * <p>Calling merge on the first case too would look harmless but is not:
     * cascading merge onto the new, still transient slots creates managed
     * copies of them, and the originals the caller holds never get an id.
     */
    public Place save(Place place) {
        if (this.em.contains(place)) {
            this.em.flush();
            return place;
        }
        if (place.getId() == null) {
            this.em.persist(place);
            return place;
        }
        return this.em.merge(place);
    }

    /** Saves the whole batch in the caller's transaction: all rows or none. */
    public List<Place> save(List<Place> places) {
        return places.stream().map(this::save).toList();
    }

    public void delete(Place place) {
        this.em.remove(this.em.contains(place) ? place : this.em.merge(place));
    }

    /**
     * Removes a place and, through the cascade, its opening hours.
     *
     * <p>Goes through the entity rather than a bulk {@code DELETE} query: a bulk
     * delete skips the cascade and orphanRemoval, and would fail on the foreign
     * key from day_opening.
     *
     * @return whether a row was removed
     */
    public boolean deleteById(Long id) {
        Place place = this.em.find(Place.class, id);
        if (place == null) {
            return false;
        }
        this.em.remove(place);
        return true;
    }

    /** Existence check that precedes a delete, in the same transaction. */
    public boolean existsById(Long id) {
        return this.em.createQuery("SELECT COUNT(p) FROM Place p WHERE p.id = :id", Long.class)
                .setParameter("id", id)
                .getSingleResult() > 0;
    }

    /**
     * Loads an aggregate that is about to be modified by an update or a patch.
     *
     * <p>The fetch graph brings the opening hours along: both callers go on to
     * replace or merge that collection, and without the fetch the first access
     * would issue a second query. No read-only hint here — the point is for
     * Hibernate to track this instance and flush what changes.
     */
    public Optional<Place> findById(Long id) {
        return this.em.createQuery("SELECT p FROM Place p WHERE p.id = :id", Place.class)
                .setHint(FETCH_GRAPH, this.em.getEntityGraph(PLACE_WITH_DAYS))
                .setParameter("id", id)
                .getResultStream()
                .findFirst();
    }
}