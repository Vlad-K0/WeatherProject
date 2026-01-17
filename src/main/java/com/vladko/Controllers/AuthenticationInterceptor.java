package com.vladko.Controllers;

import com.vladko.Entity.Session;
import com.vladko.Service.SessionService;
import com.vladko.Utils.CookieUtils;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.Optional;

@Component
public class AuthenticationInterceptor implements HandlerInterceptor {

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

}
