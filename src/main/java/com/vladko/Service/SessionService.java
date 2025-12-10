package com.vladko.Service;

import com.vladko.Entity.Session;
import com.vladko.Entity.User;
import com.vladko.Repositories.SessionRepository;
import com.vladko.Repositories.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.UUID;


@Component
public class SessionService {
    private final SessionRepository sessionRepository;

    public SessionService(SessionRepository sessionRepository) {
        this.sessionRepository = sessionRepository;
    }

    public UUID createSession(User user) {
        Session session = new Session();
        session.setUser(user);
        session.setExpiresAt(Instant.now().plus(1, ChronoUnit.HOURS));

        sessionRepository.save(session);

        return session.getId();
    }
}
