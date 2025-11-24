package com.vladko.Controllers;

import com.vladko.DTO.RegisterUserDTO;
import com.vladko.Service.UserService;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

@Controller
@RequestMapping("/auth")
public class UsersController {
    private final UserService userService;

    public UsersController(UserService userService) {
        this.userService = userService;
    }

    @GetMapping("register")
    public String showRegisterPage(Model model) {
        model.addAttribute("user", new RegisterUserDTO());
        return "authorization/sign-up";
    }

    @GetMapping("login")

    @PostMapping("register")
    public String registerUser(@ModelAttribute("user") RegisterUserDTO registerUserDTO) {
        userService.registerUser(registerUserDTO);
        return "redirect:/";
    }
}
