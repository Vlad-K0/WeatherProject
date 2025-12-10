package com.vladko.Repositories;

import com.vladko.Entity.BaseEntity;

import lombok.Cleanup;
import lombok.RequiredArgsConstructor;
import org.hibernate.Session;
import org.hibernate.SessionFactory;

import javax.persistence.criteria.CriteriaQuery;
import java.io.Serializable;
import java.util.List;
import java.util.Optional;

@RequiredArgsConstructor
public abstract class BaseRepository<K extends Serializable, E extends BaseEntity<K>> implements Repository<K, E> {

    private final Class<E> entityClass;
    protected final SessionFactory sessionFactory;

    @Override
    public E save(E entity) {
        @Cleanup
        Session session = sessionFactory.openSession();
        session.beginTransaction();
        session.save(entity);
        session.getTransaction().commit();
        return entity;
    }

    @Override
    public void delete(K id) {
        @Cleanup
        Session session = sessionFactory.openSession();
        session.beginTransaction();
        E entity = session.get(entityClass, id);
        if (entity != null) {
            session.delete(entity);
        }
        session.getTransaction().commit();
    }

    @Override
    public void update(E entity) {
        @Cleanup
        Session session = sessionFactory.openSession();
        session.beginTransaction();
        session.update(entity);
        session.getTransaction().commit();
    }

    @Override
    public Optional<E> findById(K id) {
        @Cleanup
        Session session = sessionFactory.openSession();
        return Optional.ofNullable(session.find(entityClass, id));
    }

    @Override
    public List<E> findAll() {
        @Cleanup
        Session session = sessionFactory.openSession();
        CriteriaQuery<E> criteria = session.getCriteriaBuilder().createQuery(entityClass);
        criteria.from(entityClass);

        return session.createQuery(criteria)
                .getResultList();

    }
}
