package com.twilio.taskrouter.domain.repository;

import com.twilio.taskrouter.domain.model.MissedCall;

import javax.inject.Inject;
import javax.persistence.EntityManager;
import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.Root;
import java.util.Arrays;
import java.util.List;

/**
 * Handle {@link MissedCall} entities in the database
 */
public class MissedCallRepository {

  private final CriteriaBuilder criteriaBuilder;

  private EntityManager entityManager;

  @Inject
  public MissedCallRepository(EntityManager entityManager) {
    this.entityManager = entityManager;
    this.criteriaBuilder = entityManager.getCriteriaBuilder();
  }

  /**
   * Add multiple entries wrapped in a {@link Iterable<MissedCall>} element
   *
   * @param entries {@link Iterable<MissedCall>} not <code>null<code>
   */
  public void addAll(Iterable<MissedCall> entries) {
    entityManager.getTransaction().begin();
    entries.forEach(entityManager::persist);
    entityManager.getTransaction().commit();
  }

  /**
   * Add one or more entries of {@link MissedCall}
   *
   * @param entries Instances of {@link MissedCall} to add
   */
  public void add(MissedCall... entries) {
    addAll(Arrays.asList(entries));
  }

  /**
   * Retrieve all available entries of {@link MissedCall}
   *
   * @return A {@link List<MissedCall>} not <code>null</code>
   */
  public List<MissedCall> getAll() {
    CriteriaQuery<MissedCall> query = criteriaBuilder.createQuery(MissedCall.class);
    Root<MissedCall> root = query.from(MissedCall.class);
    CriteriaQuery<MissedCall> select = query.select(root)
      .orderBy(criteriaBuilder.desc(root.get("created")));
    return entityManager.createQuery(select).getResultList();
  }
}
