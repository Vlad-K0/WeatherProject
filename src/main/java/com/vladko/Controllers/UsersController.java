package com.vladko.Controllers;

import com.vladko.DTO.AuthRequestDTO;
import com.vladko.DTO.UserDTO;
import com.vladko.Exceptions.IncorrectPasswordException;
import com.vladko.Service.SessionService;
import com.vladko.Service.UserService;
import com.vladko.Utils.CookieUtils;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.UUID;

@Controller
@RequestMapping("/auth")
public class UsersController {
    private static final String SESSION_COOKIE_NAME = "SESSION_ID";
    private static final int COOKIE_MAX_AGE = 3600; // 1 hour

    private final UserService userService;
    private final SessionService sessionService;

    public UsersController(UserService userService, SessionService sessionService) {
        this.userService = userService;
        this.sessionService = sessionService;
    }

    @GetMapping("register")
    public String showRegisterPage(Model model) {
        model.addAttribute("user", new AuthRequestDTO());
        return "authorization/sign-up";
    }

    @GetMapping("login")
    public String showLoginPage(Model model) {
        model.addAttribute("user", new AuthRequestDTO());
        return "authorization/sign-in";
    }

    @PostMapping("register")
    public String registerUser(@ModelAttribute("user") AuthRequestDTO authRequestDTO) {
        try {
            userService.registerUser(authRequestDTO);
        } catch (IllegalArgumentException e) {
            return "authorization/sign-up-with-errors";
        }
        return "redirect:/auth/login";
    }

    @PostMapping("login")
    public String loginUser(@ModelAttribute("user") AuthRequestDTO authRequestDTO, HttpServletResponse response) {
        try {
            UserDTO userDTO = userService.loginUser(authRequestDTO);
            UUID sessionId = sessionService.createSession(userDTO);
            CookieUtils.setSessionId(response, sessionId.toString());
        } catch (IllegalArgumentException | IncorrectPasswordException e) {
            return "authorization/sign-in-with-errors";
        }
    }

    @GetMapping("logout")
    public String logout(HttpServletRequest request, HttpServletResponse response) {
        Cookie[] cookies = request.getCookies();
        if (cookies != null) {
            Arrays.stream(cookies)
                    .filter(c -> SESSION_COOKIE_NAME.equals(c.getName()))
                    .findFirst()
                    .ifPresent(cookie -> {
                        try {
                            UUID sessionId = UUID.fromString(cookie.getValue());
                            sessionService.deleteSession(sessionId);
                        } catch (IllegalArgumentException ignored) {
                        }

                        Cookie deleteCookie = new Cookie(SESSION_COOKIE_NAME, "");
                        deleteCookie.setMaxAge(0);
                        deleteCookie.setPath("/");
                        response.addCookie(deleteCookie);
                    });
        }
        return "redirect:/auth/login";
    }

    @PostMapping("logout")
    public String logoutUser(HttpServletRequest request, HttpServletResponse response) {
        String currentUUID = CookieUtils.getSessionIdFromCookie(request);
        sessionService.deleteSessionsByID(currentUUID);
        //не знаю как можно лучше сделать вводить новую такую же переменную не хочется
        CookieUtils.deleteCookie(response, "SESSION_ID");
        return "redirect:/auth/login";
    }
}
