package com.vladko.Controllers;

import com.vladko.DTO.AuthRequestDTO;
import com.vladko.Exceptions.IncorrectPasswordException;
import com.vladko.Service.UserService;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletResponse;
import java.net.http.HttpRequest;

@Controller
@RequestMapping("/auth")
public class UsersController {
    private final UserService userService;

    public UsersController(UserService userService) {
        this.userService = userService;
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
            return "";
        }
        return "redirect:/auth/login";
    }

    @PostMapping("login")
    public String loginUser(@ModelAttribute("user") AuthRequestDTO authRequestDTO, HttpServletResponse response) {
        try {
            userService.loginUser(authRequestDTO);
        } catch (IllegalArgumentException | IncorrectPasswordException e) {
            return "authorization/sign-in-with-errors";
        }
        return "redirect:/";
    }
}
