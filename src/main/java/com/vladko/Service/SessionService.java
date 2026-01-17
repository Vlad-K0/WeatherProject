package com.vladko.Service;

import com.vladko.DTO.UserDTO;
import com.vladko.Entity.Session;
import com.vladko.Entity.User;
import com.vladko.Repositories.SessionRepository;
import com.vladko.Repositories.UserRepository;
import org.springframework.stereotype.Component;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.Optional;
import java.util.UUID;

@Service
public class SessionService {
    private final SessionRepository sessionRepository;
    private final UserRepository userRepository;

    public SessionService(SessionRepository sessionRepository, UserRepository userRepository) {
        this.sessionRepository = sessionRepository;
        this.userRepository = userRepository;
    }

    public UUID createSession(UserDTO user) {
        Session session = new Session();
        Optional<User> userFind = userRepository.findByUsername(user.getLogin());
        userFind.ifPresent(session::setUser);

        session.setExpiresAt(Instant.now().plus(1, ChronoUnit.HOURS));
        sessionRepository.save(session);

        return session.getId();
    }

    public Optional<Session> findSessionByID(String sessionID) {
        UUID uuid = UUID.fromString(sessionID);
        Optional<Session> session = sessionRepository.findById(uuid);
        if (session.isPresent()) {
            if (Instant.now().isAfter(session.get().getExpiresAt())) {
                deleteExpiredSessions();
                return Optional.empty();
            }
            return session;
        }
        return Optional.empty();
    }

    public void deleteExpiredSessions() {
        sessionRepository.deleteExpiredSessions();
    }

    public void deleteSessionsByID(String sessionID) {
        sessionRepository.delete(UUID.fromString(sessionID));
    }


}
