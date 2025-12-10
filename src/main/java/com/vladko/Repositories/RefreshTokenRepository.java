package com.vladko.Repositories;

import com.vladko.Entity.RefreshToken;
import com.vladko.Entity.User;
import lombok.Cleanup;
import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.hibernate.Transaction;
import org.springframework.stereotype.Component;

import java.time.Instant;
import java.util.Optional;
import java.util.UUID;

@Component
public class RefreshTokenRepository extends BaseRepository<UUID, RefreshToken> {

    public RefreshTokenRepository(SessionFactory sessionFactory) {
        super(RefreshToken.class, sessionFactory);
    }

    public Optional<RefreshToken> findByToken(String token) {
        @Cleanup
        Session session = sessionFactory.openSession();
        return session.createQuery(
                "from refresh_tokens rt where rt.token = :token and rt.revoked = false",
                RefreshToken.class)
                .setParameter("token", token)
                .uniqueResultOptional();
    }

    public void revokeAllUserTokens(User user) {
        @Cleanup
        Session session = sessionFactory.openSession();
        Transaction tx = session.beginTransaction();
        session.createQuery("update refresh_tokens rt set rt.revoked = true where rt.user = :user")
                .setParameter("user", user)
                .executeUpdate();
        tx.commit();
    }

    public void deleteExpiredTokens() {
        @Cleanup
        Session session = sessionFactory.openSession();
        Transaction tx = session.beginTransaction();
        session.createQuery("delete from refresh_tokens rt where rt.expiresAt < :now or rt.revoked = true")
                .setParameter("now", Instant.now())
                .executeUpdate();
        tx.commit();
    }
}
