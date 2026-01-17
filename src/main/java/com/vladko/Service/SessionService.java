package com.vladko.Service;

import com.vladko.Entity.Session;
import com.vladko.Entity.User;
import com.vladko.Repositories.SessionRepository;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.Optional;
import java.util.UUID;

@Service
public class SessionService {
    private final SessionRepository sessionRepository;

    public SessionService(SessionRepository sessionRepository) {
        this.sessionRepository = sessionRepository;
    }

    public UUID createSession(User user) {
        Session session = Session.builder()
                .user(user)
                .expiresAt(Instant.now().plus(1, ChronoUnit.HOURS))
                .build();

        sessionRepository.save(session);

        return session.getId();
    }

    public Optional<Session> findByToken(UUID token) {
        return sessionRepository.findById(token)
                .filter(session -> !isExpired(session));
    }

    public void deleteSession(UUID token) {
        sessionRepository.delete(token);
    }

    public boolean isExpired(Session session) {
        return session.getExpiresAt().isBefore(Instant.now());
    }

    public void deleteExpiredSessions() {
        sessionRepository.deleteExpiredSessions();
    }
}
