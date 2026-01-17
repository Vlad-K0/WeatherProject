package com.vladko.Controllers;

import com.vladko.Entity.Session;
import com.vladko.Service.SessionService;
import com.vladko.Utils.CookieUtils;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;

import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.Optional;

@Component
public class AuthenticationInterceptor implements HandlerInterceptor {
    private static final String SESSION_COOKIE_NAME = "SESSION_ID";
    public static final String CURRENT_USER_ATTR = "currentUser";

    private final SessionService sessionService;

    public AuthenticationInterceptor(SessionService service) {
        this.sessionService = service;
    }

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler)
            throws Exception {
        String cookieString = CookieUtils.getSessionIdFromCookie(request);

        if (cookieString == null) {
            response.sendRedirect(request.getContextPath() + "/auth/login");
            return false;
        }

        Optional<Session> findSession = sessionService.findSessionByID(cookieString);
        if (findSession.isPresent()) {
            Session currentSession = findSession.get();
            request.setAttribute("currentUser", currentSession.getUser());
            return true;
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
