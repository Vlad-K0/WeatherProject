package com.vladko.Controllers;

import com.vladko.Entity.Session;
import com.vladko.Entity.User;
import com.vladko.Service.SessionService;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;

import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.Arrays;
import java.util.Optional;
import java.util.UUID;

@Component
public class AuthenticationInterceptor implements HandlerInterceptor {
    private static final String SESSION_COOKIE_NAME = "SESSION_ID";
    public static final String CURRENT_USER_ATTR = "currentUser";

    private final SessionService sessionService;

    public AuthenticationInterceptor(SessionService sessionService) {
        this.sessionService = sessionService;
    }

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler)
            throws Exception {
        Optional<UUID> sessionIdOpt = getSessionIdFromCookies(request);

        if (sessionIdOpt.isPresent()) {
            Optional<Session> sessionOpt = sessionService.findByToken(sessionIdOpt.get());
            if (sessionOpt.isPresent()) {
                User user = sessionOpt.get().getUser();
                request.setAttribute(CURRENT_USER_ATTR, user);
                return true;
            }
        }

        response.sendRedirect(request.getContextPath() + "/auth/login");
        return false;
    }

    private Optional<UUID> getSessionIdFromCookies(HttpServletRequest request) {
        Cookie[] cookies = request.getCookies();
        if (cookies == null) {
            return Optional.empty();
        }

        return Arrays.stream(cookies)
                .filter(c -> SESSION_COOKIE_NAME.equals(c.getName()))
                .findFirst()
                .flatMap(cookie -> {
                    try {
                        return Optional.of(UUID.fromString(cookie.getValue()));
                    } catch (IllegalArgumentException e) {
                        return Optional.empty();
                    }
                });
    }
}
