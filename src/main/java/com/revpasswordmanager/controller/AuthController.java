package com.revpasswordmanager.controller;

import com.revpasswordmanager.dto.UserRegistrationDto;
import com.revpasswordmanager.entity.User;
import com.revpasswordmanager.service.IUserService;
import jakarta.validation.Valid;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;

@Controller
public class AuthController {

    private static final Logger logger = LogManager.getLogger(AuthController.class);

    @Autowired
    private IUserService userService;

    @GetMapping("/")
    public String index() {
        logger.info("Accessing index page.");
        return "index";
    }

    @GetMapping("/login")
    public String login() {
        logger.info("Accessing login page.");
        return "login";
    }

    @GetMapping("/register")
    public String showRegistrationForm(Model model) {
        logger.info("Displaying registration form.");
        model.addAttribute("user", new UserRegistrationDto());
        return "register";
    }

    @PostMapping("/register")
    public String registerUser(@Valid @ModelAttribute("user") UserRegistrationDto registrationDto,
            BindingResult bindingResult, Model model) {
        logger.info("Attempting to register new user with username: {}", registrationDto.getUsername());
        if (bindingResult.hasErrors()) {
            logger.warn("Registration form has errors for user: {}", registrationDto.getUsername());
            return "register";
        }
        try {
            userService.registerUser(registrationDto);
            logger.info("User registered successfully: {}", registrationDto.getUsername());
            return "redirect:/login?success";
        } catch (Exception e) {
            logger.error("Error during user registration for {}: {}", registrationDto.getUsername(), e.getMessage());
            model.addAttribute("error", e.getMessage());
            return "register";
        }
    }

    @PostMapping("/profile/update")
    public String updateProfile(@RequestParam("name") String name,
            @RequestParam("email") String email,
            @RequestParam(value = "newPassword", required = false) String newPassword,
            @RequestParam(value = "oldPassword", required = false) String oldPassword,
            Authentication authentication, Model model) {
        try {
            User user = userService.findByUsername(authentication.getName()).orElseThrow();
            user.setName(name);
            user.setEmail(email);

            if (newPassword != null && !newPassword.isEmpty()) {
                userService.updateMasterPassword(user, oldPassword, newPassword);
            } else {
                userService.updateUser(user);
            }
            return "redirect:/profile?success";
        } catch (Exception e) {
            model.addAttribute("error", e.getMessage());
            User user = userService.findByUsername(authentication.getName()).orElseThrow();
            model.addAttribute("user", user);
            return "profile";
        }
    }
}
