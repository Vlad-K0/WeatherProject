package com.vladko.Repositories;

import com.vladko.Entity.Session;
import lombok.Cleanup;
import org.hibernate.SessionFactory;
import org.hibernate.Transaction;
import org.springframework.stereotype.Component;

import java.time.Instant;
import java.util.UUID;

@Component
public class SessionRepository extends BaseRepository<UUID, Session> {
    public SessionRepository(Class<Session> entityClass, SessionFactory sessionFactory) {
        super(entityClass, sessionFactory);
    }

    public void deleteExpiredSessions() {
        @Cleanup
        org.hibernate.Session session = sessionFactory.openSession();

        Transaction tx = session.beginTransaction();
        session.createQuery("delete from sessions s where s.expiresAt < :now")
                .setParameter("now", Instant.now())
                .executeUpdate();
        tx.commit();

    }
}
